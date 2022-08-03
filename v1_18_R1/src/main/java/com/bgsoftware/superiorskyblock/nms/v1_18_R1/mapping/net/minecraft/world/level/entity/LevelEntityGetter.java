package com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.world.level.entity;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.MappedObject;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.phys.AxisAlignedBB;

import java.util.function.Consumer;

public final class LevelEntityGetter<T extends EntityAccess> extends
        MappedObject<net.minecraft.world.level.entity.LevelEntityGetter<T>> {

    public LevelEntityGetter(net.minecraft.world.level.entity.LevelEntityGetter<T> handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.entity.LevelEntityGetter",
            name = "get",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void get(AxisAlignedBB boundingBox, Consumer<T> consumer) {
        handle.a(boundingBox, consumer);
    }

}
