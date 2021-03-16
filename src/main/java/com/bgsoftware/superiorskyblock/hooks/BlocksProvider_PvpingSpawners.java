package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import skyblock.hassan.plugin.Main;
import skyblock.hassan.plugin.api.SpawnerStackEvent;
import skyblock.hassan.plugin.api.SpawnerUnstackEvent;
import skyblock.hassan.plugin.spawners.StackedSpawner;

public final class BlocksProvider_PvpingSpawners implements BlocksProvider{

    private static boolean registered = false;

    private final Main main;

    public BlocksProvider_PvpingSpawners(){
        main = (Main) Bukkit.getPluginManager().getPlugin("PvpingSpawners");
        if(!registered) {
            Bukkit.getPluginManager().registerEvents(new StackerListener(), SuperiorSkyblockPlugin.getPlugin());
            registered = true;
            SuperiorSkyblockPlugin.log("Using PvpingSpawners as a spawners provider.");
        }
    }

    @Override
    public Pair<Integer, String> getSpawner(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");

        int blockCount = -1;
        if(Bukkit.isPrimaryThread()){
            StackedSpawner stackedSpawner = main.getProps().getStackedSpawner(main, (CreatureSpawner) location.getBlock().getState());
            blockCount = stackedSpawner.getSize();
        }

        return new Pair<>(blockCount, null);
    }

    @SuppressWarnings("unused")
    private static class StackerListener implements Listener {

        private final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerStack(SpawnerStackEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());
            if(island != null)
                island.handleBlockPlace(e.getSpawner().getLocation().getBlock(), e.getSpawnerAmount());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerUnstack(SpawnerUnstackEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());
            if(island != null)
                island.handleBlockBreak(e.getSpawner().getLocation().getBlock(), e.getSpawnerAmount());
        }

    }

}
