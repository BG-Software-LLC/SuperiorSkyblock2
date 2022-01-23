package com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.dimension.end;

import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.MappedObject;
import org.jetbrains.annotations.Nullable;

public final class EnderDragonBattle extends MappedObject<net.minecraft.world.level.dimension.end.EnderDragonBattle> {

    public EnderDragonBattle(net.minecraft.world.level.dimension.end.EnderDragonBattle handle) {
        super(handle);
    }

    @Nullable
    public static EnderDragonBattle ofNullable(net.minecraft.world.level.dimension.end.EnderDragonBattle handle) {
        return handle == null ? null : new EnderDragonBattle(handle);
    }

    public int getCrystalsAlive() {
        return handle.c();
    }

}
