package com.bgsoftware.superiorskyblock.utils.chunks;

import com.bgsoftware.common.executors.IWorker;
import com.bgsoftware.common.executors.WorkerExecutor;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
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

    private static final Map<ChunkPosition, Pair<CompletableFuture<Chunk>, Set<Consumer<Chunk>>>>
            chunksInfo = new ConcurrentHashMap<>();

    private ChunksProvider() {

    }

    public static CompletableFuture<Chunk> loadChunk(ChunkPosition chunkPosition, Consumer<Chunk> onLoadConsumer) {
        SuperiorSkyblockPlugin.debug("Action: Chunk Load Attempt, Chunk: " + chunkPosition.toString());

        Pair<CompletableFuture<Chunk>, Set<Consumer<Chunk>>> chunkInfo = chunksInfo.get(chunkPosition);

        if (chunkInfo != null) {
            if (onLoadConsumer != null)
                chunkInfo.getValue().add(onLoadConsumer);
            return chunkInfo.getKey();
        } else {
            CompletableFuture<Chunk> completableFuture = new CompletableFuture<>();
            Set<Consumer<Chunk>> chunkConsumers = new HashSet<>();

            if (onLoadConsumer != null)
                chunkConsumers.add(onLoadConsumer);

            chunksInfo.put(chunkPosition, new Pair<>(completableFuture, chunkConsumers));
            chunksExecutor.addWorker(new ChunkLoadWorker(chunkPosition));

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

        public ChunkLoadWorker(ChunkPosition chunkPosition) {
            this.chunkPosition = chunkPosition;
        }

        @Override
        public void work() {
            SuperiorSkyblockPlugin.debug("Action: Chunk Load, Chunk: " + chunkPosition.toString());
            plugin.getProviders().loadChunk(chunkPosition, this::finishLoad);
        }

        private void finishLoad(Chunk chunk) {
            SuperiorSkyblockPlugin.debug("Action: Chunk Load Finish, Chunk: " + chunkPosition.toString());

            Pair<CompletableFuture<Chunk>, Set<Consumer<Chunk>>> chunkInfo = chunksInfo.remove(chunkPosition);

            if (chunkInfo != null) {
                chunkInfo.getValue().forEach(chunkConsumer -> chunkConsumer.accept(chunk));
                chunkInfo.getKey().complete(chunk);
            }
        }

    }

}
