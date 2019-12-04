package com.bgsoftware.superiorskyblock.utils.blocks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.Pair;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class BlockChangeTask {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Map<ChunkPosition,  List<Pair<Location, Integer>>> blocksCache = Maps.newConcurrentMap();
    private final List<Chunk> chunksToUpdate = new ArrayList<>();
    private final List<Runnable> runnablesOnFinish = new ArrayList<>();

    private boolean submitted = false;

    public void setBlock(Location location, int combinedId, Runnable onFinish){
        if(submitted)
            throw new IllegalArgumentException("This MultiBlockChange was already submitted.");

        ChunkPosition chunkPosition = new ChunkPosition(location.getWorld().getName(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
        blocksCache.computeIfAbsent(chunkPosition, pairs -> new ArrayList<>()).add(new Pair<>(location, combinedId));
        if(onFinish != null)
            runnablesOnFinish.add(onFinish);
    }

    public void submitUpdate(Runnable onFinish){
        if(submitted)
            throw new IllegalArgumentException("This MultiBlockChange was already submitted.");

        submitted = true;

        ExecutorService executor = Executors.newCachedThreadPool();
        for(Map.Entry<ChunkPosition, List<Pair<Location, Integer>>> entry : blocksCache.entrySet()){
            Chunk chunk = Bukkit.getWorld(entry.getKey().world).getChunkAt(entry.getKey().x, entry.getKey().z);
            chunksToUpdate.add(chunk);
            executor.execute(() -> entry.getValue().forEach(pair -> plugin.getNMSAdapter().setBlock(chunk, pair.getKey(), pair.getValue())));
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
           try{
               executor.shutdown();
               executor.awaitTermination(1, TimeUnit.MINUTES);
           }catch(Exception ex){
               ex.printStackTrace();
               return;
           }

           blocksCache.clear();

           Bukkit.getScheduler().runTask(plugin, () -> {
               chunksToUpdate.forEach(plugin.getNMSAdapter()::refreshChunk);
               runnablesOnFinish.forEach(Runnable::run);
               chunksToUpdate.clear();
               runnablesOnFinish.clear();

               if(onFinish != null)
                   onFinish.run();
           });
        });

    }

    private static class ChunkPosition{

        private String world;
        private int x, z;

        ChunkPosition(String world, int x, int z){
            this.world = world;
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChunkPosition that = (ChunkPosition) o;
            return x == that.x &&
                    z == that.z &&
                    Objects.equals(world, that.world);
        }

        @Override
        public int hashCode() {
            return Objects.hash(world, x, z);
        }
    }

}
