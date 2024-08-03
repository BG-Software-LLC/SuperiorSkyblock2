package com.bgsoftware.superiorskyblock.nms.v1_12_R1;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
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
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NMSUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ReflectField<IChunkLoader> CHUNK_LOADER = new ReflectField<>(
            ChunkProviderServer.class, IChunkLoader.class, "chunkLoader");
    private static final ReflectMethod<Void> SAVE_CHUNK = new ReflectMethod<>(
            IChunkLoader.class, "a", World.class, Chunk.class);
    private static final ReflectMethod<Void> TILE_ENTITY_LOAD = new ReflectMethod<>(
            TileEntity.class, "a", NBTTagCompound.class);

    private static final Map<UUID, IChunkLoader> chunkLoadersMap = Maps.newHashMap();

    private static final List<CompletableFuture<Void>> PENDING_CHUNK_ACTIONS = new LinkedList<>();

    private NMSUtils() {

    }

    public static void runActionOnEntityChunks(Collection<ChunkPosition> chunksCoords,
                                               ChunkCallback chunkCallback) {
        runActionOnChunksInternal(chunksCoords, chunkCallback, unloadedChunks ->
                runActionOnUnloadedEntityChunks(unloadedChunks, chunkCallback));
    }

    public static void runActionOnChunks(Collection<ChunkPosition> chunksCoords,
                                         boolean saveChunks, ChunkCallback chunkCallback) {
        runActionOnChunksInternal(chunksCoords, chunkCallback, unloadedChunks ->
                runActionOnUnloadedChunks(unloadedChunks, saveChunks, chunkCallback));
    }

    private static void runActionOnChunksInternal(Collection<ChunkPosition> chunksCoords,
                                                  ChunkCallback chunkCallback,
                                                  Consumer<List<ChunkPosition>> onUnloadChunkAction) {
        List<ChunkPosition> unloadedChunks = new LinkedList<>();
        List<Chunk> loadedChunks = new LinkedList<>();

        chunksCoords.forEach(chunkPosition -> {
            WorldServer worldServer = ((CraftWorld) chunkPosition.getWorld()).getHandle();

            Chunk chunk = worldServer.getChunkIfLoaded(chunkPosition.getX(), chunkPosition.getZ());

            if (chunk != null) {
                loadedChunks.add(chunk);
            } else {
                unloadedChunks.add(chunkPosition);
            }
        });

        boolean hasUnloadedChunks = !unloadedChunks.isEmpty();

        loadedChunks.forEach(loadedChunk -> {
            chunkCallback.onChunk(loadedChunk, true);
            chunkCallback.onUpdateChunk(loadedChunk);
        });

        if (hasUnloadedChunks) {
            onUnloadChunkAction.accept(unloadedChunks);
        } else {
            chunkCallback.onFinish();
        }
    }

    public static void runActionOnUnloadedChunks(Collection<ChunkPosition> chunks,
                                                 boolean saveChunks, ChunkCallback chunkCallback) {
        CompletableFuture<Void> pendingTask = new CompletableFuture<>();
        PENDING_CHUNK_ACTIONS.add(pendingTask);

        BukkitExecutor.createTask().runAsync(v -> {
            chunks.forEach(chunkPosition -> {
                WorldServer worldServer = ((CraftWorld) chunkPosition.getWorld()).getHandle();

                IChunkLoader chunkLoader = chunkLoadersMap.computeIfAbsent(worldServer.getDataManager().getUUID(),
                        uuid -> CHUNK_LOADER.get(worldServer.getChunkProvider()));

                if (!chunkLoader.chunkExists(chunkPosition.getX(), chunkPosition.getZ()))
                    return;

                try {
                    Chunk loadedChunk = chunkLoader.a(worldServer, chunkPosition.getX(), chunkPosition.getZ());

                    if (loadedChunk != null) {
                        chunkCallback.onChunk(loadedChunk, false);

                        if (saveChunks) {
                            if (SAVE_CHUNK.isValid())
                                SAVE_CHUNK.invoke(chunkLoader, worldServer, loadedChunk);
                            else {
                                chunkLoader.saveChunk(worldServer, loadedChunk, false);
                            }
                        }
                    }
                } catch (Exception error) {
                    Log.error(error, "An unexpected error occurred while interacting with unloaded chunk ", chunkPosition, ":");
                }
            });
        }).runSync(v -> {
            chunkCallback.onFinish();

            pendingTask.complete(null);
            PENDING_CHUNK_ACTIONS.remove(pendingTask);
        });
    }

    private static void runActionOnUnloadedEntityChunks(Collection<ChunkPosition> chunks,
                                                        ChunkCallback chunkCallback) {

    }

    public static List<CompletableFuture<Void>> getPendingChunkActions() {
        return Collections.unmodifiableList(PENDING_CHUNK_ACTIONS);
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

    private static boolean isValidPosition(World world, BlockPosition blockPosition) {
        return blockPosition.getX() >= -30000000 && blockPosition.getZ() >= -30000000 &&
                blockPosition.getX() < 30000000 && blockPosition.getZ() < 30000000 &&
                blockPosition.getY() >= 0 && blockPosition.getY() < world.getHeight();
    }

    public interface ChunkCallback {

        void onChunk(Chunk chunk, boolean isLoaded);

        void onUpdateChunk(Chunk chunk);

        void onFinish();

    }

}
