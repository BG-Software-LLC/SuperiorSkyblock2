package com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.entity;

import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.MappedObject;
import net.minecraft.world.level.entity.EntityAccess;

public final class LevelEntityGetter<T extends EntityAccess> extends
        MappedObject<net.minecraft.world.level.entity.LevelEntityGetter<T>> {

    public LevelEntityGetter(net.minecraft.world.level.entity.LevelEntityGetter<T> handle) {
        super(handle);
    }

    public Iterable<T> getAll() {
        return handle.a();
    }

}
