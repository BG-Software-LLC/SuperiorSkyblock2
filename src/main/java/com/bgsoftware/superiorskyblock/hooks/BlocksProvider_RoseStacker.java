package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.event.SpawnerStackEvent;
import dev.rosewood.rosestacker.event.SpawnerUnstackEvent;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.utils.StackerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class BlocksProvider_RoseStacker implements BlocksProvider {

    private static boolean registered = false;

    public BlocksProvider_RoseStacker(){
        if(!registered) {
            Bukkit.getPluginManager().registerEvents(new BlocksProvider_RoseStacker.StackerListener(), SuperiorSkyblockPlugin.getPlugin());
            registered = true;
            SuperiorSkyblockPlugin.log("Using RoseStacker as a spawners provider.");
        }
    }

    @Override
    public Pair<Integer, String> getSpawner(Location location) {
        int blockCount = -1;
        if(Bukkit.isPrimaryThread()){
            StackedSpawner stackedSpawner = RoseStackerAPI.getInstance().getStackedSpawner(location.getBlock());
            blockCount = stackedSpawner == null ? 1 : stackedSpawner.getStackSize();
        }
        return new Pair<>(blockCount, null);
    }

    @Override
    public String getSpawnerType(ItemStack itemStack) {
        return StackerUtils.getStackedItemEntityType(itemStack).name();
    }

    @Override
    public Set<Pair<com.bgsoftware.superiorskyblock.api.key.Key, Integer>> getBlocks(ChunkPosition chunkPosition) {
        if(!Bukkit.isPrimaryThread())
            return null;

        Map<com.bgsoftware.superiorskyblock.api.key.Key, Integer> blockKeys = new HashMap<>();
        RoseStackerAPI.getInstance().getStackedBlocks().entrySet().stream()
                .filter(entry -> chunkPosition.isInsideChunk(entry.getKey().getLocation()))
                .forEach(entry -> {
                    com.bgsoftware.superiorskyblock.api.key.Key blockKey = Key.of(entry.getKey());
                    blockKeys.put(blockKey, blockKeys.getOrDefault(blockKey, 0) + 1);
                });
        return blockKeys.entrySet().stream().map(entry -> new Pair<>(entry.getKey(), entry.getValue())).collect(Collectors.toSet());
    }

    public static boolean isRegistered(){
        return registered;
    }

    @SuppressWarnings("unused")
    private static class StackerListener implements Listener {

        private final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerStack(SpawnerStackEvent e){
            Location location = e.getStack().getLocation();
            Island island = plugin.getGrid().getIslandAt(location);
            if(island != null) {
                Key spawnerKey = Key.of(Materials.SPAWNER.toBukkitType() + ":" + e.getStack().getSpawner().getSpawnedType());
                island.handleBlockPlace(spawnerKey, e.getIncreaseAmount());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerUnstack(SpawnerUnstackEvent e){
            Location location = e.getStack().getLocation();
            Island island = plugin.getGrid().getIslandAt(location);
            if(island != null) {
                Key spawnerKey = Key.of(Materials.SPAWNER.toBukkitType() + ":" + e.getStack().getSpawner().getSpawnedType());
                island.handleBlockBreak(spawnerKey, e.getDecreaseAmount());
            }
        }

    }

}
