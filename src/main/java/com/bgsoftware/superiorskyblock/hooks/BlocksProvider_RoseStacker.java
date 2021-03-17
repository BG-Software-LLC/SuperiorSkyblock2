package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.google.common.base.Preconditions;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.event.BlockStackEvent;
import dev.rosewood.rosestacker.event.BlockUnstackEvent;
import dev.rosewood.rosestacker.event.SpawnerStackEvent;
import dev.rosewood.rosestacker.event.SpawnerUnstackEvent;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.utils.ItemUtils;
import dev.rosewood.rosestacker.utils.StackerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class BlocksProvider_RoseStacker implements BlocksProvider {

    private static ReflectMethod<EntityType> GET_STACKED_ITEM_ENTITY_TYPE = null;
    private static boolean registered = false;

    public BlocksProvider_RoseStacker(){
        if(!registered) {
            Bukkit.getPluginManager().registerEvents(new BlocksProvider_RoseStacker.StackerListener(), SuperiorSkyblockPlugin.getPlugin());
            registered = true;

            GET_STACKED_ITEM_ENTITY_TYPE = new ReflectMethod<>(StackerUtils.class, "getStackedItemEntityType", ItemStack.class);

            SuperiorSkyblockPlugin.log("Using RoseStacker as a spawners provider.");
        }
    }

    @Override
    public Pair<Integer, String> getSpawner(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");

        int blockCount = -1;

        if(Bukkit.isPrimaryThread()){
            StackedSpawner stackedSpawner = RoseStackerAPI.getInstance().getStackedSpawner(location.getBlock());
            blockCount = stackedSpawner == null ? 1 : stackedSpawner.getStackSize();
        }

        return new Pair<>(blockCount, null);
    }

    @Override
    public String getSpawnerType(ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "itemStack parameter cannot be null.");
        return GET_STACKED_ITEM_ENTITY_TYPE.isValid() ?
                GET_STACKED_ITEM_ENTITY_TYPE.invoke(null, itemStack).name() :
                ItemUtils.getStackedItemEntityType(itemStack).name();
    }

    @Override
    public Set<Pair<com.bgsoftware.superiorskyblock.api.key.Key, Integer>> getBlocks(ChunkPosition chunkPosition) {
        Preconditions.checkNotNull(chunkPosition, "chunkPosition parameter cannot be null.");

        if(!Bukkit.isPrimaryThread())
            return null;

        Map<com.bgsoftware.superiorskyblock.api.key.Key, Integer> blockKeys = new HashMap<>();
        RoseStackerAPI.getInstance().getStackedBlocks().entrySet().stream()
                .filter(entry -> chunkPosition.isInsideChunk(entry.getKey().getLocation()))
                .forEach(entry -> {
                    com.bgsoftware.superiorskyblock.api.key.Key blockKey = Key.of(entry.getKey());
                    blockKeys.put(blockKey, blockKeys.getOrDefault(blockKey, 0) + entry.getValue().getStackSize());
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

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBlockStack(BlockStackEvent e){
            Location location = e.getStack().getLocation();
            Island island = plugin.getGrid().getIslandAt(location);
            if(island != null) {
                Key blockKey = Key.of(e.getStack().getBlock());
                island.handleBlockPlace(blockKey, e.isNew() ? e.getIncreaseAmount() - 1 : e.getIncreaseAmount());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBlockUnstack(BlockUnstackEvent e){
            Location location = e.getStack().getLocation();
            Island island = plugin.getGrid().getIslandAt(location);
            if(island != null) {
                Key blockKey = Key.of(e.getStack().getBlock());
                island.handleBlockBreak(blockKey, e.getDecreaseAmount());
            }
        }

    }

}
