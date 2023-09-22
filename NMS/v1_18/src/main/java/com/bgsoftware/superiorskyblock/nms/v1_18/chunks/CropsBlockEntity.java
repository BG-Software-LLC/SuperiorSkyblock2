package com.bgsoftware.superiorskyblock.nms.v1_18.chunks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftMagicNumbers;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class CropsBlockEntity extends BlockEntity {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Map<Long, CropsBlockEntity> tickingChunks = new HashMap<>();
    private static int random = ThreadLocalRandom.current().nextInt();

    private final WeakReference<Island> island;
    private final WeakReference<LevelChunk> chunk;
    private final int chunkX;
    private final int chunkZ;

    private int currentTick = 0;

    private double cachedCropGrowthMultiplier;

    private CropsBlockEntity(Island island, LevelChunk levelChunk, BlockPos blockPos) {
        super(BlockEntityType.COMMAND_BLOCK, blockPos, levelChunk.level.getBlockState(blockPos));
        this.island = new WeakReference<>(island);
        this.chunk = new WeakReference<>(levelChunk);
        ChunkPos chunkPos = levelChunk.getPos();
        this.chunkX = chunkPos.x;
        this.chunkZ = chunkPos.z;
        setLevel(levelChunk.level);
        levelChunk.level.addBlockEntityTicker(new CropsTickingBlockEntity(this));
        this.cachedCropGrowthMultiplier = island.getCropGrowthMultiplier() - 1;
    }

    public static void create(Island island, LevelChunk levelChunk) {
        ChunkPos chunkPos = levelChunk.getPos();
        long chunkPair = chunkPos.toLong();
        if (!tickingChunks.containsKey(chunkPair)) {
            BlockPos blockPosition = new BlockPos(chunkPos.x << 4, 1, chunkPos.z << 4);
            tickingChunks.put(chunkPair, new CropsBlockEntity(island, levelChunk, blockPosition));
        }
    }

    public static CropsBlockEntity remove(ChunkPos chunkPos) {
        return tickingChunks.remove(chunkPos.toLong());
    }

    public static void forEachChunk(List<ChunkPosition> chunkPositions, Consumer<CropsBlockEntity> cropsBlockEntityConsumer) {
        if (tickingChunks.isEmpty())
            return;

        chunkPositions.forEach(chunkPosition -> {
            long chunkKey = chunkPosition.asPair();
            CropsBlockEntity cropsBlockEntity = tickingChunks.get(chunkKey);
            if (cropsBlockEntity != null)
                cropsBlockEntityConsumer.accept(cropsBlockEntity);
        });
    }

    public void remove() {
        this.remove = true;
    }

    public void tick() {
        if (++currentTick <= plugin.getSettings().getCropsInterval())
            return;

        LevelChunk levelChunk = this.chunk.get();
        Island island = this.island.get();
        ServerLevel serverLevel = (ServerLevel) getLevel();

        if (levelChunk == null || island == null || serverLevel == null) {
            remove();
            return;
        }

        currentTick = 0;

        int worldRandomTick = serverLevel.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);

        int chunkRandomTickSpeed = (int) (worldRandomTick * this.cachedCropGrowthMultiplier * plugin.getSettings().getCropsInterval());

        if (chunkRandomTickSpeed > 0) {
            for (LevelChunkSection levelChunkSection : levelChunk.getSections()) {
                if (levelChunkSection != null && levelChunkSection.isRandomlyTicking()) {
                    for (int i = 0; i < chunkRandomTickSpeed; i++) {
                        random = random * 3 + 1013904223;
                        int factor = random >> 2;
                        int x = factor & 15;
                        int z = factor >> 8 & 15;
                        int y = factor >> 16 & 15;
                        BlockState blockState = levelChunkSection.getBlockState(x, y, z);
                        Block block = blockState.getBlock();
                        if (block.isRandomlyTicking(blockState) && plugin.getSettings().getCropsToGrow().contains(
                                CraftMagicNumbers.getMaterial(block).name())) {
                            BlockPos blockPos = new BlockPos(x + (chunkX << 4),
                                    y + levelChunkSection.bottomBlockY(), z + (chunkZ << 4));
                            blockState.randomTick(serverLevel, blockPos, serverLevel.getRandom());
                        }
                    }
                }
            }
        }
    }

    public void setCropGrowthMultiplier(double cropGrowthMultiplier) {
        this.cachedCropGrowthMultiplier = cropGrowthMultiplier;
    }

}
