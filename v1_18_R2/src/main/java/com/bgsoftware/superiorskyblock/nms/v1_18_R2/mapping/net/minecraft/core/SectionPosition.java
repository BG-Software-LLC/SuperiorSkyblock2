package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.core;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.level.ChunkCoordIntPair;

public final class SectionPosition extends MappedObject<net.minecraft.core.SectionPosition> {

    public SectionPosition(net.minecraft.core.SectionPosition handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.core.SectionPos",
            name = "of",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public static SectionPosition getByIndex(ChunkCoordIntPair chunkCoords, int index) {
        return new SectionPosition(net.minecraft.core.SectionPosition.a(chunkCoords.getHandle(), index));
    }

    @Remap(classPath = "net.minecraft.core.SectionPos",
            name = "blockToSectionCoord",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public static int getSectionCoord(int coord) {
        return net.minecraft.core.SectionPosition.a(coord);
    }

}
