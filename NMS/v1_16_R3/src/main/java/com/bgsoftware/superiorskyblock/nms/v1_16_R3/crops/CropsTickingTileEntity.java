package com.bgsoftware.superiorskyblock.nms.v1_16_R3.crops;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.collections.CollectionsFactory;
import com.bgsoftware.superiorskyblock.core.collections.view.Long2ObjectMapView;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Chunk;
import net.minecraft.server.v1_16_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_16_R3.GameRules;
import net.minecraft.server.v1_16_R3.ITickable;
import net.minecraft.server.v1_16_R3.TileEntity;
import net.minecraft.server.v1_16_R3.TileEntityTypes;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.function.Consumer;

public class CropsTickingTileEntity extends TileEntity implements ITickable {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Long2ObjectMapView<CropsTickingTileEntity> tickingChunks = CollectionsFactory.createLong2ObjectHashMap();

    private final WeakReference<Island> island;
    private final WeakReference<Chunk> chunk;

    private int currentTick = 0;

    private double cachedCropGrowthMultiplier;

    private CropsTickingTileEntity(Island island, Chunk chunk) {
        super(TileEntityTypes.COMMAND_BLOCK);
        this.island = new WeakReference<>(island);
        this.chunk = new WeakReference<>(chunk);
        ChunkCoordIntPair chunkCoord = chunk.getPos();
        setLocation(chunk.getWorld(), new BlockPosition(chunkCoord.x << 4, 1, chunkCoord.z << 4));

        try {
            // Not a method of Spigot - fixes https://github.com/OmerBenGera/SuperiorSkyblock2/issues/5
            setCurrentChunk(chunk);
        } catch (Throwable ignored) {
        }

        assert world != null;

        world.tileEntityListTick.add(this);
        this.cachedCropGrowthMultiplier = island.getCropGrowthMultiplier() - 1;
    }

    public static void create(Island island, Chunk chunk) {
        long chunkPair = chunk.getPos().pair();
        tickingChunks.computeIfAbsent(chunkPair, i -> new CropsTickingTileEntity(island, chunk));
    }

    public static CropsTickingTileEntity remove(long chunkCoords) {
        return tickingChunks.remove(chunkCoords);
    }

    public static void forEachChunk(List<ChunkPosition> chunkPositions, Consumer<CropsTickingTileEntity> cropsTickingTileEntityConsumer) {
        if (tickingChunks.isEmpty())
            return;

        chunkPositions.forEach(chunkPosition -> {
            long chunkKey = chunkPosition.asPair();
            CropsTickingTileEntity cropsTickingTileEntity = tickingChunks.get(chunkKey);
            if (cropsTickingTileEntity != null)
                cropsTickingTileEntityConsumer.accept(cropsTickingTileEntity);
        });
    }

    @Override
    public void tick() {
        assert world != null;

        if (++currentTick <= plugin.getSettings().getCropsInterval())
            return;

        Chunk chunk = this.chunk.get();
        Island island = this.island.get();

        if (chunk == null || island == null) {
            world.tileEntityListTick.remove(this);
            return;
        }

        currentTick = 0;

        int worldRandomTick = world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
        int chunkRandomTickSpeed = (int) (worldRandomTick * this.cachedCropGrowthMultiplier * plugin.getSettings().getCropsInterval());
        if (chunkRandomTickSpeed > 0)
            CropsTickingMethod.tick(chunk, chunkRandomTickSpeed);
    }

    @Override
    public void w() {
        tick();
    }

    public void setCropGrowthMultiplier(double cropGrowthMultiplier) {
        this.cachedCropGrowthMultiplier = cropGrowthMultiplier;
    }

}
