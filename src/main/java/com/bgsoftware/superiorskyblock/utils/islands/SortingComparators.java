package com.bgsoftware.superiorskyblock.utils.islands;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;

import java.util.Comparator;
import java.util.UUID;

public final class SortingComparators {

    private final static Comparator<Island> ISLAND_NAMES_COMPARATOR = (o1, o2) -> {
        String firstName = o1.getName().isEmpty() ? o1.getOwner().getName() : o1.getName();
        String secondName = o2.getName().isEmpty() ? o2.getOwner().getName() : o2.getName();
        return firstName.compareTo(secondName);
    };

    private final static Comparator<SuperiorPlayer> PLAYER_NAMES_COMPARATOR = Comparator.comparing(SuperiorPlayer::getName);

    public final static Comparator<Island> WORTH_COMPARATOR = (o1, o2) -> {
        int compare = o2.getWorthAsBigDecimal().compareTo(o1.getWorthAsBigDecimal());
        return compare == 0 ? ISLAND_NAMES_COMPARATOR.compare(o1, o2) : compare;
    };

    public final static Comparator<Island> LEVEL_COMPARATOR = (o1, o2) -> {
        int compare = o2.getIslandLevelAsBigDecimal().compareTo(o1.getIslandLevelAsBigDecimal());
        return compare == 0 ? ISLAND_NAMES_COMPARATOR.compare(o1, o2) : compare;
    };

    public final static Comparator<Island> RATING_COMPARATOR = (o1, o2) -> {
        int totalRatingsCompare = Double.compare(o2.getTotalRating(), o1.getTotalRating());

        if(totalRatingsCompare == 0){
            int ratingsAmountCompare = Integer.compare(o2.getRatingAmount(), o1.getRatingAmount());
            return ratingsAmountCompare == 0 ? ISLAND_NAMES_COMPARATOR.compare(o1, o2) : ratingsAmountCompare;
        }

        return totalRatingsCompare;
    };

    public final static Comparator<Island> PLAYERS_COMPARATOR = (o1, o2) -> {
        int compare = Integer.compare(o2.allPlayersInside().size(), o1.allPlayersInside().size());
        return compare == 0 ? ISLAND_NAMES_COMPARATOR.compare(o1, o2) : compare;
    };

    public final static Comparator<UUID> ISLAND_MEMBERS_COMPARATOR = (o1, o2) -> {
        SuperiorPlayer firstPlayer = SSuperiorPlayer.of(o1), secondPlayer = SSuperiorPlayer.of(o2);
        int compare = Integer.compare(secondPlayer.getPlayerRole().getWeight(), firstPlayer.getPlayerRole().getWeight());
        return compare == 0 ? PLAYER_NAMES_COMPARATOR.compare(firstPlayer, secondPlayer) : compare;
    };

}
