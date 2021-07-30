package com.bgsoftware.superiorskyblock.nms.v1_13_R2;

import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import net.minecraft.server.v1_13_R2.Chunk;
import net.minecraft.server.v1_13_R2.ChunkConverter;
import net.minecraft.server.v1_13_R2.ChunkCoordIntPair;
import net.minecraft.server.v1_13_R2.IChunkAccess;
import net.minecraft.server.v1_13_R2.IChunkLoader;
import net.minecraft.server.v1_13_R2.ProtoChunk;
import net.minecraft.server.v1_13_R2.ProtoChunkExtension;
import net.minecraft.server.v1_13_R2.WorldServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public final class NMSUtils {

    private NMSUtils() {

    }

    public static void runActionOnChunks(WorldServer worldServer, Collection<ChunkCoordIntPair> chunksCoords,
                                         boolean saveChunks, Runnable onFinish, Consumer<IChunkAccess> chunkConsumer,
                                         Consumer<Chunk> updateChunk) {
        List<ChunkCoordIntPair> unloadedChunks = new ArrayList<>();
        List<Chunk> loadedChunks = new ArrayList<>();

        chunksCoords.forEach(chunkCoords -> {
            Chunk chunk = worldServer.getChunkIfLoaded(chunkCoords.x, chunkCoords.z);

            if (chunk != null) {
                loadedChunks.add(chunk);
            } else {
                unloadedChunks.add(chunkCoords);
            }
        });

        boolean hasUnloadedChunks = !unloadedChunks.isEmpty();

        loadedChunks.forEach(chunkConsumer);

        if(updateChunk != null)
            loadedChunks.forEach(updateChunk);

        if (hasUnloadedChunks) {
            runActionOnUnloadedChunks(worldServer, unloadedChunks, saveChunks, chunkConsumer, onFinish);
        } else if (onFinish != null) {
            onFinish.run();
        }
    }

    public static void runActionOnUnloadedChunks(WorldServer worldServer, Collection<ChunkCoordIntPair> chunks,
                                                 boolean saveChunks, Consumer<IChunkAccess> chunkConsumer,
                                                 Runnable onFinish) {
        IChunkLoader chunkLoader = worldServer.getChunkProvider().chunkLoader;

        Executor.createTask().runAsync(v -> {
            chunks.forEach(chunkCoords -> {
                try {
                    ProtoChunk protoChunk = chunkLoader.b(worldServer, chunkCoords.x, chunkCoords.z, chunkAccess -> {
                    });

                    if (protoChunk == null || protoChunk instanceof ProtoChunkExtension)
                        protoChunk = new ProtoChunk(chunkCoords, ChunkConverter.a);

                    chunkConsumer.accept(protoChunk);
                    if (saveChunks)
                        chunkLoader.saveChunk(worldServer, protoChunk);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }).runSync(v -> {
            if (onFinish != null)
                onFinish.run();
        });
    }

}
