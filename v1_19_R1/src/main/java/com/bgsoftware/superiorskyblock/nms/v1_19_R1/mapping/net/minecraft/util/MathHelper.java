package com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.util;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;

public final class MathHelper {

    @Remap(classPath = "net.minecraft.util.Mth",
            name = "floor",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public static int floor(double value) {
        return net.minecraft.util.MathHelper.b(value);
    }

}
