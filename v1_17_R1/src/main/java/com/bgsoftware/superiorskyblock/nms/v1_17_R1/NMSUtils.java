package com.bgsoftware.superiorskyblock.nms.v1_17_R1;

import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.google.common.base.Suppliers;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkConverter;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.ChunkRegionLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public final class NMSUtils {

    private NMSUtils() {

    }

    public static void runActionOnChunks(WorldServer worldServer, Collection<ChunkCoordIntPair> chunksCoords, boolean saveChunks, Runnable onFinish, Consumer<Chunk> chunkConsumer, Consumer<NBTTagCompound> unloadedChunkConsumer) {
        List<ChunkCoordIntPair> unloadedChunks = new ArrayList<>();
        List<Chunk> loadedChunks = new ArrayList<>();

        chunksCoords.forEach(chunkCoords -> {
            IChunkAccess chunkAccess;

            try {
                chunkAccess = worldServer.getChunkIfLoadedImmediately(chunkCoords.b, chunkCoords.c);
            } catch (Throwable ex) {
                chunkAccess = worldServer.getChunkIfLoaded(chunkCoords.b, chunkCoords.c);
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

    public static void runActionOnUnloadedChunks(WorldServer worldServer, Collection<ChunkCoordIntPair> chunks, boolean saveChunks, Consumer<NBTTagCompound> chunkConsumer, Runnable onFinish) {
        PlayerChunkMap playerChunkMap = worldServer.getChunkProvider().a;

        Executor.createTask().runAsync(v -> {
            chunks.forEach(chunkCoords -> {
                try {
                    NBTTagCompound chunkCompound = playerChunkMap.read(chunkCoords);

                    if (chunkCompound == null) {
                        ProtoChunk protoChunk = createProtoChunk(chunkCoords, worldServer);
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

    public static ProtoChunk createProtoChunk(ChunkCoordIntPair chunkCoords, WorldServer worldServer) {
        try {
            return new ProtoChunk(chunkCoords, ChunkConverter.a, worldServer, worldServer);
        } catch (Throwable ex) {
            //noinspection deprecation
            return new ProtoChunk(chunkCoords, ChunkConverter.a, worldServer);
        }
    }

}
