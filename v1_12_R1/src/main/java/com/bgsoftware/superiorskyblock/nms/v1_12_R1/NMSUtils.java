package com.bgsoftware.superiorskyblock.nms.v1_12_R1;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.google.common.collect.Maps;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_12_R1.ChunkProviderServer;
import net.minecraft.server.v1_12_R1.ChunkSection;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.IChunkLoader;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PlayerChunkMap;
import net.minecraft.server.v1_12_R1.TileEntity;
import net.minecraft.server.v1_12_R1.World;
import net.minecraft.server.v1_12_R1.WorldServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class NMSUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ReflectField<IChunkLoader> CHUNK_LOADER = new ReflectField<>(
            ChunkProviderServer.class, IChunkLoader.class, "chunkLoader");
    private static final ReflectMethod<Void> SAVE_CHUNK = new ReflectMethod<>(
            IChunkLoader.class, "a", World.class, Chunk.class);
    private static final ReflectMethod<Void> TILE_ENTITY_LOAD = new ReflectMethod<>(
            TileEntity.class, "a", NBTTagCompound.class);

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

        if (updateChunk != null)
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
            chunks.forEach(chunkCoords -> {
                try {
                    Chunk loadedChunk = chunkLoader.a(worldServer, chunkCoords.x, chunkCoords.z);

                    if (loadedChunk != null) {
                        chunkConsumer.accept(loadedChunk);

                        if (saveChunks) {
                            if (SAVE_CHUNK.isValid())
                                SAVE_CHUNK.invoke(chunkLoader, worldServer, loadedChunk);
                            else {
                                chunkLoader.saveChunk(worldServer, loadedChunk, false);
                            }
                        }
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

    public static void sendPacketToRelevantPlayers(WorldServer worldServer, int chunkX, int chunkZ, Packet<?> packet) {
        PlayerChunkMap playerChunkMap = worldServer.getPlayerChunkMap();
        for (EntityHuman entityHuman : worldServer.players) {
            if (entityHuman instanceof EntityPlayer && playerChunkMap.a((EntityPlayer) entityHuman, chunkX, chunkZ))
                ((EntityPlayer) entityHuman).playerConnection.sendPacket(packet);
        }
    }

    public static void setBlock(Chunk chunk, BlockPosition blockPosition, int combinedId, CompoundTag tileEntity) {
        IBlockData blockData = Block.getByCombinedId(combinedId);

        if (blockData.getMaterial().isLiquid() && plugin.getSettings().isLiquidUpdate()) {
            chunk.world.setTypeAndData(blockPosition, blockData, 3);
            return;
        }

        int blockX = blockPosition.getX() & 15;
        int blockY = blockPosition.getY();
        int blockZ = blockPosition.getZ() & 15;

        int highestBlockLight = chunk.b(blockX, blockZ);
        boolean initLight = false;

        int indexY = blockY >> 4;

        ChunkSection chunkSection = chunk.getSections()[indexY];

        if (chunkSection == null) {
            chunkSection = chunk.getSections()[indexY] = new ChunkSection(indexY << 4, chunk.world.worldProvider.m());
            initLight = blockY > highestBlockLight;
        }

        chunkSection.setType(blockX, blockY & 15, blockZ, blockData);

        chunk.markDirty();

        if (initLight)
            chunk.initLighting();

        if (tileEntity != null) {
            NBTTagCompound tileEntityCompound = (NBTTagCompound) tileEntity.toNBT();
            if (tileEntityCompound != null) {
                tileEntityCompound.setInt("x", blockPosition.getX());
                tileEntityCompound.setInt("y", blockPosition.getY());
                tileEntityCompound.setInt("z", blockPosition.getZ());
                TileEntity worldTileEntity = chunk.world.getTileEntity(blockPosition);
                if (worldTileEntity != null) {
                    if (TILE_ENTITY_LOAD.isValid())
                        TILE_ENTITY_LOAD.invoke(worldTileEntity, tileEntityCompound);
                    else
                        worldTileEntity.load(tileEntityCompound);
                }
            }
        }
    }

}
