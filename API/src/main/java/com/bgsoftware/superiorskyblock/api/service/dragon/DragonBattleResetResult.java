package com.bgsoftware.superiorskyblock.api.service.dragon;

public enum DragonBattleResetResult {

    /**
     * The dragon battle could not be reset because they are disabled on the server.
     */
    DRAGON_BATTLES_DISABLED,

    /**
     * The dragon battle could not be reset because the world was never generated before.
     */
    WORLD_NOT_GENERATED,

    /**
     * The dragon battle could not be reset because the world is not unlocked for the island.
     */
    WORLD_NOT_UNLOCKED,

    /**
     * The dragon battle was successfully been reset.
     */
    SUCCESS

}
