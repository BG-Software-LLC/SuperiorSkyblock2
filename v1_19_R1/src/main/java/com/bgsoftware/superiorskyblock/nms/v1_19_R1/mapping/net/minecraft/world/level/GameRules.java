package com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.level;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.MappedObject;

public final class GameRules extends MappedObject<net.minecraft.world.level.GameRules> {

    public GameRules(net.minecraft.world.level.GameRules handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.GameRules",
            name = "getInt",
            type = Remap.Type.METHOD,
            remappedName = "c")
    public int getInt(net.minecraft.world.level.GameRules.GameRuleKey<net.minecraft.world.level.GameRules.GameRuleInt>
                              gameRuleKey) {
        return handle.c(gameRuleKey);
    }

}
