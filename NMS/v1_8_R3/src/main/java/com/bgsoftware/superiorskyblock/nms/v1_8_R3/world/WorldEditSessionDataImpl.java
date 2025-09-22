package com.bgsoftware.superiorskyblock.nms.v1_8_R3.world;

import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.collections.view.Long2ObjectMapView;
import com.bgsoftware.superiorskyblock.nms.world.WorldEditSession;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R3.IBlockData;
import org.bukkit.Location;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class WorldEditSessionDataImpl implements WorldEditSession.Data {

    private final List<PositionedObject<WorldEditSessionImpl.ChunkData>> chunks = new LinkedList<>();
    private final List<PositionedObject<IBlockData>> blocksToUpdate = new LinkedList<>();
    private final List<PositionedObject<CompoundTag>> blockEntities = new LinkedList<>();
    private final List<PositionedObject<Void>> lights = new LinkedList<>();

    public WorldEditSessionDataImpl(Location baseLocation, Long2ObjectMapView<WorldEditSessionImpl.ChunkData> chunks,
                                    List<Pair<BlockPosition, IBlockData>> blocksToUpdate,
                                    List<Pair<BlockPosition, CompoundTag>> blockEntities,
                                    List<BlockPosition> lights) {
        int baseBlockPosXAxis = baseLocation.getBlockX();
        int baseBlockPosYAxis = baseLocation.getBlockY();
        int baseBlockPosZAxis = baseLocation.getBlockZ();
        int baseChunkPosXAxis = baseBlockPosXAxis >> 4;
        int baseChunkPosZAxis = baseBlockPosZAxis >> 4;

        // Convert chunk data
        Iterator<Long2ObjectMapView.Entry<WorldEditSessionImpl.ChunkData>> chunksIterator = chunks.entryIterator();
        while (chunksIterator.hasNext()) {
            Long2ObjectMapView.Entry<WorldEditSessionImpl.ChunkData> entry = chunksIterator.next();
            int currChunkPosXAxis = getChunkCoordX(entry.getKey());
            int currChunkPosZAxis = getChunkCoordZ(entry.getKey());
            this.chunks.add(new PositionedObject<>(currChunkPosXAxis - baseChunkPosXAxis,
                    currChunkPosZAxis - baseChunkPosZAxis, entry.getValue()));
        }

        // Convert blocksToUpdate
        blocksToUpdate.forEach(blockToUpdate -> {
            BlockPosition blockPosition = blockToUpdate.getKey();
            this.blocksToUpdate.add(new PositionedObject<>(blockPosition.getX() - baseBlockPosXAxis,
                    blockPosition.getY() - baseBlockPosYAxis, blockPosition.getZ() - baseBlockPosZAxis,
                    blockToUpdate.getValue()));
        });

        // Convert blockEntities
        blockEntities.forEach(blockEntity -> {
            BlockPosition blockPosition = blockEntity.getKey();
            this.blockEntities.add(new PositionedObject<>(blockPosition.getX() - baseBlockPosXAxis,
                    blockPosition.getY() - baseBlockPosYAxis, blockPosition.getZ() - baseBlockPosZAxis,
                    blockEntity.getValue()));
        });

        // Convert lights
        lights.forEach(lightPosition -> {
            this.lights.add(new PositionedObject<>(lightPosition.getX() - baseBlockPosXAxis,
                    lightPosition.getY() - baseBlockPosYAxis, lightPosition.getZ() - baseBlockPosZAxis,
                    null));
        });
    }

    public void readChunks(int baseChunkPosXAxis, int baseChunkPosZAxis, Long2ObjectMapView<WorldEditSessionImpl.ChunkData> chunks) {
        this.chunks.forEach(chunkDataPositioned -> {
            long newPos = ChunkCoordIntPair.a(baseChunkPosXAxis + chunkDataPositioned.xOffset,
                    baseChunkPosZAxis + chunkDataPositioned.zOffset);
            chunks.put(newPos, chunkDataPositioned.object);
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

    public void readLights(int baseBlockPosXAxis, int baseBlockPosYAxis, int baseBlockPosZAxis, List<BlockPosition> lights) {
        this.lights.forEach(lightPositioned -> {
            BlockPosition newPos = new BlockPosition(baseBlockPosXAxis + lightPositioned.xOffset,
                    baseBlockPosYAxis + lightPositioned.yOffset,
                    baseBlockPosZAxis + lightPositioned.zOffset);
            lights.add(newPos);
        });
    }

    private static int getChunkCoordX(long i) {
        return (int) (i & 4294967295L);
    }

    private static int getChunkCoordZ(long i) {
        return (int) (i >>> 32 & 4294967295L);
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

}