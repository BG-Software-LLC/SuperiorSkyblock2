package com.bgsoftware.superiorskyblock.nms.v1_21.chunks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;

public record CropsTickingBlockEntity(CropsBlockEntity cropsBlockEntity) implements TickingBlockEntity {


    @Override
    public void tick() {
        cropsBlockEntity.tick();
    }

    @Override
    public boolean isRemoved() {
        return cropsBlockEntity.isRemoved();
    }

    @Override
    public BlockPos getPos() {
        return cropsBlockEntity.getBlockPos();
    }

    @Override
    public String getType() {
        return BlockEntityType.getKey(cropsBlockEntity.getType()) + "";
    }

}
