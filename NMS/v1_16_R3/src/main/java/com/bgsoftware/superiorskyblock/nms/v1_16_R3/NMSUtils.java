package com.bgsoftware.superiorskyblock.nms.v1_16_R3;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.world.BlockStatesMapper;
import com.bgsoftware.superiorskyblock.tag.ByteTag;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.IntArrayTag;
import com.bgsoftware.superiorskyblock.tag.StringTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.google.common.base.Suppliers;
import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.BlockBed;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Chunk;
import net.minecraft.server.v1_16_R3.ChunkConverter;
import net.minecraft.server.v1_16_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_16_R3.ChunkProviderServer;
import net.minecraft.server.v1_16_R3.ChunkSection;
import net.minecraft.server.v1_16_R3.HeightMap;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.IBlockState;
import net.minecraft.server.v1_16_R3.IChunkAccess;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PlayerChunk;
import net.minecraft.server.v1_16_R3.PlayerChunkMap;
import net.minecraft.server.v1_16_R3.ProtoChunk;
import net.minecraft.server.v1_16_R3.TileEntity;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class NMSUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ReflectField<Map<Long, PlayerChunk>> VISIBLE_CHUNKS = new ReflectField<>(
            PlayerChunkMap.class, Map.class, "visibleChunks");
    private static final ReflectMethod<Void> SEND_PACKETS_TO_RELEVANT_PLAYERS = new ReflectMethod<>(
            PlayerChunk.class, "a", Packet.class, boolean.class);
    private static final ReflectMethod<Chunk> CHUNK_PROVIDER_SERVER_GET_CHUNK_IF_CACHED = new ReflectMethod<>(
            ChunkProviderServer.class, "getChunkAtIfCachedImmediately", int.class, int.class);

    private static final List<CompletableFuture<Void>> PENDING_CHUNK_ACTIONS = new LinkedList<>();

    private NMSUtils() {

    }

    public static void runActionOnChunks(Collection<ChunkPosition> chunksCoords,
                                         boolean saveChunks, ChunkCallback chunkCallback) {
        runActionOnChunksInternal(chunksCoords, saveChunks, chunkCallback);
    }

    private static void runActionOnChunksInternal(Collection<ChunkPosition> chunksCoords,
                                                  boolean saveChunks, ChunkCallback chunkCallback) {
        List<ChunkPosition> unloadedChunks = new LinkedList<>();
        List<Chunk> loadedChunks = new LinkedList<>();

        chunksCoords.forEach(chunkPosition -> {
            WorldServer worldServer = ((CraftWorld) chunkPosition.getWorld()).getHandle();

            IChunkAccess chunkAccess;

            try {
                chunkAccess = worldServer.getChunkIfLoadedImmediately(chunkPosition.getX(), chunkPosition.getZ());
            } catch (Throwable ex) {
                chunkAccess = worldServer.getChunkIfLoaded(chunkPosition.getX(), chunkPosition.getZ());
            }

            if (chunkAccess instanceof Chunk) {
                loadedChunks.add((Chunk) chunkAccess);
            } else {
                unloadedChunks.add(chunkPosition);
            }
        });

        boolean hasUnloadedChunks = !unloadedChunks.isEmpty();

        if (!loadedChunks.isEmpty())
            runActionOnLoadedChunks(loadedChunks, chunkCallback);

        if (hasUnloadedChunks) {
            runActionOnUnloadedChunks(unloadedChunks, saveChunks, chunkCallback);
        } else {
            chunkCallback.onFinish();
        }
    }

    private static void runActionOnLoadedChunks(Collection<Chunk> chunks, ChunkCallback chunkCallback) {
        chunks.forEach(chunkCallback::onLoadedChunk);
    }

    private static void runActionOnUnloadedChunks(Collection<ChunkPosition> chunks,
                                                  boolean saveChunks, ChunkCallback chunkCallback) {
        if (CHUNK_PROVIDER_SERVER_GET_CHUNK_IF_CACHED.isValid()) {
            Iterator<ChunkPosition> chunksIterator = chunks.iterator();
            while (chunksIterator.hasNext()) {
                ChunkPosition chunkPosition = chunksIterator.next();

                WorldServer worldServer = ((CraftWorld) chunkPosition.getWorld()).getHandle();

                Chunk cachedUnloadedChunk = worldServer.getChunkProvider().getChunkAtIfCachedImmediately(
                        chunkPosition.getX(), chunkPosition.getZ());
                if (cachedUnloadedChunk != null) {
                    chunkCallback.onLoadedChunk(cachedUnloadedChunk);
                    chunksIterator.remove();
                }
            }

            if (chunks.isEmpty()) {
                chunkCallback.onFinish();
                return;
            }
        }

        CompletableFuture<Void> pendingTask = new CompletableFuture<>();
        PENDING_CHUNK_ACTIONS.add(pendingTask);

        BukkitExecutor.createTask().runAsync(v -> {
            chunks.forEach(chunkPosition -> {
                WorldServer worldServer = ((CraftWorld) chunkPosition.getWorld()).getHandle();
                PlayerChunkMap playerChunkMap = worldServer.getChunkProvider().playerChunkMap;

                ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ());

                try {
                    NBTTagCompound chunkCompound = playerChunkMap.read(chunkCoords);

                    if (chunkCompound == null)
                        return;

                    NBTTagCompound chunkDataCompound = playerChunkMap.getChunkData(worldServer.getTypeKey(),
                            Suppliers.ofInstance(worldServer.getWorldPersistentData()), chunkCompound, chunkCoords, worldServer);

                    if (chunkDataCompound.hasKeyOfType("Level", 10)) {
                        chunkCallback.onUnloadedChunk(chunkPosition, chunkDataCompound.getCompound("Level"));
                        if (saveChunks)
                            playerChunkMap.a(chunkCoords, chunkDataCompound);
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

    public static List<CompletableFuture<Void>> getPendingChunkActions() {
        return Collections.unmodifiableList(PENDING_CHUNK_ACTIONS);
    }

    public static ProtoChunk createProtoChunk(ChunkCoordIntPair chunkCoord, World world) {
        try {
            // Paper's constructor for ProtoChunk
            return new ProtoChunk(chunkCoord, ChunkConverter.a, world);
        } catch (Throwable ex) {
            // Spigot's constructor for ProtoChunk
            // noinspection deprecation
            return new ProtoChunk(chunkCoord, ChunkConverter.a);
        }
    }

    public static void sendPacketToRelevantPlayers(WorldServer worldServer, int chunkX, int chunkZ, Packet<?> packet) {
        PlayerChunkMap playerChunkMap = worldServer.getChunkProvider().playerChunkMap;
        ChunkCoordIntPair chunkCoordIntPair = new ChunkCoordIntPair(chunkX, chunkZ);
        PlayerChunk playerChunk;

        try {
            playerChunk = playerChunkMap.getVisibleChunk(chunkCoordIntPair.pair());
        } catch (Throwable ex) {
            playerChunk = VISIBLE_CHUNKS.get(playerChunkMap).get(chunkCoordIntPair.pair());
        }

        if (playerChunk != null) {
            try {
                playerChunk.sendPacketToTrackedPlayers(packet, false);
            } catch (Throwable ex) {
                SEND_PACKETS_TO_RELEVANT_PLAYERS.invoke(playerChunk, packet, false);
            }
        }
    }

    public static void setBlock(Chunk chunk, BlockPosition blockPosition, int combinedId, CompoundTag statesTag,
                                CompoundTag tileEntity) {
        if (!isValidPosition(chunk.getWorld(), blockPosition))
            return;

        IBlockData blockData = Block.getByCombinedId(combinedId);

        if (statesTag != null) {
            for (Map.Entry<String, Tag<?>> entry : statesTag.entrySet()) {
                try {
                    // noinspection rawtypes
                    IBlockState blockState = BlockStatesMapper.getBlockState(entry.getKey());
                    if (blockState != null) {
                        if (entry.getValue() instanceof ByteTag) {
                            // noinspection unchecked
                            blockData = blockData.set(blockState, ((ByteTag) entry.getValue()).getValue() == 1);
                        } else if (entry.getValue() instanceof IntArrayTag) {
                            int[] data = ((IntArrayTag) entry.getValue()).getValue();
                            // noinspection unchecked
                            blockData = blockData.set(blockState, data[0]);
                        } else if (entry.getValue() instanceof StringTag) {
                            String data = ((StringTag) entry.getValue()).getValue();
                            // noinspection unchecked
                            blockData = blockData.set(blockState, Enum.valueOf(blockState.getType(), data));
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

        if ((blockData.getMaterial().isLiquid() && plugin.getSettings().isLiquidUpdate()) || blockData.getBlock() instanceof BlockBed) {
            chunk.world.setTypeAndData(blockPosition, blockData, 3);
            return;
        }

        int indexY = blockPosition.getY() >> 4;

        ChunkSection chunkSection = chunk.getSections()[indexY];

        if (chunkSection == null) {
            try {
                // Paper's constructor for ChunkSection for more optimized chunk sections.
                chunkSection = chunk.getSections()[indexY] = new ChunkSection(indexY << 4, chunk, chunk.world, true);
            } catch (Throwable ex) {
                // Spigot's constructor for ChunkSection
                // noinspection deprecation
                chunkSection = chunk.getSections()[indexY] = new ChunkSection(indexY << 4);
            }
        }

        int blockX = blockPosition.getX() & 15;
        int blockY = blockPosition.getY();
        int blockZ = blockPosition.getZ() & 15;

        boolean isOriginallyChunkSectionEmpty = chunkSection.c();

        chunkSection.setType(blockX, blockY & 15, blockZ, blockData, false);

        chunk.heightMap.get(HeightMap.Type.MOTION_BLOCKING).a(blockX, blockY, blockZ, blockData);
        chunk.heightMap.get(HeightMap.Type.MOTION_BLOCKING_NO_LEAVES).a(blockX, blockY, blockZ, blockData);
        chunk.heightMap.get(HeightMap.Type.OCEAN_FLOOR).a(blockX, blockY, blockZ, blockData);
        chunk.heightMap.get(HeightMap.Type.WORLD_SURFACE).a(blockX, blockY, blockZ, blockData);

        chunk.markDirty();
        chunk.setNeedsSaving(true);

        boolean isChunkSectionEmpty = chunkSection.c();

        if (isOriginallyChunkSectionEmpty != isChunkSectionEmpty)
            chunk.getWorld().e().a(blockPosition, isChunkSectionEmpty);

        chunk.getWorld().e().a(blockPosition);

        if (tileEntity != null) {
            NBTTagCompound tileEntityCompound = (NBTTagCompound) tileEntity.toNBT();
            if (tileEntityCompound != null) {
                tileEntityCompound.setInt("x", blockPosition.getX());
                tileEntityCompound.setInt("y", blockPosition.getY());
                tileEntityCompound.setInt("z", blockPosition.getZ());
                TileEntity worldTileEntity = chunk.world.getTileEntity(blockPosition);
                if (worldTileEntity != null)
                    worldTileEntity.load(blockData, tileEntityCompound);
            }
        }
    }

    private static boolean isValidPosition(World world, BlockPosition blockPosition) {
        return blockPosition.getX() >= -30000000 && blockPosition.getZ() >= -30000000 &&
                blockPosition.getX() < 30000000 && blockPosition.getZ() < 30000000 &&
                blockPosition.getY() >= 0 && blockPosition.getY() < world.getHeight();
    }

    public interface ChunkCallback {

        void onLoadedChunk(Chunk chunk);

        void onUnloadedChunk(ChunkPosition chunkPosition, NBTTagCompound unloadedChunk);

        void onFinish();

    }

}
