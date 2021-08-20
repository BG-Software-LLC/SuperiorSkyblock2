package com.bgsoftware.superiorskyblock.nms.v1_15_R1;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.google.common.base.Suppliers;
import net.minecraft.server.v1_15_R1.Chunk;
import net.minecraft.server.v1_15_R1.ChunkConverter;
import net.minecraft.server.v1_15_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_15_R1.ChunkRegionLoader;
import net.minecraft.server.v1_15_R1.IChunkAccess;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.Packet;
import net.minecraft.server.v1_15_R1.PlayerChunk;
import net.minecraft.server.v1_15_R1.PlayerChunkMap;
import net.minecraft.server.v1_15_R1.ProtoChunk;
import net.minecraft.server.v1_15_R1.World;
import net.minecraft.server.v1_15_R1.WorldServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class NMSUtils {

    private static final ReflectField<Map<Long, PlayerChunk>> VISIBLE_CHUNKS = new ReflectField<>(PlayerChunkMap.class, Map.class, "visibleChunks");

    private NMSUtils() {

    }

    public static void runActionOnChunks(WorldServer worldServer, Collection<ChunkCoordIntPair> chunksCoords,
                                         boolean saveChunks, Runnable onFinish, Consumer<Chunk> chunkConsumer,
                                         BiConsumer<ChunkCoordIntPair, NBTTagCompound> unloadedChunkConsumer) {
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

    public static void runActionOnUnloadedChunks(WorldServer worldServer,
                                                 Collection<ChunkCoordIntPair> chunks,
                                                 boolean saveChunks,
                                                 BiConsumer<ChunkCoordIntPair, NBTTagCompound> chunkConsumer,
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
                        chunkCompound = playerChunkMap.getChunkData(worldServer.getWorldProvider().getDimensionManager(),
                                Suppliers.ofInstance(worldServer.getWorldPersistentData()), chunkCompound, chunkCoords, worldServer);
                    }

                    if (chunkCompound.hasKeyOfType("Level", 10)) {
                        chunkConsumer.accept(chunkCoords, chunkCompound.getCompound("Level"));
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

    public static ProtoChunk createProtoChunk(ChunkCoordIntPair chunkCoord, World world){
        try{
            // Paper's constructor for ProtoChunk
            return new ProtoChunk(chunkCoord, ChunkConverter.a, world);
        }catch (Throwable ex){
            // Spigot's constructor for ProtoChunk
            // noinspection deprecation
            return new ProtoChunk(chunkCoord, ChunkConverter.a);
        }
    }

    public static void sendPacketToRelevantPlayers(WorldServer worldServer, int chunkX, int chunkZ, Packet<?> packet){
        PlayerChunkMap playerChunkMap = worldServer.getChunkProvider().playerChunkMap;
        ChunkCoordIntPair chunkCoordIntPair = new ChunkCoordIntPair(chunkX, chunkZ);
        PlayerChunk playerChunk;

        try {
            playerChunk = playerChunkMap.getVisibleChunk(chunkCoordIntPair.pair());
        }catch (Throwable ex){
            playerChunk = VISIBLE_CHUNKS.get(playerChunkMap).get(chunkCoordIntPair.pair());
        }

        if(playerChunk != null)
            playerChunk.sendPacketToTrackedPlayers(packet, false);
    }

}
