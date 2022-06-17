package com.bgsoftware.superiorskyblock.temp.nms.v1_18_R2.mapping.level.pathfinder;

import com.bgsoftware.superiorskyblock.temp.nms.v1_18_R2.mapping.BlockPosition;
import com.bgsoftware.superiorskyblock.temp.nms.v1_18_R2.mapping.MappedObject;
import org.jetbrains.annotations.Nullable;

public class PathEntity extends MappedObject<net.minecraft.world.level.pathfinder.PathEntity> {

    public PathEntity(net.minecraft.world.level.pathfinder.PathEntity handle) {
        super(handle);
    }

    @Nullable
    public static PathEntity ofNullable(net.minecraft.world.level.pathfinder.PathEntity handle) {
        return handle == null ? null : new PathEntity(handle);
    }

    public boolean isDone() {
        return handle.c();
    }

    public BlockPosition getNextNodePos() {
        return new BlockPosition(handle.g());
    }

    public void advance() {
        handle.a();
    }


}
