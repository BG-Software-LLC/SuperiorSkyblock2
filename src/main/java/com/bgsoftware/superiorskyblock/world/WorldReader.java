package com.bgsoftware.superiorskyblock.world;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.collections.CompletableFutureList;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.mutable.MutableBoolean;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.nms.world.ChunkReader;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksProvider;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class WorldReader {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final World world;
    private final ChunkLoadReason chunkLoadReason;
    private final CompletableFutureList<ChunkReader> chunkReaderFutures = new CompletableFutureList<>(-1L);

    private final Map<ChunkPosition, ChunkReader> cachedChunkReaders = new HashMap<>();
    private boolean finishCalled = false;
    private boolean inFinishCallback = false;

    public WorldReader(World world, ChunkLoadReason chunkLoadReason) {
        this.world = world;
        this.chunkLoadReason = chunkLoadReason;
    }

    public void prepareChunk(ChunkPosition chunkPosition) {
        if (this.finishCalled)
            throw new IllegalStateException("Cannot call WorldReader#prepareChunk after WorldReader#finish was called");

        chunkReaderFutures.add(ChunksProvider.loadChunk(chunkPosition, this.chunkLoadReason, null)
                .thenApply(chunk -> plugin.getNMSWorld().createChunkReader(chunk)));
    }

    @Nullable
    public ChunkReader getChunkReader(int blockX, int blockZ) {
        if (!this.inFinishCallback)
            throw new IllegalStateException("Cannot call WorldReader#getChunkReader from this state");

        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        try (ChunkPosition chunkPosition = ChunkPosition.of(this.world, chunkX, chunkZ)) {
            return this.cachedChunkReaders.get(chunkPosition);
        }
    }

    public void finish(Runnable onFinish) {
        this.finishCalled = true;

        BukkitExecutor.async(() -> {
            MutableBoolean failed = new MutableBoolean(false);

            chunkReaderFutures.forEachCompleted(chunkReader -> {
                ChunkPosition chunkPosition = ChunkPosition.of(world, chunkReader.getX(), chunkReader.getZ(), false);
                cachedChunkReaders.put(chunkPosition, chunkReader);
            }, error -> {
                failed.set(true);
                Log.error(error, "An error occurred while waiting for chunks to load:");
            });

            if (!failed.get()) {
                try {
                    this.inFinishCallback = true;
                    onFinish.run();
                } finally {
                    this.inFinishCallback = true;
                }
            }
        });
    }

}
