package com.bgsoftware.superiorskyblock.nms.v1_13_R1;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.google.common.collect.Maps;
import net.minecraft.server.v1_13_R1.Chunk;
import net.minecraft.server.v1_13_R1.ChunkConverter;
import net.minecraft.server.v1_13_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_13_R1.ChunkProviderServer;
import net.minecraft.server.v1_13_R1.EntityHuman;
import net.minecraft.server.v1_13_R1.EntityPlayer;
import net.minecraft.server.v1_13_R1.IChunkAccess;
import net.minecraft.server.v1_13_R1.IChunkLoader;
import net.minecraft.server.v1_13_R1.Packet;
import net.minecraft.server.v1_13_R1.PlayerChunkMap;
import net.minecraft.server.v1_13_R1.ProtoChunk;
import net.minecraft.server.v1_13_R1.ProtoChunkExtension;
import net.minecraft.server.v1_13_R1.WorldServer;

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
        IChunkLoader chunkLoader = chunkLoadersMap.computeIfAbsent(worldServer.getDataManager().getUUID(),
                uuid -> CHUNK_LOADER.get(worldServer.getChunkProvider()));

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

    public static void sendPacketToRelevantPlayers(WorldServer worldServer, int chunkX, int chunkZ, Packet<?> packet) {
        PlayerChunkMap playerChunkMap = worldServer.getPlayerChunkMap();
        for (EntityHuman entityHuman : worldServer.players) {
            if (entityHuman instanceof EntityPlayer && playerChunkMap.a((EntityPlayer) entityHuman, chunkX, chunkZ))
                ((EntityPlayer) entityHuman).playerConnection.sendPacket(packet);
        }
    }

}
