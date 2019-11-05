package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.utils.Pair;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
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
import org.bukkit.inventory.ItemStack;

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

    @Override
    public Key getSpawnerKey(ItemStack itemStack) {
        return Key.of(Materials.SPAWNER.toBukkitType() + ":" + instance.getSpawnerManager().getSpawnerData(itemStack).getEntities().get(0));
    }

    @SuppressWarnings("unused")
    private static class StackerListener implements Listener {

        private final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onSpawnerStack(SpawnerPlaceEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());

            if(island == null)
                return;

            Key blockKey = Key.of(Materials.SPAWNER.toBukkitType() + ":" + e.getSpawner().getIdentifyingName().toUpperCase().replace(' ', '_'));

            int blockLimit = island.getBlockLimit(blockKey), increaseAmount = e.getSpawner().getFirstStack().getStackSize() - 1;

            if(blockLimit > SIsland.NO_BLOCK_LIMIT && island.getBlockCount(blockKey) + increaseAmount > blockLimit){
                e.setCancelled(true);
                Locale.REACHED_BLOCK_LIMIT.send(e.getPlayer(), StringUtils.format(Materials.SPAWNER.toBukkitType() + ""));
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

            Key blockKey = Key.of(Materials.SPAWNER.toBukkitType() + ":" + e.getSpawner().getIdentifyingName().toUpperCase().replace(' ', '_'));

            int blockLimit = island.getBlockLimit(blockKey), increaseAmount = e.getStackSize() - e.getOldStackSize();

            if(increaseAmount < 0){
                island.handleBlockBreak(blockKey, -increaseAmount);
            }
            else if(blockLimit > SIsland.NO_BLOCK_LIMIT && island.getBlockCount(blockKey) + increaseAmount > blockLimit){
                e.setCancelled(true);
                Locale.REACHED_BLOCK_LIMIT.send(e.getPlayer(), StringUtils.format(Materials.SPAWNER.toBukkitType() + ""));
            }

            else{
                island.handleBlockPlace(blockKey, increaseAmount);
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
