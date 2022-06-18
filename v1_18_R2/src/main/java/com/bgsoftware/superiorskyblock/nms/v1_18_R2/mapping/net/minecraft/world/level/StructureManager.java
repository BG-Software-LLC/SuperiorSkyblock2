package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.level;

import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import net.minecraft.server.level.RegionLimitedWorldAccess;

public class StructureManager extends MappedObject<net.minecraft.world.level.StructureManager> {

    public StructureManager(net.minecraft.world.level.StructureManager handle) {
        super(handle);
    }

    public StructureManager getStructureManager(RegionLimitedWorldAccess region) {
        return new StructureManager(this.handle.a(region));
    }

}
