package com.bgsoftware.superiorskyblock.world.chunk;

public enum ChunkLoadReason {

    SCHEMATIC_PLACE,
    ENTITY_TELEPORT,
    BLOCKS_RECALCULATE,
    ENTITIES_RECALCULATE,
    FIND_SAFE_SPOT,
    API_REQUEST,
    BIOME_REQUEST,
    WARP_SIGN_BREAK

}
