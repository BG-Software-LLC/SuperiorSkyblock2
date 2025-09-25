package com.bgsoftware.superiorskyblock.external.entities;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.EntitiesProvider;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.collections.AutoRemovalCollection;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeEntityLimits;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeMobDrops;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.google.common.collect.Multimap;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.event.EntityStackMultipleDeathEvent;
import dev.rosewood.rosestacker.stack.StackedEntity;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class EntitiesProvider_RoseStacker implements EntitiesProvider {

    private final AutoRemovalCollection<UUID> stackedEntityDeaths = AutoRemovalCollection.newHashSet(5, TimeUnit.SECONDS);

    private final SuperiorSkyblockPlugin plugin;

    public EntitiesProvider_RoseStacker(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(new DeathListener(), plugin);
    }

    @Override
    public boolean shouldTrackEntity(Entity entity) {
        return !stackedEntityDeaths.contains(entity.getUniqueId());
    }

    private class DeathListener implements Listener {

        @EventHandler
        public void onEntityDeath(EntityDeathEvent e) {
            if (!BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeEntityLimits.class) ||
                    !BukkitEntities.canHaveLimit(e.getEntityType()) ||
                    BukkitEntities.canBypassEntityLimit(e.getEntity(), false))
                return;

            StackedEntity stackedEntity = RoseStackerAPI.getInstance().getStackedEntity(e.getEntity());
            if (stackedEntity == null || stackedEntity.getStackSize() <= 1)
                return;

            Island island;
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                island = plugin.getGrid().getIslandAt(e.getEntity().getLocation(wrapper.getHandle()));
            }

            if (island == null)
                return;

            stackedEntityDeaths.add(e.getEntity().getUniqueId());
        }

        // This event is fired when a stacked entity dies, and we can use it to apply the mob drops upgrade.
        // Issue: https://github.com/BG-Software-LLC/SuperiorSkyblock2/issues/2525
        @EventHandler
        public void onEntityStackDeath(EntityStackMultipleDeathEvent event) {
            if (!BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeMobDrops.class))
                return;

            Island island = plugin.getGrid().getIslandAt(event.getStack().getLocation());
            int upgradeLevel = island == null ? 1 : island.getUpgrades().getOrDefault("mob-drops", 1);
            if (island == null || upgradeLevel < 2)
                return;

            Multimap<LivingEntity, EntityStackMultipleDeathEvent.EntityDrops> entityDrops = event.getEntityDrops();

            for (Collection<EntityStackMultipleDeathEvent.EntityDrops> drops : entityDrops.asMap().values()) {
                for (EntityStackMultipleDeathEvent.EntityDrops entityDrop : drops) {
                    for (ItemStack itemstack : entityDrop.getDrops()) {
                        itemstack.setAmount(itemstack.getAmount() * upgradeLevel);
                    }
                }
            }
        }
    }
}
