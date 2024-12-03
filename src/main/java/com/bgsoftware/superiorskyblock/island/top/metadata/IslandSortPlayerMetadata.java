package com.bgsoftware.superiorskyblock.island.top.metadata;

import com.bgsoftware.superiorskyblock.api.island.Island;

public class IslandSortPlayerMetadata extends IslandSortMetadata<IslandSortPlayerMetadata> {

    private final int allPlayersCount;

    public IslandSortPlayerMetadata(Island island) {
        super(island);
        this.allPlayersCount = island.getAllPlayersInside().size();
    }

    @Override
    public int compareTo(IslandSortPlayerMetadata o) {
        int compare = Integer.compare(o.allPlayersCount, this.allPlayersCount);
        return compare == 0 ? super.compareTo(o) : compare;
    }

}
