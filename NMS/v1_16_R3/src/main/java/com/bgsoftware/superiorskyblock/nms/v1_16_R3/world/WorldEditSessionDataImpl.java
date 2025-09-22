package com.bgsoftware.superiorskyblock.nms.v1_16_R3.world;

import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.collections.view.Long2ObjectMapView;
import com.bgsoftware.superiorskyblock.nms.world.WorldEditSession;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_16_R3.ChunkSection;
import net.minecraft.server.v1_16_R3.HeightMap;
import net.minecraft.server.v1_16_R3.IBlockData;
import org.bukkit.Location;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class WorldEditSessionDataImpl implements WorldEditSession.Data {

    private static final boolean isStarLightInterface = ((Supplier<Boolean>) () -> {
        try {
            Class.forName("ca.spottedleaf.moonrise.patches.starlight.light.StarLightInterface");
            return true;
        } catch (ClassNotFoundException error) {
            return false;
        }
    }).get();

    private final List<PositionedObject<PositionedChunkData>> chunks = new LinkedList<>();
    private final List<PositionedObject<IBlockData>> blocksToUpdate = new LinkedList<>();
    private final List<PositionedObject<CompoundTag>> blockEntities = new LinkedList<>();
    private final List<PositionedObject<Void>> lightenChunks = isStarLightInterface ? new LinkedList<>() : Collections.emptyList();

    public WorldEditSessionDataImpl(Location baseLocation, Long2ObjectMapView<WorldEditSessionImpl.ChunkData> chunks,
                                    List<Pair<BlockPosition, IBlockData>> blocksToUpdate,
                                    List<Pair<BlockPosition, CompoundTag>> blockEntities,
                                    Set<ChunkCoordIntPair> lightenChunks) {
        int baseBlockPosXAxis = baseLocation.getBlockX();
        int baseBlockPosYAxis = baseLocation.getBlockY();
        int baseBlockPosZAxis = baseLocation.getBlockZ();
        int baseChunkPosXAxis = baseBlockPosXAxis >> 4;
        int baseChunkPosZAxis = baseBlockPosZAxis >> 4;

        // Convert chunk data
        Iterator<Long2ObjectMapView.Entry<WorldEditSessionImpl.ChunkData>> chunksIterator = chunks.entryIterator();
        while (chunksIterator.hasNext()) {
            Long2ObjectMapView.Entry<WorldEditSessionImpl.ChunkData> entry = chunksIterator.next();
            int currChunkPosXAxis = ChunkCoordIntPair.getX(entry.getKey());
            int currChunkPosZAxis = ChunkCoordIntPair.getZ(entry.getKey());
            WorldEditSessionImpl.ChunkData chunkData = entry.getValue();

            List<PositionedObject<Void>> lights;
            if (isStarLightInterface || chunkData.lights().isEmpty()) {
                lights = Collections.emptyList();
            } else {
                lights = new LinkedList<>();
                chunkData.lights().forEach(lightPosition -> {
                    lights.add(new PositionedObject<>(lightPosition.getX() - baseBlockPosXAxis,
                            lightPosition.getY() - baseBlockPosYAxis, lightPosition.getZ() - baseBlockPosZAxis,
                            null));
                });
            }

            PositionedChunkData positionedChunkData = new PositionedChunkData(chunkData.chunkSections(),
                    chunkData.heightmaps(), lights);

            this.chunks.add(new PositionedObject<>(currChunkPosXAxis - baseChunkPosXAxis,
                    currChunkPosZAxis - baseChunkPosZAxis, positionedChunkData));
        }

        // Convert blocksToUpdate
        blocksToUpdate.forEach(blockToUpdate -> {
            BlockPosition blockPos = blockToUpdate.getKey();
            this.blocksToUpdate.add(new PositionedObject<>(blockPos.getX() - baseBlockPosXAxis,
                    blockPos.getY() - baseBlockPosYAxis, blockPos.getZ() - baseBlockPosZAxis,
                    blockToUpdate.getValue()));
        });

        // Convert blockEntities
        blockEntities.forEach(blockEntity -> {
            BlockPosition blockPos = blockEntity.getKey();
            this.blockEntities.add(new PositionedObject<>(blockPos.getX() - baseBlockPosXAxis,
                    blockPos.getY() - baseBlockPosYAxis, blockPos.getZ() - baseBlockPosZAxis,
                    blockEntity.getValue()));
        });

        // Convert lights
        lightenChunks.forEach(lightPosition -> {
            this.lightenChunks.add(new PositionedObject<>(lightPosition.x - baseChunkPosXAxis,
                    lightPosition.z - baseChunkPosZAxis, null));
        });
    }

    public void readChunks(int baseChunkPosXAxis, int baseChunkPosZAxis, int baseBlockPosXAxis, int baseBlockPosYAxis,
                           int baseBlockPosZAxis, WorldEditSessionImpl worldEditSession,
                           Long2ObjectMapView<WorldEditSessionImpl.ChunkData> chunks) {
        this.chunks.forEach(chunkDataPositioned -> {
            long newPos = ChunkCoordIntPair.pair(baseChunkPosXAxis + chunkDataPositioned.xOffset,
                    baseChunkPosZAxis + chunkDataPositioned.zOffset);

            List<BlockPosition> lights = chunkDataPositioned.object.lights.isEmpty() ? Collections.emptyList() : new LinkedList<>();
            chunkDataPositioned.object.lights.forEach(lightPositioned -> {
                lights.add(new BlockPosition(baseBlockPosXAxis + lightPositioned.xOffset,
                        baseBlockPosYAxis + lightPositioned.yOffset,
                        baseBlockPosZAxis + lightPositioned.zOffset));
            });

            chunks.put(newPos, worldEditSession.createChunkData(chunkDataPositioned.object.chunkSections,
                    chunkDataPositioned.object.heightmaps, lights));
        });
    }

    public void readBlocksToUpdate(int baseBlockPosXAxis, int baseBlockPosYAxis, int baseBlockPosZAxis, List<Pair<BlockPosition, IBlockData>> blocksToUpdate) {
        this.blocksToUpdate.forEach(blockToUpdatePositioned -> {
            BlockPosition newPos = new BlockPosition(baseBlockPosXAxis + blockToUpdatePositioned.xOffset,
                    baseBlockPosYAxis + blockToUpdatePositioned.yOffset,
                    baseBlockPosZAxis + blockToUpdatePositioned.zOffset);
            blocksToUpdate.add(new Pair<>(newPos, blockToUpdatePositioned.object));
        });
    }

    public void readBlockEntities(int baseBlockPosXAxis, int baseBlockPosYAxis, int baseBlockPosZAxis, List<Pair<BlockPosition, CompoundTag>> blockEntities) {
        this.blockEntities.forEach(blockEntityPositioned -> {
            BlockPosition newPos = new BlockPosition(baseBlockPosXAxis + blockEntityPositioned.xOffset,
                    baseBlockPosYAxis + blockEntityPositioned.yOffset,
                    baseBlockPosZAxis + blockEntityPositioned.zOffset);
            blockEntities.add(new Pair<>(newPos, blockEntityPositioned.object));
        });
    }

    public void readLights(int baseChunkPosXAxis, int baseChunkPosZAxis, Set<ChunkCoordIntPair> lightenChunks) {
        this.lightenChunks.forEach(lightPositioned -> {
            ChunkCoordIntPair newPos = new ChunkCoordIntPair(baseChunkPosXAxis + lightPositioned.xOffset,
                    baseChunkPosZAxis + lightPositioned.zOffset);
            lightenChunks.add(newPos);
        });
    }

    private static class PositionedObject<V> {

        private final int xOffset;
        private final int yOffset;
        private final int zOffset;
        private final V object;

        PositionedObject(int xOffset, int zOffset, V object) {
            this(xOffset, 0, zOffset, object);
        }

        PositionedObject(int xOffset, int yOffset, int zOffset, V object) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.zOffset = zOffset;
            this.object = object;
        }

    }

    private static class PositionedChunkData {

        private final ChunkSection[] chunkSections;
        private final Map<HeightMap.Type, HeightMap> heightmaps;
        private final List<PositionedObject<Void>> lights;

        public PositionedChunkData(ChunkSection[] chunkSections,
                                   Map<HeightMap.Type, HeightMap> heightmaps,
                                   List<PositionedObject<Void>> lights) {
            this.chunkSections = chunkSections;
            this.heightmaps = heightmaps;
            this.lights = lights;
        }

    }

}
