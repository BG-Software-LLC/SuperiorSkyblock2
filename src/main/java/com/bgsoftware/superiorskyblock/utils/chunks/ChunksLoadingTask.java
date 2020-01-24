package com.bgsoftware.superiorskyblock.utils.chunks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.hooks.PaperHook;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class ChunksLoadingTask {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ConcurrentLinkedQueue<Pair<ChunkPosition, CompletableFuture<Chunk>>>
            chunksToLoad = new ConcurrentLinkedQueue<>();

    private static int chunksLoaderId;

    static {
        chunksLoaderId = runChunksLoader();
    }

    public static CompletableFuture<Chunk> loadChunk(World world, int x, int z){
        CompletableFuture<Chunk> completableFuture = new CompletableFuture<>();
        chunksToLoad.add(new Pair<>(ChunkPosition.of(world, x, z), completableFuture));
        return completableFuture;
    }

    public static void stop(){
        Bukkit.getScheduler().cancelTask(chunksLoaderId);
    }

    private static int runChunksLoader(){
        Runnable loadChunks = () -> {
            for(int i = 0; i < plugin.getSettings().chunksPerTick; i++) {
                Pair<ChunkPosition, CompletableFuture<Chunk>> pair = chunksToLoad.poll();

                if(pair == null)
                    break;

                pair.getValue().complete(pair.getKey().loadChunk());
            }
        };

        if(PaperHook.isUsingPaper() && ServerVersion.isAtLeast(ServerVersion.v1_13)) {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, loadChunks, 1L, 1L).getTaskId();
        }

        else{
            return Bukkit.getScheduler().runTaskTimer(plugin, loadChunks, 1L, 1L).getTaskId();
        }
    }

}
