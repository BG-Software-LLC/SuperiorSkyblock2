package com.bgsoftware.superiorskyblock.nms.v1_19.hologram;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public class EntityHologram extends com.bgsoftware.superiorskyblock.nms.v1_19.hologram.AbstractEntityHologram {

    public EntityHologram(ServerLevel serverLevel, double x, double y, double z) {
        super(serverLevel, x, y, z);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        // Do not save NBT.
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        // Do not load NBT.
    }

    @Override
    public boolean saveAsPassenger(CompoundTag compoundTag) {
        // Do not save NBT.
        return false;
    }

    @Override
    public CompoundTag saveWithoutId(CompoundTag compoundTag) {
        // Do not save NBT.
        return compoundTag;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        // Do not load NBT.
    }

}
