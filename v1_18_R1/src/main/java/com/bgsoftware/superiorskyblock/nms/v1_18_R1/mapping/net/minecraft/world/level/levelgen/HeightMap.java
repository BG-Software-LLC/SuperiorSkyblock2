package com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.world.level.levelgen;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.world.level.block.state.BlockData;
import org.jetbrains.annotations.Nullable;

public final class HeightMap extends MappedObject<net.minecraft.world.level.levelgen.HeightMap> {

    public HeightMap(net.minecraft.world.level.levelgen.HeightMap handle) {
        super(handle);
    }

    @Nullable
    public static HeightMap ofNullable(net.minecraft.world.level.levelgen.HeightMap handle) {
        return handle == null ? null : new HeightMap(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.levelgen.Heightmap",
            name = "update",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void setBlock(int x, int y, int z, BlockData blockData) {
        handle.a(x, y, z, blockData.getHandle());
    }

}
