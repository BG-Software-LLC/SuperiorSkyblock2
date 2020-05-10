package com.bgsoftware.superiorskyblock.utils.chunks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.hooks.PaperHook;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public final class ChunksProvider {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ConcurrentLinkedQueue<ChunkPosition> pendingChunks = new ConcurrentLinkedQueue<>();

    private static final Map<ChunkPosition, Pair<CompletableFuture<Chunk>, Set<Consumer<Chunk>>>>
            chunksInfo = new ConcurrentHashMap<>();

    private static BukkitTask chunksLoaderId;

    public static CompletableFuture<Chunk> loadChunk(World world, int x, int z, Consumer<Chunk> onLoadConsumer){
        ChunkPosition chunkPosition = ChunkPosition.of(world, x, z);
        Pair<CompletableFuture<Chunk>, Set<Consumer<Chunk>>> chunkInfo = chunksInfo.get(chunkPosition);

        if(chunkInfo != null){
            if(onLoadConsumer != null)
                chunkInfo.getValue().add(onLoadConsumer);
            return chunkInfo.getKey();
        }
        else {
            CompletableFuture<Chunk> completableFuture = new CompletableFuture<>();
            Set<Consumer<Chunk>> chunkConsumers = new HashSet<>();

            if(onLoadConsumer != null)
                chunkConsumers.add(onLoadConsumer);

            chunksInfo.put(chunkPosition, new Pair<>(completableFuture, chunkConsumers));
            pendingChunks.add(chunkPosition);
            return completableFuture;
        }
    }

    public static void stop(){
        if(chunksLoaderId != null)
            chunksLoaderId.cancel();
    }

    public static void init(){
        chunksLoaderId = runChunksLoader();
    }

    private static BukkitTask runChunksLoader(){
        Runnable loadChunks = () -> {
            for(int i = 0; i < plugin.getSettings().chunksPerTick; i++) {
                ChunkPosition chunkPosition = pendingChunks.poll();

                if (chunkPosition == null)
                    return;

                Chunk chunk = chunkPosition.loadChunk();

                if(!Bukkit.isPrimaryThread()){
                    Executor.sync(() -> finishLoad(chunkPosition, chunk));
                }
                else{
                    finishLoad(chunkPosition, chunk);
                }
            }
        };

        if(PaperHook.isUsingPaper() && ServerVersion.isAtLeast(ServerVersion.v1_13)) {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, loadChunks, 1L, 1L);
        }

        else{
            return Bukkit.getScheduler().runTaskTimer(plugin, loadChunks, 1L, 1L);
        }
    }

    private static void finishLoad(ChunkPosition chunkPosition, Chunk chunk){
        Pair<CompletableFuture<Chunk>, Set<Consumer<Chunk>>> chunkInfo = chunksInfo.remove(chunkPosition);
        if(chunkInfo != null) {
            chunkInfo.getValue().forEach(chunkConsumer -> chunkConsumer.accept(chunk));
            chunkInfo.getKey().complete(chunk);
        }
    }

}
