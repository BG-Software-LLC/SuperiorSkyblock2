package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.core;

import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.level.ChunkCoordIntPair;

public class SectionPosition extends MappedObject<net.minecraft.core.SectionPosition> {

    public SectionPosition(net.minecraft.core.SectionPosition handle) {
        super(handle);
    }

    public static SectionPosition getByIndex(ChunkCoordIntPair chunkCoords, int index) {
        return new SectionPosition(net.minecraft.core.SectionPosition.a(chunkCoords.getHandle(), index));
    }

    public static int getSectionCoord(int coord) {
        return coord >> 4;
    }

}
