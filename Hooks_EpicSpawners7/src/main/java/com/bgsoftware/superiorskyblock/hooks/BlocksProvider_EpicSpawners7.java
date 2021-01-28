package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.api.events.SpawnerBreakEvent;
import com.songoda.epicspawners.api.events.SpawnerChangeEvent;
import com.songoda.epicspawners.api.events.SpawnerPlaceEvent;
import com.songoda.epicspawners.spawners.spawner.PlacedSpawner;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("unused")
public final class BlocksProvider_EpicSpawners7 implements BlocksProvider {

    private static boolean registered = false;

    private final EpicSpawners instance = EpicSpawners.getInstance();

    public BlocksProvider_EpicSpawners7(){
        if(!registered) {
            Bukkit.getPluginManager().registerEvents(new BlocksProvider_EpicSpawners7.StackerListener(), SuperiorSkyblockPlugin.getPlugin());
            registered = true;
            SuperiorSkyblockPlugin.log("Using EpicSpawners as a spawners provider.");
        }
    }

    @Override
    public Pair<Integer, String> getSpawner(Location location) {
        int blockCount = -1;
        String entityType = null;
        if(Bukkit.isPrimaryThread()){
            PlacedSpawner placedSpawner = instance.getSpawnerManager().getSpawnerFromWorld(location);
            blockCount = placedSpawner.getFirstStack().getStackSize();
            entityType = placedSpawner.getIdentifyingName();
        }
        return new Pair<>(blockCount, entityType);
    }

    @Override
    public String getSpawnerType(ItemStack itemStack) {
        return instance.getSpawnerManager().getSpawnerTier(itemStack).getSpawnerData().getIdentifyingName();
    }

    private static class StackerListener implements Listener {

        private final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onSpawnerStack(SpawnerPlaceEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());

            if(island == null)
                return;

            Key blockKey = Key.of(Materials.SPAWNER.toBukkitType() + ":" + e.getSpawner().getIdentifyingName());
            int increaseAmount = e.getSpawner().getFirstStack().getStackSize();

            // When the spawner is a vanilla one, SSB already handles one placement from vanilla events
            // However, custom ones aren't handled there and only here, so we don't subtract one from the amount.
            if(Key.of(e.getSpawner().getLocation().getBlock()).equals(blockKey))
                increaseAmount--;

            if(island.hasReachedBlockLimit(blockKey, increaseAmount)){
                e.setCancelled(true);
                Locale.REACHED_BLOCK_LIMIT.send(e.getPlayer(), StringUtils.format(blockKey.toString()));
            }

            else{
                island.handleBlockPlace(blockKey, increaseAmount);
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onSpawnerStack(SpawnerChangeEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());

            if(island == null)
                return;

            Key blockKey = Key.of(Materials.SPAWNER.toBukkitType() + ":" + e.getSpawner().getIdentifyingName());

            int increaseAmount = e.getStackSize() - e.getOldStackSize();

            if(increaseAmount < 0){
                island.handleBlockBreak(blockKey, -increaseAmount);
            }

            else if(island.hasReachedBlockLimit(blockKey, increaseAmount)){
                e.setCancelled(true);
                Locale.REACHED_BLOCK_LIMIT.send(e.getPlayer(), StringUtils.format(blockKey.toString()));
            }

            else{
                island.handleBlockPlace(blockKey, increaseAmount);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerUnstack(SpawnerBreakEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());

            if(island == null)
                return;

            Key blockKey = Key.of(Materials.SPAWNER.toBukkitType() + ":" + e.getSpawner().getIdentifyingName());

            island.handleBlockBreak(blockKey, e.getSpawner().getFirstStack().getStackSize());
        }

    }

}
