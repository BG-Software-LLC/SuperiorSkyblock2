package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.material;

import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;

public final class Material extends MappedObject<net.minecraft.world.level.material.Material> {

    public Material(net.minecraft.world.level.material.Material handle) {
        super(handle);
    }

    public boolean isLiquid() {
        return handle.a();
    }

}
