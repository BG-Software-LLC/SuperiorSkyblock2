package com.bgsoftware.superiorskyblock.platform.event;

public @interface GameEventFlags {

    int ENTITY_EVENT = 1 << 0;
    int MAYBE_ENTITY_EVENT = 1 << 1;
    int BLOCK_EVENT = 1 << 2;
    int MAYBE_BLOCK_EVENT = 1 << 3;
    int PLAYER_EVENT = 1 << 4;
    int GENERIC_WORLD_EVENT = 1 << 5;
    int INVENTORY_EVENT = 1 << 5;

}
