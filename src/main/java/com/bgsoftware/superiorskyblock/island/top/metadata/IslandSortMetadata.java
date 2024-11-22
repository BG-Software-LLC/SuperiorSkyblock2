package com.bgsoftware.superiorskyblock.island.top.metadata;

import com.bgsoftware.superiorskyblock.api.island.Island;
import org.jetbrains.annotations.NotNull;

public abstract class IslandSortMetadata<T extends IslandSortMetadata<T>> implements Comparable<T> {

    private final Island island;
    protected final String islandName;

    protected IslandSortMetadata(Island island) {
        this.island = island;
        this.islandName = island.getName().isEmpty() ? island.getOwner().getName() : island.getName();
    }

    public Island getIsland() {
        return island;
    }

    @Override
    public int compareTo(@NotNull T o) {
        return this.islandName.compareTo(o.islandName);
    }

}
