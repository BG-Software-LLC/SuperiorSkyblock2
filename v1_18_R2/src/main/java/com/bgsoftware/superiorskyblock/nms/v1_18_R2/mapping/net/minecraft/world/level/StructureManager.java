package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.level;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import net.minecraft.server.level.RegionLimitedWorldAccess;

public final class StructureManager extends MappedObject<net.minecraft.world.level.StructureManager> {

    public StructureManager(net.minecraft.world.level.StructureManager handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.StructureFeatureManager",
            name = "forWorldGenRegion",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public StructureManager getStructureManager(RegionLimitedWorldAccess region) {
        return new StructureManager(this.handle.a(region));
    }

}
