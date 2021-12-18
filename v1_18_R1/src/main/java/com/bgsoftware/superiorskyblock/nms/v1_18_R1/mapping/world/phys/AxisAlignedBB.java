package com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.world.phys;

import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.MappedObject;

public final class AxisAlignedBB extends MappedObject<net.minecraft.world.phys.AxisAlignedBB> {

    public AxisAlignedBB(BlockPosition min, BlockPosition max) {
        this(new net.minecraft.world.phys.AxisAlignedBB(min.getHandle(), max.getHandle()));
    }

    public AxisAlignedBB(net.minecraft.world.phys.AxisAlignedBB handle) {
        super(handle);
    }

    public boolean intercepts(net.minecraft.world.phys.AxisAlignedBB other) {
        return handle.c(other);
    }

}
