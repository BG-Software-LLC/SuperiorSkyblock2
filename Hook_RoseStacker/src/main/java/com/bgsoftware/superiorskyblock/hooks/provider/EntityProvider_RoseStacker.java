package com.bgsoftware.superiorskyblock.hooks.provider;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.EntityProvider;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.key.Key;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.event.EntityStackClearEvent;
import dev.rosewood.rosestacker.event.EntityStackEvent;
import dev.rosewood.rosestacker.event.EntityUnstackEvent;
import dev.rosewood.rosestacker.stack.StackedEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class EntityProvider_RoseStacker implements EntityProvider {

    private static boolean registered = false;

    private final SuperiorSkyblockPlugin plugin;

    public EntityProvider_RoseStacker(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        if (!registered) {
            registered = true;
            Bukkit.getPluginManager().registerEvents(new StackingListener(), plugin);
        }
    }

    @Override
    public int getEntityAmount(Entity entity) {
        if (!(entity instanceof LivingEntity))
            return 1;

        StackedEntity stackedEntity = RoseStackerAPI.getInstance().getStackedEntity((LivingEntity) entity);

        return stackedEntity == null ? 1 : stackedEntity.getStackSize();
    }

    private final class StackingListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onEntityStack(EntityStackEvent event) {
            Island island = plugin.getGrid().getIslandAt(event.getStack().getLocation());
            if (island != null) {
                Key entityKey = Key.of(event.getStack().getEntity());

                island.getEntitiesTracker().untrackEntity(entityKey, 1);
                if (island.hasReachedEntityLimit(entityKey).getNow(false)) {
                    event.setCancelled(true);
                    island.getEntitiesTracker().trackEntity(entityKey, 1);
                } else {
                    // We want to increase the entity track amount by 1, as it will
                    // later get decreased by one when the entity is removed from the world.
                    island.getEntitiesTracker().trackEntity(entityKey, 2);
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onEntityUnstack(EntityUnstackEvent event) {
//            Island island = plugin.getGrid().getIslandAt(event.getStack().getLocation());
//            if (island != null)
//                island.getEntitiesTracker().untrackEntity(Key.of(event.getStack().getEntity()), event. ());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onEntityClear(EntityStackClearEvent event) {
            if (!plugin.getGrid().isIslandsWorld(event.getWorld()))
                return;

            for (StackedEntity stackedEntity : event.getStacks()) {
                Island island = plugin.getGrid().getIslandAt(stackedEntity.getLocation());
                if (island != null) {
                    island.getEntitiesTracker().untrackEntity(Key.of(stackedEntity.getEntity()),
                            stackedEntity.getStackSize() - 1);
                }
            }
        }

    }

}
