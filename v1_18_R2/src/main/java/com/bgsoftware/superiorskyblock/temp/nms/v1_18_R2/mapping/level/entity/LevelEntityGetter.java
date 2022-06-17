package com.bgsoftware.superiorskyblock.temp.nms.v1_18_R2.mapping.level.entity;

import com.bgsoftware.superiorskyblock.temp.nms.v1_18_R2.mapping.MappedObject;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.phys.AxisAlignedBB;

import java.util.function.Consumer;

public class LevelEntityGetter<T extends EntityAccess> extends
        MappedObject<net.minecraft.world.level.entity.LevelEntityGetter<T>> {

    public LevelEntityGetter(net.minecraft.world.level.entity.LevelEntityGetter<T> handle) {
        super(handle);
    }

    public void get(AxisAlignedBB boundingBox, Consumer<T> consumer) {
        handle.a(boundingBox, consumer);
    }

}
