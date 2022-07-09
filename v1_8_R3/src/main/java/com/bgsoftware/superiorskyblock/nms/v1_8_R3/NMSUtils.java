package com.bgsoftware.superiorskyblock.nms.v1_8_R3;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.google.common.collect.Maps;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R3.ChunkProviderServer;
import net.minecraft.server.v1_8_R3.ChunkRegionLoader;
import net.minecraft.server.v1_8_R3.ChunkSection;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.IChunkLoader;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PlayerChunkMap;
import net.minecraft.server.v1_8_R3.TileEntity;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.WorldServer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NMSUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ReflectField<IChunkLoader> CHUNK_LOADER = new ReflectField<>(
            ChunkProviderServer.class, IChunkLoader.class, "chunkLoader");

    private static final Map<UUID, IChunkLoader> chunkLoadersMap = Maps.newHashMap();

    private NMSUtils() {

    }

    public static void runActionOnChunks(WorldServer worldServer, Collection<ChunkCoordIntPair> chunksCoords,
                                         boolean saveChunks, Runnable onFinish, BiConsumer<Chunk, Boolean> chunkConsumer,
                                         Consumer<Chunk> updateChunk) {
        List<ChunkCoordIntPair> unloadedChunks = new LinkedList<>();
        List<Chunk> loadedChunks = new LinkedList<>();

        chunksCoords.forEach(chunkCoords -> {
            Chunk chunk = worldServer.getChunkIfLoaded(chunkCoords.x, chunkCoords.z);

            if (chunk != null) {
                loadedChunks.add(chunk);
            } else {
                unloadedChunks.add(chunkCoords);
            }
        });

        boolean hasUnloadedChunks = !unloadedChunks.isEmpty();

        loadedChunks.forEach(loadedChunk -> chunkConsumer.accept(loadedChunk, true));

        if (updateChunk != null)
            loadedChunks.forEach(updateChunk);

        if (hasUnloadedChunks) {
            runActionOnUnloadedChunks(worldServer, unloadedChunks, saveChunks, chunkConsumer, onFinish);
        } else if (onFinish != null) {
            onFinish.run();
        }
    }

    public static void runActionOnUnloadedChunks(WorldServer worldServer, Collection<ChunkCoordIntPair> chunks,
                                                 boolean saveChunks, BiConsumer<Chunk, Boolean> chunkConsumer,
                                                 Runnable onFinish) {
        IChunkLoader chunkLoader = chunkLoadersMap.computeIfAbsent(worldServer.getDataManager().getUUID(),
                uuid -> CHUNK_LOADER.get(worldServer.chunkProviderServer));

        chunks.forEach(chunkCoords -> {
            if (chunkLoader instanceof ChunkRegionLoader &&
                    !((ChunkRegionLoader) chunkLoader).chunkExists(worldServer, chunkCoords.x, chunkCoords.z))
                return;

            try {
                Chunk loadedChunk = chunkLoader.a(worldServer, chunkCoords.x, chunkCoords.z);

                if (loadedChunk != null)
                    chunkConsumer.accept(loadedChunk, false);

                if (loadedChunk != null) {
                    if (saveChunks) {
                        try {
                            chunkLoader.a(worldServer, loadedChunk);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            PluginDebugger.debug(ex);
                        }
                    }
                }

                if (onFinish != null)
                    onFinish.run();
            } catch (Exception ex) {
                ex.printStackTrace();
                PluginDebugger.debug(ex);
            }
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
        if (!isValidPosition(chunk.world, blockPosition))
            return;

        IBlockData blockData = Block.getByCombinedId(combinedId);

        if (blockData.getBlock().getMaterial().isLiquid() && plugin.getSettings().isLiquidUpdate()) {
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
            chunkSection = chunk.getSections()[indexY] = new ChunkSection(indexY << 4, !chunk.world.worldProvider.o());
            initLight = blockY > highestBlockLight;
        }

        chunkSection.setType(blockX, blockY & 15, blockZ, blockData);

        chunk.e();

        if (initLight)
            chunk.initLighting();

        if (tileEntity != null) {
            NBTTagCompound tileEntityCompound = (NBTTagCompound) tileEntity.toNBT();
            if (tileEntityCompound != null) {
                tileEntityCompound.setInt("x", blockPosition.getX());
                tileEntityCompound.setInt("y", blockPosition.getY());
                tileEntityCompound.setInt("z", blockPosition.getZ());
                TileEntity worldTileEntity = chunk.world.getTileEntity(blockPosition);
                if (worldTileEntity != null)
                    worldTileEntity.a(tileEntityCompound);
            }
        }
    }

    private static boolean isValidPosition(World world, BlockPosition blockPosition) {
        return blockPosition.getX() >= -30000000 && blockPosition.getZ() >= -30000000 &&
                blockPosition.getX() < 30000000 && blockPosition.getZ() < 30000000 &&
                blockPosition.getY() >= 0 && blockPosition.getY() < world.getHeight();
    }

}
