package com.bgsoftware.superiorskyblock.nms.v1_18.crops;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftMagicNumbers;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public abstract class CropsTickingMethod {

    private static final ReflectField<Random> SERVER_LEVEL_RANDOM_TICK_RANDOM = new ReflectField<>(
            ServerLevel.class, Random.class, "randomTickRandom");

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final BlockPos.MutableBlockPos chunkTickMutablePosition = new BlockPos.MutableBlockPos();

    private static final CropsTickingMethod INSTANCE = SERVER_LEVEL_RANDOM_TICK_RANDOM.isValid() ?
            new PaperCropsTickingMethod() : new SpigotCropsTickingMethod();


    protected CropsTickingMethod() {

    }

    public static void tick(LevelChunk levelChunk, int tickSpeed) {
        INSTANCE.doTick(levelChunk, tickSpeed);
    }

    protected abstract void doTick(LevelChunk levelChunk, int tickSpeed);

    private static class PaperCropsTickingMethod extends CropsTickingMethod {

        @Override
        protected void doTick(LevelChunk levelChunk, int tickSpeed) {
            ServerLevel serverLevel = levelChunk.level;
            LevelChunkSection[] sections = levelChunk.getSections();

            int minSection = serverLevel.getMinSection();
            ChunkPos chunkPos = levelChunk.getPos();
            int chunkOffsetX = chunkPos.x << 4;
            int chunkOffsetZ = chunkPos.z << 4;

            Random randomTickRandom = SERVER_LEVEL_RANDOM_TICK_RANDOM.get(serverLevel);

            for (int sectionIndex = 0, sectionsLen = sections.length; sectionIndex < sectionsLen; sectionIndex++) {
                LevelChunkSection levelChunkSection = sections[sectionIndex];
                if (levelChunkSection == null || !levelChunkSection.isRandomlyTickingBlocks()) {
                    continue;
                }

                int tickingBlocks = levelChunkSection.tickingList.size();
                if (tickingBlocks <= 0) {
                    continue;
                }

                int offsetY = (sectionIndex + minSection) << 4;

                for (int i = 0; i < tickSpeed; ++i) {
                    int index = randomTickRandom.nextInt(0x1000);

                    if (index >= tickingBlocks) {
                        continue;
                    }

                    long raw = levelChunkSection.tickingList.getRaw(index);
                    int location = com.destroystokyo.paper.util.maplist.IBlockDataList.getLocationFromRaw(raw);
                    BlockState blockState = com.destroystokyo.paper.util.maplist.IBlockDataList.getBlockDataFromRaw(raw);
                    Block block = blockState.getBlock();
                    if (!plugin.getSettings().getCropsToGrow().contains(CraftMagicNumbers.getMaterial(block).name()))
                        continue;

                    chunkTickMutablePosition.set((location & 15) | chunkOffsetX,
                            ((location >>> 8) & 255) | offsetY,
                            ((location >>> 4) & 15) | chunkOffsetZ);

                    blockState.randomTick(serverLevel, chunkTickMutablePosition, randomTickRandom);
                }
            }

        }

    }

    private static class SpigotCropsTickingMethod extends CropsTickingMethod {

        private static int random = ThreadLocalRandom.current().nextInt();

        @Override
        protected void doTick(LevelChunk levelChunk, int tickSpeed) {
            LevelChunkSection[] sections = levelChunk.getSections();
            ServerLevel serverLevel = levelChunk.level;

            ChunkPos chunkPos = levelChunk.getPos();
            int chunkOffsetX = chunkPos.x << 4;
            int chunkOffsetZ = chunkPos.z << 4;

            for (int sectionIndex = 0; sectionIndex < sections.length; ++sectionIndex) {
                LevelChunkSection levelChunkSection = sections[sectionIndex];
                if (levelChunkSection == null || !levelChunkSection.isRandomlyTickingBlocks()) {
                    continue;
                }

                int sectionBottomY = serverLevel.getSectionYFromSectionIndex(sectionIndex) << 4;

                for (int i = 0; i < tickSpeed; i++) {
                    random = random * 3 + 1013904223;
                    int factor = random >> 2;
                    int x = factor & 15;
                    int z = factor >> 8 & 15;
                    int y = factor >> 16 & 15;
                    BlockState blockState = levelChunkSection.getBlockState(x, y, z);
                    Block block = blockState.getBlock();
                    if (blockState.isRandomlyTicking() &&
                            plugin.getSettings().getCropsToGrow().contains(CraftMagicNumbers.getMaterial(block).name())) {
                        chunkTickMutablePosition.set(x + chunkOffsetX, y + sectionBottomY, z + chunkOffsetZ);
                        blockState.randomTick(serverLevel, chunkTickMutablePosition, serverLevel.getRandom());
                    }
                }
            }
        }
    }

}
