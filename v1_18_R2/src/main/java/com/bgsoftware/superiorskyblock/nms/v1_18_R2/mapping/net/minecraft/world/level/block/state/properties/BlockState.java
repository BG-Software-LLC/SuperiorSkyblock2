package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.level.block.state.properties;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import net.minecraft.world.level.block.state.properties.IBlockState;

public final class BlockState<T extends Comparable<T>> extends MappedObject<IBlockState<T>> {

    public BlockState(IBlockState<T> handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.block.state.properties.Property",
            name = "getName",
            type = Remap.Type.METHOD,
            remappedName = "f")
    public String getName() {
        return handle.f();
    }

    @Remap(classPath = "net.minecraft.world.level.block.state.properties.Property",
            name = "getValueClass",
            type = Remap.Type.METHOD,
            remappedName = "g")
    public Class<T> getType() {
        return handle.g();
    }

}
