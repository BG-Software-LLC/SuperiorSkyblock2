package com.bgsoftware.superiorskyblock.nms.v1_20_4.hologram;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class EntityHologram extends com.bgsoftware.superiorskyblock.nms.v1_20_4.hologram.AbstractEntityHologram {

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

    @Override
    public InteractionResult interactAt(Player player, Vec3 hitPos, InteractionHand hand) {
        // Prevent stand being equipped
        return InteractionResult.PASS;
    }

}
