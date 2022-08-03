package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.level.material;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;

public final class Material extends MappedObject<net.minecraft.world.level.material.Material> {

    public Material(net.minecraft.world.level.material.Material handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.material.Material",
            name = "isLiquid",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public boolean isLiquid() {
        return handle.a();
    }

}
