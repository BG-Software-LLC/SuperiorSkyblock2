package com.bgsoftware.superiorskyblock.island.container.grid;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;

public interface IslandsGrid {

    void addIsland(String worldName, long packedPos, Island island);

    @Nullable
    Island removeIslandAt(String worldName, long packedPos);

    @Nullable
    Island getIslandAt(String worldName, long packedPos);

}
