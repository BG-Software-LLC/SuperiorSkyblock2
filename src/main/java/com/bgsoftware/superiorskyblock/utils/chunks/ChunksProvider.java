package com.bgsoftware.superiorskyblock.utils.chunks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.hooks.PaperHook;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
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

    private ChunksProvider(){

    }

    public static CompletableFuture<Chunk> loadChunk(ChunkPosition chunkPosition, Consumer<Chunk> onLoadConsumer){
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

    public static int getSize(){
        return pendingChunks.size();
    }

    public static void stop(){
        if(chunksLoaderId != null)
            chunksLoaderId.cancel();
    }

    public static void init(){
        chunksLoaderId = runChunksLoader();
    }

    private static BukkitTask runChunksLoader(){
        boolean asyncLoading = PaperHook.isUsingPaper() && ServerVersion.isAtLeast(ServerVersion.v1_13);

        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for(int i = 0; i < plugin.getSettings().chunksPerTick; i++) {
                double[] tps = plugin.getNMSAdapter().getTPS();
                double averageTPS = (tps[0] + tps[1] + tps[2]) / 3;

                if(tps[0] < averageTPS * 0.8)
                    return;

                ChunkPosition chunkPosition = pendingChunks.poll();

                if (chunkPosition == null)
                    return;

                if(asyncLoading){
                    PaperLib.getChunkAtAsync(chunkPosition.getWorld(), chunkPosition.getX(), chunkPosition.getZ(), true)
                            .whenComplete((chunk, ex) -> finishLoad(chunkPosition, chunk));
                }

                else {
                    Chunk chunk = chunkPosition.loadChunk();
                    finishLoad(chunkPosition, chunk);
                }
            }
        }, 1L, 1L);
    }

    private static void finishLoad(ChunkPosition chunkPosition, Chunk chunk){
        Pair<CompletableFuture<Chunk>, Set<Consumer<Chunk>>> chunkInfo = chunksInfo.remove(chunkPosition);
        if(chunkInfo != null) {
            chunkInfo.getValue().forEach(chunkConsumer -> chunkConsumer.accept(chunk));
            chunkInfo.getKey().complete(chunk);
        }
    }

}
