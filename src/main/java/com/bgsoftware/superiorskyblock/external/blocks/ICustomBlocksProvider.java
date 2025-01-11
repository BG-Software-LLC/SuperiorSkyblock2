package com.bgsoftware.superiorskyblock.external.blocks;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;

public interface ICustomBlocksProvider {

    @Nullable
    KeyMap<Integer> getBlockCountsForChunk(ChunkPosition chunkPosition);

}
