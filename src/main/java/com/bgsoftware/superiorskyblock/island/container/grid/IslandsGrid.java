package com.bgsoftware.superiorskyblock.island.container.grid;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;

public interface IslandsGrid {

    void addIsland(Dimension dimension, long packedPos, Island island);

    @Nullable
    Island removeIslandAt(Dimension dimension, long packedPos);

    @Nullable
    Island getIslandAt(Dimension dimension, long packedPos);

}
