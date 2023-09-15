package com.bgsoftware.superiorskyblock.world.chunk;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.executors.IWorker;
import com.bgsoftware.common.executors.WorkerExecutor;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.profiler.ProfileType;
import com.bgsoftware.superiorskyblock.core.profiler.Profiler;
import org.bukkit.Chunk;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ChunksProvider {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final WorkerExecutor chunksExecutor = new WorkerExecutor(10);

    private static final Map<ChunkPosition, PendingChunkLoadRequest> pendingRequests = new ConcurrentHashMap<>();

    private static boolean stopped = false;

    private ChunksProvider() {

    }

    public static CompletableFuture<Chunk> loadChunk(ChunkPosition chunkPosition, ChunkLoadReason chunkLoadReason,
                                                     @Nullable Consumer<Chunk> onLoadConsumer) {
        if (stopped)
            return new CompletableFuture<>();

        Log.debug(Debug.LOAD_CHUNK, chunkPosition, chunkLoadReason);

        Chunk loadedChunk = ChunkPosition.getLoadedChunk(chunkPosition).orElse(null);
        if (loadedChunk != null) {
            processPendingChunkLoadRequest(loadedChunk, chunkPosition);
            if (onLoadConsumer != null)
                onLoadConsumer.accept(loadedChunk);
            return CompletableFuture.completedFuture(loadedChunk);
        }

        PendingChunkLoadRequest pendingRequest = pendingRequests.get(chunkPosition);

        if (pendingRequest != null) {
            if (onLoadConsumer != null)
                pendingRequest.callbacks.add(onLoadConsumer);
            return pendingRequest.completableFuture;
        } else {
            CompletableFuture<Chunk> completableFuture = new CompletableFuture<>();
            Set<Consumer<Chunk>> chunkConsumers = new HashSet<>();

            if (onLoadConsumer != null)
                chunkConsumers.add(onLoadConsumer);

            pendingRequests.put(chunkPosition, new PendingChunkLoadRequest(completableFuture, chunkConsumers));
            chunksExecutor.addWorker(new ChunkLoadWorker(chunkPosition, chunkLoadReason));

            if (!chunksExecutor.isRunning())
                start();

            return completableFuture;
        }
    }

    public static void stop() {
        stopped = true;
        if (chunksExecutor.isRunning())
            chunksExecutor.stop();
    }

    public static void start() {
        chunksExecutor.start(plugin);
    }

    private static class ChunkLoadWorker implements IWorker {

        private final ChunkPosition chunkPosition;
        private final ChunkLoadReason chunkLoadReason;

        public ChunkLoadWorker(ChunkPosition chunkPosition, ChunkLoadReason chunkLoadReason) {
            this.chunkPosition = chunkPosition;
            this.chunkLoadReason = chunkLoadReason;
        }

        @Override
        public void work() {
            if (stopped)
                return;

            long profiler = Profiler.start(ProfileType.LOAD_CHUNK);
            Log.debug(Debug.LOAD_CHUNK, chunkPosition, chunkLoadReason);

            plugin.getProviders().getChunksProvider().loadChunk(chunkPosition.getWorld(),
                    chunkPosition.getX(), chunkPosition.getZ()).whenComplete((chunk, error) -> {
                if (error != null) {
                    Log.entering("ENTER", chunkPosition, chunkLoadReason);
                    Log.error(error, "An unexpected error occurred while loading chunk:");
                    error.printStackTrace();
                }

                try {
                    finishLoad(chunk, profiler);
                } catch (Exception error2) {
                    Log.entering("ENTER", chunkPosition, chunkLoadReason);
                    Log.error(error2, "An unexpected error occurred while finishing chunk loading:");
                }
            });
        }

        private void finishLoad(Chunk chunk, long profiler) {
            Profiler.end(profiler);
            Log.debug(Debug.LOAD_CHUNK, chunkPosition, chunkLoadReason);
            processPendingChunkLoadRequest(chunk, chunkPosition);
        }

    }

    private static void processPendingChunkLoadRequest(Chunk chunk, ChunkPosition chunkPosition) {
        PendingChunkLoadRequest pendingRequest = pendingRequests.remove(chunkPosition);

        if (pendingRequest == null)
            return;

        pendingRequest.callbacks.forEach(chunkConsumer -> chunkConsumer.accept(chunk));
        pendingRequest.completableFuture.complete(chunk);
    }

    private static class PendingChunkLoadRequest {

        private final CompletableFuture<Chunk> completableFuture;
        private final Set<Consumer<Chunk>> callbacks;

        public PendingChunkLoadRequest(CompletableFuture<Chunk> completableFuture, Set<Consumer<Chunk>> callbacks) {
            this.completableFuture = completableFuture;
            this.callbacks = callbacks;
        }

    }

}
