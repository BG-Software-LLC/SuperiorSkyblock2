package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.level.block;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.level.block.state.BlockData;
import net.minecraft.core.RegistryBlockID;
import net.minecraft.world.level.block.Blocks;
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

    @Remap(classPath = "net.minecraft.world.level.block.Block",
            name = "defaultBlockState",
            type = Remap.Type.METHOD,
            remappedName = "n")
    public BlockData getBlockData() {
        return new BlockData(handle.n());
    }

    @Remap(classPath = "net.minecraft.world.level.block.Block",
            name = "isRandomlyTicking",
            type = Remap.Type.METHOD,
            remappedName = "e_")
    public boolean isTicking(BlockData blockData) {
        return handle.e_(blockData.getHandle());
    }

    @Remap(classPath = "net.minecraft.world.level.block.Block",
            name = "getId",
            type = Remap.Type.METHOD,
            remappedName = "i")
    public static int getCombinedId(BlockData blockData) {
        return net.minecraft.world.level.block.Block.i(blockData.getHandle());
    }

    @Remap(classPath = "net.minecraft.world.level.block.Block",
            name = "stateById",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public static BlockData getByCombinedId(int combinedId) {
        return new BlockData(net.minecraft.world.level.block.Block.a(combinedId));
    }

}
