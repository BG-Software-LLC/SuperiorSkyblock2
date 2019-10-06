package com.bgsoftware.superiorskyblock.utils.islands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;

import java.util.Comparator;
import java.util.UUID;

public final class SortingComparators {

    private final static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private final static Comparator<Island> ISLAND_NAMES_COMPARATOR = (o1, o2) -> {
        String firstName = o1.getName().isEmpty() ? o1.getOwner().getName() : o1.getName();
        String secondName = o2.getName().isEmpty() ? o2.getOwner().getName() : o2.getName();
        return firstName.compareTo(secondName);
    };

    private final static Comparator<SuperiorPlayer> PLAYER_NAMES_COMPARATOR = Comparator.comparing(SuperiorPlayer::getName);

    public final static Comparator<UUID> WORTH_COMPARATOR = (o1, o2) -> {
        Island firstIsland = plugin.getGrid().getIsland(o1), secondIsland = plugin.getGrid().getIsland(o2);
        int compare = secondIsland.getWorthAsBigDecimal().compareTo(firstIsland.getWorthAsBigDecimal());
        return compare == 0 ? ISLAND_NAMES_COMPARATOR.compare(firstIsland, secondIsland) : compare;
    };

    public final static Comparator<UUID> LEVEL_COMPARATOR = (o1, o2) -> {
        Island firstIsland = plugin.getGrid().getIsland(o1), secondIsland = plugin.getGrid().getIsland(o2);
        int compare = secondIsland.getIslandLevelAsBigDecimal().compareTo(firstIsland.getIslandLevelAsBigDecimal());
        return compare == 0 ? ISLAND_NAMES_COMPARATOR.compare(firstIsland, secondIsland) : compare;
    };

    public final static Comparator<UUID> RATING_COMPARATOR = (o1, o2) -> {
        Island firstIsland = plugin.getGrid().getIsland(o1), secondIsland = plugin.getGrid().getIsland(o2);
        int totalRatingsCompare = Double.compare(secondIsland.getTotalRating(), firstIsland.getTotalRating());

        if(totalRatingsCompare == 0){
            int ratingsAmountCompare = Integer.compare(secondIsland.getRatingAmount(), firstIsland.getRatingAmount());
            return ratingsAmountCompare == 0 ? ISLAND_NAMES_COMPARATOR.compare(firstIsland, secondIsland) : ratingsAmountCompare;
        }

        return totalRatingsCompare;
    };

    public final static Comparator<UUID> PLAYERS_COMPARATOR = (o1, o2) -> {
        Island firstIsland = plugin.getGrid().getIsland(o1), secondIsland = plugin.getGrid().getIsland(o2);
        int compare = Integer.compare(secondIsland.allPlayersInside().size(), firstIsland.allPlayersInside().size());
        return compare == 0 ? ISLAND_NAMES_COMPARATOR.compare(firstIsland, secondIsland) : compare;
    };

    public final static Comparator<UUID> ISLAND_MEMBERS_COMPARATOR = (o1, o2) -> {
        SuperiorPlayer firstPlayer = SSuperiorPlayer.of(o1), secondPlayer = SSuperiorPlayer.of(o2);
        int compare = Integer.compare(secondPlayer.getPlayerRole().getWeight(), firstPlayer.getPlayerRole().getWeight());
        return compare == 0 ? PLAYER_NAMES_COMPARATOR.compare(firstPlayer, secondPlayer) : compare;
    };

}
