package com.bgsoftware.superiorskyblock.api.island.level;

import com.bgsoftware.superiorskyblock.api.island.IslandBase;
import com.bgsoftware.superiorskyblock.api.island.Island;

public class IslandLoadLevel<T extends IslandBase> {

    public static IslandLoadLevel<IslandBase> BASE_LOAD = new IslandLoadLevel<>(IslandBase.class);
    public static IslandLoadLevel<Island> FULL_LOAD = new IslandLoadLevel<>(Island.class);

    private final Class<T> islandType;

    public IslandLoadLevel(Class<T> islandType) {
        this.islandType = islandType;
    }

    public Class<T> getIslandType() {
        return islandType;
    }

}
