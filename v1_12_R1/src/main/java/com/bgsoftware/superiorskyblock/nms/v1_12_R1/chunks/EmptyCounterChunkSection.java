package com.bgsoftware.superiorskyblock.nms.v1_12_R1.chunks;

import com.bgsoftware.common.reflection.ReflectField;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.ChunkSection;
import net.minecraft.server.v1_12_R1.DataPaletteBlock;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.NibbleArray;

public final class EmptyCounterChunkSection extends ChunkSection {

    private static final ReflectField<Integer> NON_EMPTY_BLOCK_COUNT = new ReflectField<>(ChunkSection.class, int.class, "nonEmptyBlockCount");
    private static final ReflectField<Integer> TICKING_BLOCK_COUNT = new ReflectField<>(ChunkSection.class, int.class, "tickingBlockCount");
    private static final ReflectField<DataPaletteBlock> BLOCK_IDS = new ReflectField<>(ChunkSection.class, DataPaletteBlock.class, "blockIds");
    private static final ReflectField<NibbleArray> EMITTED_LIGHT = new ReflectField<>(ChunkSection.class, NibbleArray.class, "emittedLight");
    private static final ReflectField<NibbleArray> SKY_LIGHT = new ReflectField<>(ChunkSection.class, NibbleArray.class, "skyLight");

    private int nonEmptyBlockCount, tickingBlockCount;

    private EmptyCounterChunkSection(ChunkSection chunkSection) {
        super(chunkSection.getYPosition(), chunkSection.getSkyLightArray() != null);

        nonEmptyBlockCount = NON_EMPTY_BLOCK_COUNT.get(chunkSection, 0);
        tickingBlockCount = TICKING_BLOCK_COUNT.get(chunkSection, 0);
        BLOCK_IDS.set(this, chunkSection.getBlocks());
        EMITTED_LIGHT.set(this, chunkSection.getEmittedLightArray());
        SKY_LIGHT.set(this, chunkSection.getSkyLightArray());
    }

    @Override
    public void setType(int i, int j, int k, IBlockData iblockdata) {
        Block currentBlock = getType(i, j, k).getBlock(), placedBlock = iblockdata.getBlock();

        if (currentBlock != Blocks.AIR) {
            nonEmptyBlockCount--;
            if (currentBlock.isTicking()) {
                tickingBlockCount--;
            }
        }

        if (placedBlock != Blocks.AIR) {
            nonEmptyBlockCount++;
            if (placedBlock.isTicking()) {
                tickingBlockCount++;
            }
        }

        super.setType(i, j, k, iblockdata);
    }

    public void recalcBlockCounts() {
        nonEmptyBlockCount = 0;
        tickingBlockCount = 0;

        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    Block block = getType(i, j, k).getBlock();
                    if (block != Blocks.AIR) {
                        nonEmptyBlockCount++;
                        if (block.isTicking()) {
                            tickingBlockCount++;
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldTick() {
        return tickingBlockCount > 0;
    }

    @Override
    public boolean a() {
        return nonEmptyBlockCount == 0;
    }

    public static EmptyCounterChunkSection of(ChunkSection chunkSection) {
        return chunkSection == null ? null : new EmptyCounterChunkSection(chunkSection);
    }

}
