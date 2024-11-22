package com.bgsoftware.superiorskyblock.island.top.metadata;

import com.bgsoftware.superiorskyblock.api.island.Island;

public class IslandSortRatingMetadata extends IslandSortMetadata<IslandSortRatingMetadata> {

    private final int ratingAmount;
    private final double compareValue;

    public IslandSortRatingMetadata(Island island) {
        super(island);
        this.ratingAmount = island.getRatingAmount();
        this.compareValue = island.getTotalRating() * ratingAmount;
    }

    @Override
    public int compareTo(IslandSortRatingMetadata o) {
        int compare = Double.compare(o.compareValue, this.compareValue);
        if (compare == 0) {
            compare = Integer.compare(o.ratingAmount, this.ratingAmount);
            if (compare == 0)
                compare = super.compareTo(o);
        }
        return compare;
    }

}
