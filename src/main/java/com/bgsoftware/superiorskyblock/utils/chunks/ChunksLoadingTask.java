package com.bgsoftware.superiorskyblock.utils.chunks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.hooks.PaperHook;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class ChunksLoadingTask {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ConcurrentLinkedQueue<Pair<ChunksLoadMethod, CompletableFuture<List<Chunk>>>>
            chunksToLoad = new ConcurrentLinkedQueue<>();

    private static int chunksLoaderId;

    static {
        chunksLoaderId = runChunksLoader();
    }

    public static CompletableFuture<List<Chunk>> loadIslandChunks(Island island, World.Environment environment, boolean onlyProtected){
        CompletableFuture<List<Chunk>> completableFuture = new CompletableFuture<>();
        chunksToLoad.add(new Pair<>(new ChunksLoadMethod(island, environment, onlyProtected), completableFuture));
        return completableFuture;
    }

    public static void stop(){
        Bukkit.getScheduler().cancelTask(chunksLoaderId);
    }

    private static int runChunksLoader(){
        Runnable loadChunks = () -> {
            Pair<ChunksLoadMethod, CompletableFuture<List<Chunk>>> pair = chunksToLoad.poll();

            if(pair == null)
                return;

            ChunksLoadMethod method = pair.getKey();

            pair.getValue().complete(method.island.getAllChunks(method.environment, method.onlyProtected));
        };

        if(PaperHook.isUsingPaper() && ServerVersion.isAtLeast(ServerVersion.v1_13)) {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, loadChunks, 5L, 5L).getTaskId();
        }

        else{
            return Bukkit.getScheduler().runTaskTimer(plugin, loadChunks, 5L, 5L).getTaskId();
        }
    }

    private final static class ChunksLoadMethod{

        private final boolean onlyProtected;
        private final World.Environment environment;
        private final Island island;

        public ChunksLoadMethod(Island island, World.Environment environment, boolean onlyProtected){
            this.island = island;
            this.environment = environment;
            this.onlyProtected = onlyProtected;
        }

    }

}
