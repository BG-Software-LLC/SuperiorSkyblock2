package com.bgsoftware.superiorskyblock.utils.chunks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.hooks.PaperHook;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.pair.BiPair;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public final class ChunksProvider {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ConcurrentLinkedQueue<BiPair<ChunkPosition, CompletableFuture<Chunk>, Consumer<Chunk>>>
            pendingChunks = new ConcurrentLinkedQueue<>();

    private static BukkitTask chunksLoaderId;

    public static CompletableFuture<Chunk> loadChunk(World world, int x, int z, Consumer<Chunk> onLoadConsumer){
        CompletableFuture<Chunk> completableFuture = new CompletableFuture<>();
        pendingChunks.add(new BiPair<>(ChunkPosition.of(world, x, z), completableFuture, onLoadConsumer));
        return completableFuture;
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
                BiPair<ChunkPosition, CompletableFuture<Chunk>, Consumer<Chunk>> pair = pendingChunks.poll();

                if (pair == null)
                    return;

                Chunk chunk = pair.getX().loadChunk();

                if(!Bukkit.isPrimaryThread()){
                    Executor.sync(() -> finishLoad(pair, chunk));
                }
                else{
                    finishLoad(pair, chunk);
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

    private static void finishLoad(BiPair<ChunkPosition, CompletableFuture<Chunk>, Consumer<Chunk>> pair, Chunk chunk){
        if(pair.getZ() != null)
            pair.getZ().accept(chunk);

        pair.getY().complete(chunk);
    }

}
