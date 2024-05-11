package com.bgsoftware.superiorskyblock.external.entities;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.EntitiesProvider;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.collections.AutoRemovalCollection;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeEntityLimits;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.stack.StackedEntity;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

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

            Location location = e.getEntity().getLocation();

            Island island = plugin.getGrid().getIslandAt(location);

            if (island == null)
                return;

            stackedEntityDeaths.add(e.getEntity().getUniqueId());
        }

    }

}
