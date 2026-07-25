package com.bgsoftware.superiorskyblock.island.top.metadata;

import com.bgsoftware.superiorskyblock.api.island.Island;

import java.math.BigDecimal;

public class IslandSortValueMetadata extends IslandSortMetadata<IslandSortValueMetadata> {

    private final BigDecimal value;

    public IslandSortValueMetadata(Island island, BigDecimal value) {
        super(island);
        this.value = value;
    }

    @Override
    public int compareTo(IslandSortValueMetadata o) {
        int compare = o.value.compareTo(this.value);
        return compare == 0 ? super.compareTo(o) : compare;
    }

}
