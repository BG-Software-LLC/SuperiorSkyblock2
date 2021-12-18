package com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level;

import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.MappedObject;

public final class GameRules extends MappedObject<net.minecraft.world.level.GameRules> {

    public GameRules(net.minecraft.world.level.GameRules handle) {
        super(handle);
    }

    public int getInt(net.minecraft.world.level.GameRules.GameRuleKey<net.minecraft.world.level.GameRules.GameRuleInt>
                              gameRuleKey) {
        return handle.c(gameRuleKey);
    }

}
