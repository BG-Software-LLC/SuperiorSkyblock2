package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.block.state.properties;

import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import net.minecraft.world.level.block.state.properties.IBlockState;

public class BlockState<T extends Comparable<T>> extends MappedObject<IBlockState<T>> {

    public BlockState(IBlockState<T> handle) {
        super(handle);
    }

    public String getName() {
        return handle.f();
    }

    public Class<T> getType() {
        return handle.g();
    }

}
