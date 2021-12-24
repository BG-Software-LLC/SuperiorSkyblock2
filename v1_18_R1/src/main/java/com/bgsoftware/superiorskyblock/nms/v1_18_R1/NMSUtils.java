package com.bgsoftware.superiorskyblock.nms.v1_18_R1;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.ChunkCoordIntPair;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.SectionPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.PlayerChunkMap;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.block.Block;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.block.entity.TileEntity;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.block.state.BlockData;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.block.state.properties.BlockState;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.chunk.ChunkAccess;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.chunk.ChunkSection;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.nbt.NBTTagCompound;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.nbt.NBTTagList;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.world.BlockStatesMapper;
import com.bgsoftware.superiorskyblock.tag.ByteTag;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.IntArrayTag;
import com.bgsoftware.superiorskyblock.tag.StringTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.google.common.base.Suppliers;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.PlayerChunk;
import net.minecraft.world.level.block.BlockBed;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkConverter;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.HeightMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class NMSUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ReflectMethod<Void> SEND_PACKETS_TO_RELEVANT_PLAYERS = new ReflectMethod<>(
            PlayerChunk.class, 1, Packet.class, boolean.class);

    private NMSUtils() {

    }

    public static void runActionOnChunks(WorldServer worldServer, Collection<ChunkCoordIntPair> chunksCoords,
                                         boolean saveChunks, Runnable onFinish, Consumer<ChunkAccess> chunkConsumer,
                                         Consumer<UnloadedChunkCompound> unloadedChunkConsumer) {
        List<ChunkCoordIntPair> unloadedChunks = new ArrayList<>();
        List<ChunkAccess> loadedChunks = new ArrayList<>();

        chunksCoords.forEach(chunkCoords -> {
            ChunkAccess chunkAccess = worldServer.getChunkIfLoaded(chunkCoords.getX(), chunkCoords.getZ());
            if (chunkAccess != null && chunkAccess.getHandle() instanceof Chunk) {
                loadedChunks.add(chunkAccess);
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

    public static void runActionOnLoadedChunks(Collection<ChunkAccess> chunks, Consumer<ChunkAccess> chunkConsumer) {
        chunks.forEach(chunkConsumer);
    }

    public static void runActionOnUnloadedChunks(WorldServer worldServer,
                                                 Collection<ChunkCoordIntPair> chunks,
                                                 boolean saveChunks,
                                                 Consumer<UnloadedChunkCompound> chunkConsumer,
                                                 Runnable onFinish) {
        PlayerChunkMap playerChunkMap = worldServer.getChunkProvider().getPlayerChunkMap();

        Executor.createTask().runAsync(v -> {
            List<Pair<ChunkCoordIntPair, NBTTagCompound>> chunkCompounds = new ArrayList<>();

            chunks.forEach(chunkCoords -> {
                try {
                    NBTTagCompound chunkCompound = playerChunkMap.read(chunkCoords);

                    if (chunkCompound == null) {
                        ChunkAccess protoChunk = createProtoChunk(chunkCoords, worldServer);
                        chunkCompound = worldServer.saveChunk(protoChunk);
                    } else {
                        chunkCompound = playerChunkMap.getChunkData(worldServer.getTypeKey(),
                                Suppliers.ofInstance(worldServer.getWorldPersistentData()), chunkCompound,
                                chunkCoords, worldServer.getHandle());
                    }

                    UnloadedChunkCompound unloadedChunkCompound = new UnloadedChunkCompound(chunkCompound, chunkCoords);
                    chunkConsumer.accept(unloadedChunkCompound);

                    if (saveChunks)
                        chunkCompounds.add(new Pair<>(chunkCoords, chunkCompound));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    PluginDebugger.debug(ex);
                }
            });

            return chunkCompounds;
        }).runSync(chunkCompounds -> {
            chunkCompounds.forEach(chunkCompoundPair -> {
                try {
                    playerChunkMap.saveChunk(chunkCompoundPair.getKey(), chunkCompoundPair.getValue());
                } catch (IOException error) {
                    error.printStackTrace();
                    PluginDebugger.debug(error);
                }
            });

            if (onFinish != null)
                onFinish.run();
        });
    }

    public static ChunkAccess createProtoChunk(ChunkCoordIntPair chunkCoords, WorldServer worldServer) {
        return new ChunkAccess(new ProtoChunk(chunkCoords.getHandle(),
                ChunkConverter.a,
                worldServer.getHandle(),
                worldServer.getBiomeRegistry(),
                null));
    }

    public static void sendPacketToRelevantPlayers(WorldServer worldServer, int chunkX, int chunkZ, Packet<?> packet) {
        PlayerChunkMap playerChunkMap = worldServer.getChunkProvider().getPlayerChunkMap();
        ChunkCoordIntPair chunkCoordIntPair = new ChunkCoordIntPair(chunkX, chunkZ);
        PlayerChunk playerChunk = playerChunkMap.getPlayerChunk(chunkCoordIntPair.pair());
        if (playerChunk != null) {
            SEND_PACKETS_TO_RELEVANT_PLAYERS.invoke(playerChunk, packet, false);
        }
    }

    public static void setBlock(ChunkAccess chunk, BlockPosition blockPosition,
                                int combinedId, CompoundTag statesTag, CompoundTag tileEntity) {
        BlockData blockData = Block.getByCombinedId(combinedId);

        if (statesTag != null) {
            for (Map.Entry<String, Tag<?>> entry : statesTag.getValue().entrySet()) {
                try {
                    // noinspection rawtypes
                    BlockState blockState = BlockStatesMapper.getBlockState(entry.getKey());
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

        WorldServer worldServer = chunk.getWorld();

        if ((blockData.getMaterial().isLiquid() && plugin.getSettings().isLiquidUpdate()) ||
                blockData.getBlock().getHandle() instanceof BlockBed) {
            worldServer.setTypeAndData(blockPosition, blockData, 3);
            return;
        }

        if (plugin.getSettings().isLightsUpdate()) {
            chunk.setType(blockPosition, blockData, true, true);
        } else {
            int indexY = worldServer.getSectionIndex(blockPosition.getY());

            ChunkSection chunkSection = ChunkSection.ofNullable(chunk.getSections()[indexY]);

            if (chunkSection == null) {
                int yOffset = SectionPosition.getSectionCoord(blockPosition.getY());
                //noinspection deprecation
                chunk.getSections()[indexY] = new net.minecraft.world.level.chunk.ChunkSection(
                        yOffset, chunk.getBiomeRegistry());
                chunkSection = new ChunkSection(chunk.getSections()[indexY]);
            }

            int blockX = blockPosition.getX() & 15;
            int blockY = blockPosition.getY();
            int blockZ = blockPosition.getZ() & 15;

            chunkSection.setType(blockX, blockY & 15, blockZ, blockData, false);

            chunk.getHeightmap(HeightMap.Type.e).setBlock(blockX, blockY, blockZ, blockData);
            chunk.getHeightmap(HeightMap.Type.f).setBlock(blockX, blockY, blockZ, blockData);
            chunk.getHeightmap(HeightMap.Type.d).setBlock(blockX, blockY, blockZ, blockData);
            chunk.getHeightmap(HeightMap.Type.b).setBlock(blockX, blockY, blockZ, blockData);

            chunk.setNeedsSaving(true);
        }

        if (tileEntity != null) {
            NBTTagCompound tileEntityCompound = NBTTagCompound.ofNullable((net.minecraft.nbt.NBTTagCompound) tileEntity.toNBT());
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

    public record UnloadedChunkCompound(NBTTagCompound chunkCompound, ChunkCoordIntPair chunkCoords) {

        public NBTTagList getSections() {
            return new NBTTagList(chunkCompound.getList("sections", 10));
        }

        public void setSections(NBTTagList sectionsList) {
            chunkCompound.set("sections", sectionsList.getHandle());
        }

        public void setEntities(NBTTagList entitiesList) {
            chunkCompound.set("entities", entitiesList.getHandle());
        }

        public void setBlockEntities(NBTTagList blockEntitiesList) {
            chunkCompound.set("block_entities", blockEntitiesList.getHandle());
        }

        public ChunkCoordIntPair getChunkCoords() {
            return chunkCoords;
        }

    }

}
