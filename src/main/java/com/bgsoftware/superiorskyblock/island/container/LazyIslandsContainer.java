package com.bgsoftware.superiorskyblock.island.container;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.IslandBase;
import com.bgsoftware.superiorskyblock.api.island.container.IslandsContainer;
import com.bgsoftware.superiorskyblock.api.island.level.IslandLoadLevel;
import com.bgsoftware.superiorskyblock.island.cache.IslandsCache;
import com.google.common.base.Preconditions;

public class LazyIslandsContainer extends IslandsContainerAccess {

    private final SuperiorSkyblockPlugin plugin;
    private final IslandsCache islandsCache;

    public LazyIslandsContainer(SuperiorSkyblockPlugin plugin, IslandsContainer islandsContainer, IslandsCache islandsCache) {
        super(islandsContainer);
        this.plugin = plugin;
        this.islandsCache = islandsCache;
    }

    @Override
    protected <T extends IslandBase> T loadIsland(IslandBase islandBase, IslandLoadLevel<T> loadLevel) {
        Preconditions.checkNotNull(islandBase, "Cannot load null island.");
        Class<T> islandType = loadLevel.getIslandType();

        if (islandType.isAssignableFrom(islandBase.getClass()))
            return islandType.cast(islandBase);

        if (islandsCache == null)
            throw new IllegalStateException();

        T island = islandsCache.loadIsland(islandBase, loadLevel);

        plugin.getEventsBus().callIslandLoadEvent(islandBase, island, loadLevel);

        return island;
    }

}
