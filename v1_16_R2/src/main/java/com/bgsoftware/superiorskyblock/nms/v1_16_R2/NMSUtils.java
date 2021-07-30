package com.bgsoftware.superiorskyblock.nms.v1_16_R2;

import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.google.common.base.Suppliers;
import net.minecraft.server.v1_16_R2.Chunk;
import net.minecraft.server.v1_16_R2.ChunkConverter;
import net.minecraft.server.v1_16_R2.ChunkCoordIntPair;
import net.minecraft.server.v1_16_R2.ChunkRegionLoader;
import net.minecraft.server.v1_16_R2.IChunkAccess;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import net.minecraft.server.v1_16_R2.PlayerChunkMap;
import net.minecraft.server.v1_16_R2.ProtoChunk;
import net.minecraft.server.v1_16_R2.WorldServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public final class NMSUtils {

    private NMSUtils() {

    }

    public static void runActionOnChunks(WorldServer worldServer, Collection<ChunkCoordIntPair> chunksCoords,
                                         boolean saveChunks, Runnable onFinish, Consumer<Chunk> chunkConsumer,
                                         Consumer<NBTTagCompound> unloadedChunkConsumer) {
        List<ChunkCoordIntPair> unloadedChunks = new ArrayList<>();
        List<Chunk> loadedChunks = new ArrayList<>();

        chunksCoords.forEach(chunkCoords -> {
            IChunkAccess chunkAccess;

            try {
                chunkAccess = worldServer.getChunkIfLoadedImmediately(chunkCoords.x, chunkCoords.z);
            } catch (Throwable ex) {
                chunkAccess = worldServer.getChunkIfLoaded(chunkCoords.x, chunkCoords.z);
            }

            if (chunkAccess instanceof Chunk) {
                loadedChunks.add((Chunk) chunkAccess);
            } else {
                unloadedChunks.add(chunkCoords);
            }
        });

        boolean hasUnloadedChunks = !unloadedChunks.isEmpty();

        if (!loadedChunks.isEmpty())
            runActionOnLoadedChunks(loadedChunks, chunkConsumer);

        if (hasUnloadedChunks) {
            runActionOnUnloadedChunks(worldServer, unloadedChunks, saveChunks, unloadedChunkConsumer, onFinish);
        } else if (onFinish != null) {
            onFinish.run();
        }
    }

    public static void runActionOnLoadedChunks(Collection<Chunk> chunks, Consumer<Chunk> chunkConsumer) {
        chunks.forEach(chunkConsumer);
    }

    public static void runActionOnUnloadedChunks(WorldServer worldServer, Collection<ChunkCoordIntPair> chunks,
                                                 boolean saveChunks, Consumer<NBTTagCompound> chunkConsumer,
                                                 Runnable onFinish) {
        PlayerChunkMap playerChunkMap = worldServer.getChunkProvider().playerChunkMap;

        Executor.createTask().runAsync(v -> {
            chunks.forEach(chunkCoords -> {
                try {
                    NBTTagCompound chunkCompound = playerChunkMap.read(chunkCoords);

                    if (chunkCompound == null) {
                        ProtoChunk protoChunk = new ProtoChunk(chunkCoords, ChunkConverter.a, worldServer);
                        chunkCompound = ChunkRegionLoader.saveChunk(worldServer, protoChunk);
                    } else {
                        chunkCompound = playerChunkMap.getChunkData(worldServer.getTypeKey(),
                                Suppliers.ofInstance(worldServer.getWorldPersistentData()), chunkCompound, chunkCoords, worldServer);
                    }

                    if (chunkCompound.hasKeyOfType("Level", 10)) {
                        chunkConsumer.accept(chunkCompound.getCompound("Level"));
                        if (saveChunks)
                            playerChunkMap.a(chunkCoords, chunkCompound);
                    }
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
