package com.bgsoftware.superiorskyblock.missions;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.api.missions.MissionLoadException;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Counter;
import com.bgsoftware.superiorskyblock.missions.common.BuiltinMission;
import com.bgsoftware.superiorskyblock.missions.common.Placeholders;
import com.bgsoftware.superiorskyblock.missions.common.requirements.KeyRequirements;
import com.bgsoftware.superiorskyblock.missions.common.tracker.KeyDataTracker;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class FishingMissions extends BuiltinMission<KeyDataTracker> implements Listener {

    private final Map<KeyRequirements, Integer> itemsToCatch = new LinkedHashMap<>();

    @Override
    protected void loadConfiguration(ConfigurationSection section) throws MissionLoadException {
        ConfigurationSection requiredCaughtsSection = section.getConfigurationSection("required-caughts");
        if (requiredCaughtsSection == null)
            throw new MissionLoadException("You must have the \"required-caughts\" section in the config.");

        for (String key : requiredCaughtsSection.getKeys(false)) {
            KeySet blocks = KeySet.createKeySet();
            section.getStringList("required-caughts." + key + ".types").forEach(requiredBlock ->
                    blocks.add(Key.ofMaterialAndData(requiredBlock.toUpperCase(Locale.ENGLISH))));
            int amount = section.getInt("required-caughts." + key + ".amount", 1);
            this.itemsToCatch.put(new KeyRequirements(blocks), amount);
        }
    }

    @Override
    protected void registerListeners() {
        registerListener(this);
    }

    @Override
    protected void clearModuleData(KeyDataTracker data) {
        data.clear();
    }

    @Override
    public double getProgress(SuperiorPlayer superiorPlayer) {
        KeyDataTracker fishingTracker = get(superiorPlayer);

        if (fishingTracker == null)
            return 0.0;

        int requiredItems = 0;
        int interactions = 0;

        for (Map.Entry<KeyRequirements, Integer> entry : this.itemsToCatch.entrySet()) {
            requiredItems += entry.getValue();
            interactions += Math.min(fishingTracker.getCounts(entry.getKey()), entry.getValue());
        }

        return (double) interactions / requiredItems;
    }

    @Override
    public int getProgressValue(SuperiorPlayer superiorPlayer) {
        KeyDataTracker fishingTracker = get(superiorPlayer);

        if (fishingTracker == null)
            return 0;

        int interactions = 0;

        for (Map.Entry<KeyRequirements, Integer> entry : this.itemsToCatch.entrySet())
            interactions += Math.min(fishingTracker.getCounts(entry.getKey()), entry.getValue());

        return interactions;
    }

    @Override
    public void saveProgress(ConfigurationSection section) {
        for (Map.Entry<SuperiorPlayer, KeyDataTracker> entry : entrySet()) {
            String uuid = entry.getKey().getUniqueId().toString();
            int index = 0;
            for (Map.Entry<Key, Counter> craftedEntry : entry.getValue().getCounts().entrySet()) {
                section.set(uuid + "." + index + ".item", craftedEntry.getKey().toString());
                section.set(uuid + "." + index + ".amount", craftedEntry.getValue().get());
                index++;
            }
        }
    }

    @Override
    public void loadProgress(ConfigurationSection section) {
        for (String uuid : section.getKeys(false)) {
            if (uuid.equals("players"))
                continue;

            KeyDataTracker fishingTracker = new KeyDataTracker();
            UUID playerUUID = UUID.fromString(uuid);
            SuperiorPlayer superiorPlayer = this.plugin.getPlayers().getSuperiorPlayer(playerUUID);

            insertData(superiorPlayer, fishingTracker);

            for (String key : section.getConfigurationSection(uuid).getKeys(false)) {
                Key typeKey;
                if (section.isItemStack(uuid + "." + key + ".item")) {
                    ItemStack itemStack = section.getItemStack(uuid + "." + key + ".item");
                    typeKey = itemStack == null ? null : Key.of(itemStack);
                } else {
                    typeKey = Key.ofMaterialAndData(section.getString(uuid + "." + key + ".item"));
                }
                if (typeKey != null) {
                    typeKey = getMissionItemKey(typeKey);
                    if (typeKey == null) continue;
                    int amount = section.getInt(uuid + "." + key + ".amount");
                    fishingTracker.load(typeKey, amount);
                }
            }
        }
    }

    @Override
    public void formatItem(SuperiorPlayer superiorPlayer, ItemStack itemStack) {
        KeyDataTracker fishingTracker = getOrCreate(superiorPlayer, s -> new KeyDataTracker());

        if (fishingTracker == null)
            return;

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null)
            return;

        if (itemMeta.hasDisplayName())
            itemMeta.setDisplayName(Placeholders.parseKeyPlaceholders(this.itemsToCatch, fishingTracker, itemMeta.getDisplayName(), true));

        if (itemMeta.hasLore()) {
            List<String> lore = new ArrayList<>();
            for (String line : Objects.requireNonNull(itemMeta.getLore()))
                lore.add(Placeholders.parseKeyPlaceholders(this.itemsToCatch, fishingTracker, line, true));
            itemMeta.setLore(lore);
        }

        itemStack.setItemMeta(itemMeta);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent e) {
        if (!(e.getCaught() instanceof Item) || e.getState() != PlayerFishEvent.State.CAUGHT_FISH)
            return;

        SuperiorPlayer superiorPlayer = this.plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if (!this.plugin.getMissions().canCompleteNoProgress(superiorPlayer, this))
            return;

        Item caughtItem = (Item) e.getCaught();
        ItemStack caughtItemStack = caughtItem.getItemStack();

        Key itemKey = getMissionItemKey(Key.of(caughtItemStack));
        if (itemKey == null)
            return;

        trackItem(superiorPlayer, itemKey, caughtItemStack.getAmount());
    }

    private void trackItem(SuperiorPlayer superiorPlayer, Key itemKey, int count) {
        KeyDataTracker blocksTracker = getOrCreate(superiorPlayer, s -> new KeyDataTracker());

        if (blocksTracker == null)
            return;

        blocksTracker.track(itemKey, count);

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> superiorPlayer.runIfOnline(player -> {
            if (canComplete(superiorPlayer))
                this.plugin.getMissions().rewardMission(this, superiorPlayer, true);
        }), 2L);
    }

    @Nullable
    private Key getMissionItemKey(Key blockKey) {
        for (KeyRequirements requirementsList : itemsToCatch.keySet()) {
            if (requirementsList.contains(blockKey))
                return requirementsList.getKey(blockKey);
        }

        return null;
    }

}
