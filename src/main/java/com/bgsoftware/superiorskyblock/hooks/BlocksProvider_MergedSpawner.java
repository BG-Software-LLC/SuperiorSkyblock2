package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.vk2gpz.mergedspawner.api.MergedSpawnerAPI;
import com.vk2gpz.mergedspawner.event.MergedSpawnerBreakEvent;
import com.vk2gpz.mergedspawner.event.MergedSpawnerPlaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public final class BlocksProvider_MergedSpawner implements BlocksProvider {

    private static boolean registered = false;

    public BlocksProvider_MergedSpawner(){
        if(!registered) {
            Bukkit.getPluginManager().registerEvents(new BlocksProvider_MergedSpawner.StackerListener(), SuperiorSkyblockPlugin.getPlugin());
            registered = true;
        }
    }

    @Override
    public Pair<Integer, String> getSpawner(Location location) {
        int blockCount = -1;
        if(Bukkit.isPrimaryThread()){
            MergedSpawnerAPI spawnerAPI = MergedSpawnerAPI.getInstance();
            blockCount = spawnerAPI.getCountFor(location.getBlock());
        }
        return new Pair<>(blockCount, null);
    }

    @Override
    public String getSpawnerType(ItemStack itemStack) {
        return MergedSpawnerAPI.getInstance().getEntityType(itemStack).name();
    }

    @SuppressWarnings("unused")
    private static class StackerListener implements Listener {

        private final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerStack(MergedSpawnerPlaceEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());
            int increaseAmount = e.getAmount() - e.getSpawnerCount();
            if(island != null && increaseAmount > 1)
                island.handleBlockPlace(e.getSpawner().getLocation().getBlock(), increaseAmount - 1);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerUnstack(MergedSpawnerBreakEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());
            if(island != null)
                island.handleBlockBreak(e.getSpawner().getLocation().getBlock(), e.getPlayer().isSneaking() ? e.getAmount() : 1);
        }

    }

}
