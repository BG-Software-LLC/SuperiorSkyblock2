package com.bgsoftware.superiorskyblock.hooks.provider;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.EntityProvider;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.events.EntityStackEvent;
import com.bgsoftware.wildstacker.api.events.EntityUnstackEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class EntityProvider_WildStacker implements EntityProvider {

    private static boolean registered = false;

    private final SuperiorSkyblockPlugin plugin;

    public EntityProvider_WildStacker(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        if (!registered) {
            registered = true;
            Bukkit.getPluginManager().registerEvents(new StackingListener(), plugin);
        }
    }

    @Override
    public int getEntityAmount(Entity entity) {
        return entity instanceof LivingEntity ? WildStackerAPI.getEntityAmount((LivingEntity) entity) : 1;
    }

    private final class StackingListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onEntityStack(EntityStackEvent event) {
            Island island = plugin.getGrid().getIslandAt(event.getTarget().getLocation());
            if (island != null) {
                // We want to increase the entity track amount by 1, as it will
                // later get decreased by one when the entity is removed from the world.
                island.getEntitiesTracker().trackEntity(Key.of(event.getEntity().getLivingEntity()), 1);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onEntityUnstack(EntityUnstackEvent event) {
            Island island = plugin.getGrid().getIslandAt(event.getEntity().getLocation());
            if (island != null)
                island.getEntitiesTracker().untrackEntity(Key.of(event.getEntity().getLivingEntity()), event.getAmount());
        }

    }

}
