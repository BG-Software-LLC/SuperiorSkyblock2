package com.bgsoftware.superiorskyblock.nms.v1_17_R1;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.nms.v1_17_R1.world.BlockStatesMapper;
import com.bgsoftware.superiorskyblock.tag.ByteTag;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.IntArrayTag;
import com.bgsoftware.superiorskyblock.tag.StringTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.google.common.base.Suppliers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.SectionPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.PlayerChunk;
import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockBed;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkConverter;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.HeightMap;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NMSUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ReflectField<Map<Long, PlayerChunk>> VISIBLE_CHUNKS = new ReflectField<>(
            PlayerChunkMap.class, Map.class, "visibleChunks", "l");
    private static final ReflectMethod<Void> SEND_PACKETS_TO_RELEVANT_PLAYERS = new ReflectMethod<>(
            PlayerChunk.class, "a", Packet.class, boolean.class);

    private NMSUtils() {

    }

    public static void runActionOnChunks(WorldServer worldServer, Collection<ChunkCoordIntPair> chunksCoords,
                                         boolean saveChunks, Runnable onFinish, Consumer<Chunk> chunkConsumer,
                                         BiConsumer<ChunkCoordIntPair, NBTTagCompound> unloadedChunkConsumer) {
        List<ChunkCoordIntPair> unloadedChunks = new LinkedList<>();
        List<Chunk> loadedChunks = new LinkedList<>();

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

    public static void runActionOnUnloadedChunks(WorldServer worldServer,
                                                 Collection<ChunkCoordIntPair> chunks,
                                                 boolean saveChunks,
                                                 BiConsumer<ChunkCoordIntPair, NBTTagCompound> chunkConsumer,
                                                 Runnable onFinish) {
        PlayerChunkMap playerChunkMap = worldServer.getChunkProvider().a;

        BukkitExecutor.createTask().runAsync(v -> {
            chunks.forEach(chunkCoords -> {
                try {
                    NBTTagCompound chunkCompound = playerChunkMap.read(chunkCoords);

                    if (chunkCompound == null)
                        return;

                    NBTTagCompound chunkDataCompound = playerChunkMap.getChunkData(worldServer.getTypeKey(),
                            Suppliers.ofInstance(worldServer.getWorldPersistentData()), chunkCompound, chunkCoords, worldServer);

                    if (chunkDataCompound.hasKeyOfType("Level", 10)) {
                        chunkConsumer.accept(chunkCoords, chunkDataCompound.getCompound("Level"));
                        if (saveChunks)
                            playerChunkMap.a(chunkCoords, chunkDataCompound);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    PluginDebugger.debug(ex);
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

    public static void sendPacketToRelevantPlayers(WorldServer worldServer, int chunkX, int chunkZ, Packet<?> packet) {
        PlayerChunkMap playerChunkMap = worldServer.getChunkProvider().a;
        ChunkCoordIntPair chunkCoordIntPair = new ChunkCoordIntPair(chunkX, chunkZ);
        PlayerChunk playerChunk;

        try {
            playerChunk = playerChunkMap.getVisibleChunk(chunkCoordIntPair.pair());
        } catch (Throwable ex) {
            playerChunk = VISIBLE_CHUNKS.get(playerChunkMap).get(chunkCoordIntPair.pair());
        }

        if (playerChunk != null) {
            try {
                playerChunk.a(packet, false);
            } catch (Throwable ex) {
                SEND_PACKETS_TO_RELEVANT_PLAYERS.invoke(playerChunk, packet, false);
            }
        }
    }

    public static void setBlock(net.minecraft.world.level.chunk.Chunk chunk, BlockPosition blockPosition,
                                int combinedId, CompoundTag statesTag, CompoundTag tileEntity) {
        if (!isValidPosition(chunk.getWorld(), blockPosition))
            return;

        IBlockData blockData = net.minecraft.world.level.block.Block.getByCombinedId(combinedId);

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
                } catch (Exception error) {
                    PluginDebugger.debug(error);
                }
            }
        }

        if ((blockData.getMaterial().isLiquid() && plugin.getSettings().isLiquidUpdate()) ||
                blockData.getBlock() instanceof BlockBed) {
            chunk.getWorld().setTypeAndData(blockPosition, blockData, 3);
            return;
        }

        int indexY = chunk.getSectionIndex(blockPosition.getY());

        ChunkSection chunkSection = chunk.getSections()[indexY];

        if (chunkSection == null) {
            int yOffset = SectionPosition.a(blockPosition.getY());
            try {
                // Paper's constructor for ChunkSection for more optimized chunk sections.
                chunkSection = chunk.getSections()[indexY] = new ChunkSection(yOffset, chunk, chunk.getWorld(), true);
            } catch (Throwable ex) {
                // Spigot's constructor for ChunkSection
                // noinspection deprecation
                chunkSection = chunk.getSections()[indexY] = new ChunkSection(yOffset);
            }
        }

        int blockX = blockPosition.getX() & 15;
        int blockY = blockPosition.getY();
        int blockZ = blockPosition.getZ() & 15;

        boolean isOriginallyChunkSectionEmpty = chunkSection.c();

        chunkSection.setType(blockX, blockY & 15, blockZ, blockData, false);

        chunk.j.get(HeightMap.Type.e).a(blockX, blockY, blockZ, blockData);
        chunk.j.get(HeightMap.Type.f).a(blockX, blockY, blockZ, blockData);
        chunk.j.get(HeightMap.Type.d).a(blockX, blockY, blockZ, blockData);
        chunk.j.get(HeightMap.Type.b).a(blockX, blockY, blockZ, blockData);

        chunk.markDirty();
        chunk.setNeedsSaving(true);

        boolean isChunkSectionEmpty = chunkSection.c();

        if (isOriginallyChunkSectionEmpty != isChunkSectionEmpty)
            chunk.getWorld().k_().a(blockPosition, isChunkSectionEmpty);

        chunk.getWorld().k_().a(blockPosition);

        if (tileEntity != null) {
            NBTTagCompound tileEntityCompound = (NBTTagCompound) tileEntity.toNBT();
            if (tileEntityCompound != null) {
                tileEntityCompound.setInt("x", blockPosition.getX());
                tileEntityCompound.setInt("y", blockPosition.getY());
                tileEntityCompound.setInt("z", blockPosition.getZ());
                TileEntity worldTileEntity = chunk.getWorld().getTileEntity(blockPosition);
                if (worldTileEntity != null)
                    worldTileEntity.load(tileEntityCompound);
            }
        }
    }

    private static boolean isValidPosition(World world, BlockPosition blockPosition) {
        return blockPosition.getX() >= -30000000 && blockPosition.getZ() >= -30000000 &&
                blockPosition.getX() < 30000000 && blockPosition.getZ() < 30000000 &&
                blockPosition.getY() >= world.getMinBuildHeight() && blockPosition.getY() < world.getMaxBuildHeight();
    }

}
