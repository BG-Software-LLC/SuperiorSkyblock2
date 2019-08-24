package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.utils.Pair;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.api.events.SpawnerBreakEvent;
import com.songoda.epicspawners.api.events.SpawnerChangeEvent;
import com.songoda.epicspawners.api.events.SpawnerPlaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class BlocksProvider_EpicSpawners implements BlocksProvider {

    private final EpicSpawners instance = EpicSpawners.getInstance();

    public BlocksProvider_EpicSpawners(){
        Bukkit.getPluginManager().registerEvents(new StackerListener(), SuperiorSkyblockPlugin.getPlugin());
    }

    @Override
    public Pair<Integer, EntityType> getSpawner(Location location) {
        int blockCount = -1;
        if(Bukkit.isPrimaryThread()){
            blockCount = instance.getSpawnerManager().getSpawnerFromWorld(location).getFirstStack().getStackSize();
        }
        return new Pair<>(blockCount, null);
    }

    @SuppressWarnings("unused")
    private static class StackerListener implements Listener {

        private final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerStack(SpawnerPlaceEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());
            if(island != null){
                Executor.sync(() ->
                        island.handleBlockPlace(e.getSpawner().getLocation().getBlock(), e.getSpawner().getFirstStack().getStackSize() - 1), 1L);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerStack(SpawnerChangeEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());
            if(island != null) {
                Executor.sync(() ->
                        island.handleBlockPlace(e.getSpawner().getLocation().getBlock(), e.getStackSize() - e.getOldStackSize()), 1L);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerUnstack(SpawnerBreakEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());
            if(island != null)
                island.handleBlockBreak(e.getSpawner().getLocation().getBlock(), e.getSpawner().getFirstStack().getStackSize());
        }

    }

}
