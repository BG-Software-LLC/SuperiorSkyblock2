package com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level;

import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.MappedObject;
import net.minecraft.server.level.RegionLimitedWorldAccess;

public final class StructureManager extends MappedObject<net.minecraft.world.level.StructureManager> {

    public StructureManager(net.minecraft.world.level.StructureManager handle) {
        super(handle);
    }

    public StructureManager getStructureManager(RegionLimitedWorldAccess region) {
        return new StructureManager(this.handle.a(region));
    }

}
