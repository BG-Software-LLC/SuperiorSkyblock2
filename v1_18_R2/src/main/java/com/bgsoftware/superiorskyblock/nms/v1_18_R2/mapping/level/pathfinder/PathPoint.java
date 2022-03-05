package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.pathfinder;

import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;

public final class PathPoint extends MappedObject<net.minecraft.world.level.pathfinder.PathPoint> {

    public PathPoint(int x, int y, int z) {
        this(new net.minecraft.world.level.pathfinder.PathPoint(x, y, z));
    }

    public PathPoint(net.minecraft.world.level.pathfinder.PathPoint handle) {
        super(handle);
    }

}
