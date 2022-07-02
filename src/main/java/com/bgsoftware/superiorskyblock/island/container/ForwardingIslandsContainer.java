package com.bgsoftware.superiorskyblock.island.container;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandBase;
import com.bgsoftware.superiorskyblock.api.island.container.IslandsContainer;
import com.bgsoftware.superiorskyblock.api.island.level.IslandLoadLevel;

public class ForwardingIslandsContainer extends IslandsContainerAccess {

    public ForwardingIslandsContainer(IslandsContainer islandsContainer) {
        super(islandsContainer);
    }

    @Override
    protected <T extends IslandBase> T loadIsland(IslandBase island, IslandLoadLevel<T> loadLevel) {
        assert island instanceof Island;
        return (T) island;
    }

}
