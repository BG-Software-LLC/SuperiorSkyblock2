package com.bgsoftware.superiorskyblock.world.chunks;

import com.bgsoftware.common.executors.IWorker;
import com.bgsoftware.common.executors.WorkerExecutor;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import org.bukkit.Chunk;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class ChunksProvider {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final WorkerExecutor chunksExecutor = new WorkerExecutor(10);

    private static final Map<ChunkPosition, PendingChunkLoadRequest> pendingRequests = new ConcurrentHashMap<>();

    private ChunksProvider() {

    }

    public static CompletableFuture<Chunk> loadChunk(ChunkPosition chunkPosition, ChunkLoadReason chunkLoadReason, Consumer<Chunk> onLoadConsumer) {
        PluginDebugger.debug("Action: Chunk Load Attempt, Chunk: " + chunkPosition.toString() + ", Reason: " + chunkLoadReason);

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

            pendingRequests.put(chunkPosition, new PendingChunkLoadRequest(completableFuture, chunkConsumers, chunkLoadReason));
            chunksExecutor.addWorker(new ChunkLoadWorker(chunkPosition, chunkLoadReason));

            if (!chunksExecutor.isRunning())
                start();

            return completableFuture;
        }
    }

    public static void stop() {
        if (chunksExecutor.isRunning())
            chunksExecutor.stop();
    }

    public static void start() {
        chunksExecutor.start(plugin);
    }

    private static final class ChunkLoadWorker implements IWorker {

        private final ChunkPosition chunkPosition;
        private final ChunkLoadReason chunkLoadReason;

        public ChunkLoadWorker(ChunkPosition chunkPosition, ChunkLoadReason chunkLoadReason) {
            this.chunkPosition = chunkPosition;
            this.chunkLoadReason = chunkLoadReason;
        }

        @Override
        public void work() {
            PluginDebugger.debug("Action: Chunk Load, Chunk: " + chunkPosition.toString() + ", Reason: " + chunkLoadReason);
            plugin.getProviders().getChunksProvider().loadChunk(chunkPosition.getWorld(),
                    chunkPosition.getX(), chunkPosition.getZ()).whenComplete((chunk, error) -> {
                if (error != null) {
                    error.printStackTrace();
                }

                try {
                    finishLoad(chunk);
                } catch (Exception ex) {
                    SuperiorSkyblockPlugin.log("&cAn unexpected error occurred while loading chunk " + chunkPosition + ":");
                    ex.printStackTrace();
                }
            });
        }

        private void finishLoad(Chunk chunk) {
            PendingChunkLoadRequest pendingRequest = pendingRequests.remove(chunkPosition);


            PluginDebugger.debug("Action: Chunk Load Finish, Chunk: " + chunkPosition.toString() +
                    (pendingRequest == null ? "" : ", Reason: " + pendingRequest.chunkLoadReason));


            if (pendingRequest != null) {
                pendingRequest.callbacks.forEach(chunkConsumer -> chunkConsumer.accept(chunk));
                pendingRequest.completableFuture.complete(chunk);
            }
        }

    }

    private static final class PendingChunkLoadRequest {

        private final CompletableFuture<Chunk> completableFuture;
        private final Set<Consumer<Chunk>> callbacks;
        private final ChunkLoadReason chunkLoadReason;

        public PendingChunkLoadRequest(CompletableFuture<Chunk> completableFuture, Set<Consumer<Chunk>> callbacks,
                                       ChunkLoadReason chunkLoadReason) {
            this.completableFuture = completableFuture;
            this.callbacks = callbacks;
            this.chunkLoadReason = chunkLoadReason;
        }

    }

}
