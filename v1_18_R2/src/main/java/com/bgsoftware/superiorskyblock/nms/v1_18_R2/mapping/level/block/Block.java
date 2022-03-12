package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.block;

import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.block.entity.TileEntity;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.block.state.BlockData;
import net.minecraft.core.RegistryBlockID;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ITileEntity;
import net.minecraft.world.level.block.state.IBlockData;

public final class Block extends MappedObject<net.minecraft.world.level.block.Block> {

    public static final Block AIR = new Block(Blocks.a);
    public static final RegistryBlockID<IBlockData> CODEC = net.minecraft.world.level.block.Block.o;

    public Block(net.minecraft.world.level.block.Block block) {
        super(block);
    }

    public static Block ofNullable(net.minecraft.world.level.block.Block block) {
        return block == null ? null : new Block(block);
    }

    public BlockData getBlockData() {
        return new BlockData(handle.n());
    }

    public boolean isTicking(BlockData blockData) {
        return handle.e_(blockData.getHandle());
    }

    public static int getCombinedId(BlockData blockData) {
        return net.minecraft.world.level.block.Block.i(blockData.getHandle());
    }

    public static BlockData getByCombinedId(int combinedId) {
        return new BlockData(net.minecraft.world.level.block.Block.a(combinedId));
    }

}
