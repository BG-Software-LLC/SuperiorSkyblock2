package com.bgsoftware.superiorskyblock.nms.v26_2.hologram;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class EntityHologram extends com.bgsoftware.superiorskyblock.nms.v26_2.hologram.AbstractEntityHologram {

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

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        // Prevent stand being equipped
        return InteractionResult.PASS;
    }
}
