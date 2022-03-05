package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.world.level.block.state.pattern;

import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import net.minecraft.world.level.block.state.IBlockData;

import java.util.function.Predicate;

public final class ShapeDetectorBlock extends
        MappedObject<net.minecraft.world.level.block.state.pattern.ShapeDetectorBlock> {

    public ShapeDetectorBlock(net.minecraft.world.level.block.state.pattern.ShapeDetectorBlock handle) {
        super(handle);
    }

    public BlockPosition getPosition() {
        return new BlockPosition(handle.d());
    }

    public static Predicate<net.minecraft.world.level.block.state.pattern.ShapeDetectorBlock> hasState(
            Predicate<IBlockData> state) {
        return net.minecraft.world.level.block.state.pattern.ShapeDetectorBlock.a(state);
    }

}
