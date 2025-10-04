package com.bgsoftware.superiorskyblock.nms.v1_21_7.hologram;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class EntityHologram extends com.bgsoftware.superiorskyblock.nms.v1_21_7.hologram.AbstractEntityHologram {

    public EntityHologram(ServerLevel serverLevel, double x, double y, double z) {
        super(serverLevel, x, y, z);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        // Do not save NBT.
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output, boolean includeAll) {
        // Do not save NBT.
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        // Do not load NBT.
    }

    @Override
    public boolean saveAsPassenger(ValueOutput output, boolean includeAll, boolean includeNonSaveable, boolean forceSerialization) {
        // Do not save NBT.
        return false;
    }

    @Override
    public boolean saveAsPassenger(ValueOutput output) {
        // Do not save NBT.
        return false;
    }

    @Override
    public void saveWithoutId(ValueOutput output, boolean includeAll, boolean includeNonSaveable, boolean forceSerialization) {
        // Do not save NBT.
    }

    @Override
    public void saveWithoutId(ValueOutput output) {
        // Do not save NBT.
    }

    @Override
    public void load(ValueInput input) {
        // Do not load NBT.
    }

}
