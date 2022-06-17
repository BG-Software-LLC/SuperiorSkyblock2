package com.bgsoftware.superiorskyblock.temp.nms.v1_18_R1.mapping.level.pathfinder;

import com.bgsoftware.superiorskyblock.temp.nms.v1_18_R1.mapping.MappedObject;

public class PathPoint extends MappedObject<net.minecraft.world.level.pathfinder.PathPoint> {

    public PathPoint(int x, int y, int z) {
        this(new net.minecraft.world.level.pathfinder.PathPoint(x, y, z));
    }

    public PathPoint(net.minecraft.world.level.pathfinder.PathPoint handle) {
        super(handle);
    }

}
