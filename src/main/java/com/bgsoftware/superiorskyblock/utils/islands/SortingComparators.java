package com.bgsoftware.superiorskyblock.utils.islands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;

import java.util.Comparator;
import java.util.UUID;

public final class SortingComparators {

    private final static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private final static Comparator<Island> namesComparator = (o1, o2) -> {
        String firstName = o1.getName().isEmpty() ? o1.getOwner().getName() : o1.getName();
        String secondName = o2.getName().isEmpty() ? o2.getOwner().getName() : o2.getName();
        return firstName.compareTo(secondName);
    };

    public final static Comparator<UUID> WORTH_COMPARATOR = (o1, o2) -> {
        Island firstIsland = plugin.getGrid().getIsland(o1), secondIsland = plugin.getGrid().getIsland(o2);
        int compare = secondIsland.getWorthAsBigDecimal().compareTo(firstIsland.getWorthAsBigDecimal());
        return compare == 0 ? namesComparator.compare(firstIsland, secondIsland) : compare;
    };

    public final static Comparator<UUID> LEVEL_COMPARATOR = (o1, o2) -> {
        Island firstIsland = plugin.getGrid().getIsland(o1), secondIsland = plugin.getGrid().getIsland(o2);
        int compare = secondIsland.getIslandLevelAsBigDecimal().compareTo(firstIsland.getIslandLevelAsBigDecimal());
        return compare == 0 ? namesComparator.compare(firstIsland, secondIsland) : compare;
    };

    public final static Comparator<UUID> RATING_COMPARATOR = (o1, o2) -> {
        Island firstIsland = plugin.getGrid().getIsland(o1), secondIsland = plugin.getGrid().getIsland(o2);
        int totalRatingsCompare = Double.compare(secondIsland.getTotalRating(), firstIsland.getTotalRating());

        if(totalRatingsCompare == 0){
            int ratingsAmountCompare = Integer.compare(secondIsland.getRatingAmount(), firstIsland.getRatingAmount());
            return ratingsAmountCompare == 0 ? namesComparator.compare(firstIsland, secondIsland) : ratingsAmountCompare;
        }

        return totalRatingsCompare;
    };

    public final static Comparator<UUID> PLAYERS_COMPARATOR = (o1, o2) -> {
        Island firstIsland = plugin.getGrid().getIsland(o1), secondIsland = plugin.getGrid().getIsland(o2);
        int compare = Integer.compare(secondIsland.allPlayersInside().size(), firstIsland.allPlayersInside().size());
        return compare == 0 ? namesComparator.compare(firstIsland, secondIsland) : compare;
    };

}
