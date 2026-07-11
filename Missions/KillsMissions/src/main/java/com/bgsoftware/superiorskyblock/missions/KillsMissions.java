package com.bgsoftware.superiorskyblock.missions;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.api.missions.MissionLoadException;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.missions.common.BuiltinMission;
import com.bgsoftware.superiorskyblock.missions.common.Placeholders;
import com.bgsoftware.superiorskyblock.missions.common.requirements.KeyRequirements;
import com.bgsoftware.superiorskyblock.missions.common.tracker.KeyDataTracker;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public final class KillsMissions extends BuiltinMission<KeyDataTracker> implements Listener {

    private final Map<KeyRequirements, Integer> requiredEntities = new LinkedHashMap<>();

    private Function<LivingEntity, Integer> getEntityCount;

    @Override
    protected void loadConfiguration(ConfigurationSection section) throws MissionLoadException {
        ConfigurationSection requiredEntitiesSection = section.getConfigurationSection("required-entities");

        if (requiredEntitiesSection == null)
            throw new MissionLoadException("You must have the \"required-entities\" section in the config.");

        for (String key : requiredEntitiesSection.getKeys(false)) {
            KeySet entityTypes = KeySet.createKeySet();
            section.getStringList("required-entities." + key + ".types").forEach(entityTypeName ->
                    entityTypes.add(Key.ofEntityType(entityTypeName.toUpperCase(Locale.ENGLISH))));
            int requiredAmount = section.getInt("required-entities." + key + ".amount");
            requiredEntities.put(new KeyRequirements(entityTypes), requiredAmount);
        }
    }

    @Override
    protected void registerListeners() {
        registerListener(this);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getServer().getPluginManager().isPluginEnabled("WildStacker")) {
                this.getEntityCount = WildStackerAPI::getEntityAmount;
            } else {
                this.getEntityCount = entity -> 1;
            }
        }, 1L);
    }

    @Override
    protected void clearModuleData(KeyDataTracker data) {
        data.clear();
    }

    @Override
    public double getProgress(SuperiorPlayer superiorPlayer) {
        KeyDataTracker killsTracker = get(superiorPlayer);

        if (killsTracker == null)
            return 0.0;

        int requiredEntities = 0;
        int kills = 0;

        for (Map.Entry<KeyRequirements, Integer> requiredEntity : this.requiredEntities.entrySet()) {
            requiredEntities += requiredEntity.getValue();
            kills += Math.min(killsTracker.getCounts(requiredEntity.getKey()), requiredEntity.getValue());
        }

        return (double) kills / requiredEntities;
    }

    @Override
    public int getProgressValue(SuperiorPlayer superiorPlayer) {
        KeyDataTracker killsTracker = get(superiorPlayer);

        if (killsTracker == null)
            return 0;

        int kills = 0;

        for (Map.Entry<KeyRequirements, Integer> requiredEntity : this.requiredEntities.entrySet())
            kills += Math.min(killsTracker.getCounts(requiredEntity.getKey()), requiredEntity.getValue());

        return kills;
    }

    @Override
    public void saveProgress(ConfigurationSection section) {
        for (Map.Entry<SuperiorPlayer, KeyDataTracker> entry : entrySet()) {
            String uuid = entry.getKey().getUniqueId().toString();
            entry.getValue().getCounts().forEach((killedType, count) ->
                    section.set(uuid + "." + killedType, count.get()));
        }
    }

    @Override
    public void loadProgress(ConfigurationSection section) {
        for (String uuid : section.getKeys(false)) {
            KeyDataTracker killsTracker = new KeyDataTracker();
            UUID playerUUID = UUID.fromString(uuid);
            SuperiorPlayer superiorPlayer = this.plugin.getPlayers().getSuperiorPlayer(playerUUID);

            insertData(superiorPlayer, killsTracker);

            for (String key : section.getConfigurationSection(uuid).getKeys(false)) {
                killsTracker.load(Key.ofEntityType(key), section.getInt(uuid + "." + key));
            }
        }
    }

    @Override
    public void formatItem(SuperiorPlayer superiorPlayer, ItemStack itemStack) {
        KeyDataTracker killsTracker = getOrCreate(superiorPlayer, s -> new KeyDataTracker());

        if (killsTracker == null)
            return;

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null)
            return;

        if (itemMeta.hasDisplayName())
            itemMeta.setDisplayName(Placeholders.parseKeyPlaceholders(this.requiredEntities, killsTracker, itemMeta.getDisplayName(), false));

        if (itemMeta.hasLore()) {
            List<String> lore = new ArrayList<>();
            for (String line : Objects.requireNonNull(itemMeta.getLore()))
                lore.add(Placeholders.parseKeyPlaceholders(this.requiredEntities, killsTracker, line, false));
            itemMeta.setLore(lore);
        }

        itemStack.setItemMeta(itemMeta);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityKill(EntityDeathEvent e) {
        if (!isMissionEntity(e.getEntity()))
            return;

        EntityDamageEvent damageCause = e.getEntity().getLastDamageCause();

        if (!(damageCause instanceof EntityDamageByEntityEvent))
            return;

        EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) damageCause;

        Player damager = null;

        if (entityDamageByEntityEvent.getDamager() instanceof Player) {
            damager = (Player) entityDamageByEntityEvent.getDamager();
        } else if (entityDamageByEntityEvent.getDamager() instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) entityDamageByEntityEvent.getDamager()).getShooter();
            if (shooter instanceof Player)
                damager = (Player) shooter;
        }

        if (damager == null)
            return;

        SuperiorPlayer superiorPlayer = this.plugin.getPlayers().getSuperiorPlayer(damager);

        if (!this.plugin.getMissions().canCompleteNoProgress(superiorPlayer, this))
            return;

        KeyDataTracker killsTracker = getOrCreate(superiorPlayer, s -> new KeyDataTracker());

        if (killsTracker == null)
            return;

        killsTracker.track(Key.of(e.getEntity()), this.getEntityCount.apply(e.getEntity()));

        Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, () -> superiorPlayer.runIfOnline(player -> {
            if (canComplete(superiorPlayer))
                this.plugin.getMissions().rewardMission(this, superiorPlayer, true);
        }), 2L);
    }

    private boolean isMissionEntity(@Nullable Entity entity) {
        if (entity == null || entity instanceof ArmorStand)
            return false;

        Key entityKey = Key.of(entity);

        for (KeyRequirements requirement : requiredEntities.keySet()) {
            if (requirement.contains(entityKey))
                return true;
        }

        return false;
    }

}
