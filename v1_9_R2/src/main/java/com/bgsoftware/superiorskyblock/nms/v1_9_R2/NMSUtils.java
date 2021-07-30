package com.bgsoftware.superiorskyblock.nms.v1_9_R2;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.google.common.collect.Maps;
import net.minecraft.server.v1_9_R2.Chunk;
import net.minecraft.server.v1_9_R2.ChunkCoordIntPair;
import net.minecraft.server.v1_9_R2.ChunkProviderServer;
import net.minecraft.server.v1_9_R2.IChunkLoader;
import net.minecraft.server.v1_9_R2.WorldServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class NMSUtils {

    private static final ReflectField<IChunkLoader> CHUNK_LOADER = new ReflectField<>(ChunkProviderServer.class, IChunkLoader.class, "chunkLoader");

    private static final Map<UUID, IChunkLoader> chunkLoadersMap = Maps.newHashMap();

    private NMSUtils() {

    }

    public static void runActionOnChunks(WorldServer worldServer, Collection<ChunkCoordIntPair> chunksCoords,
                                         boolean saveChunks, Runnable onFinish, Consumer<Chunk> chunkConsumer,
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
                                                 boolean saveChunks, Consumer<Chunk> chunkConsumer,
                                                 Runnable onFinish) {
        IChunkLoader chunkLoader = chunkLoadersMap.computeIfAbsent(worldServer.getDataManager().getUUID(),
                uuid -> CHUNK_LOADER.get(worldServer.getChunkProvider()));

        Executor.createTask().runAsync(v -> {
            List<Chunk> loadedChunks = new ArrayList<>();

            chunks.forEach(chunkCoords -> {
                try {
                    Chunk loadedChunk = chunkLoader.a(worldServer, chunkCoords.x, chunkCoords.z);

                    if (loadedChunk != null) {
                        chunkConsumer.accept(loadedChunk);
                        loadedChunks.add(loadedChunk);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            return loadedChunks;
        }).runSync(loadedChunks -> {
            loadedChunks.forEach(loadedChunk -> {
                if (loadedChunk != null) {
                    if (saveChunks) {
                        try {
                            chunkLoader.a(worldServer, loadedChunk);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
            if (onFinish != null)
                onFinish.run();
        });
    }

}
