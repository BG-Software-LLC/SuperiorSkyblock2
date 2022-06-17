package com.bgsoftware.superiorskyblock.island.top;

import com.bgsoftware.superiorskyblock.api.enums.TopIslandMembersSorting;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SIsland;

import java.util.Comparator;

public class SortingComparators {

    public final static Comparator<SuperiorPlayer> PLAYER_NAMES_COMPARATOR = Comparator.comparing(SuperiorPlayer::getName);
    public final static Comparator<SIsland.UniqueVisitor> PAIRED_PLAYERS_NAMES_COMPARATOR =
            Comparator.comparing(o -> o.getSuperiorPlayer().getName());
    public final static Comparator<BankTransaction> BANK_TRANSACTIONS_COMPARATOR =
            Comparator.comparingInt(BankTransaction::getPosition);
    private final static Comparator<Island> ISLAND_NAMES_COMPARATOR = (o1, o2) -> {
        String firstName = o1.getName().isEmpty() ? o1.getOwner().getName() : o1.getName();
        String secondName = o2.getName().isEmpty() ? o2.getOwner().getName() : o2.getName();
        return firstName.compareTo(secondName);
    };
    public final static Comparator<Island> WORTH_COMPARATOR = (o1, o2) -> {
        int compare = o2.getWorth().compareTo(o1.getWorth());
        return compare == 0 ? ISLAND_NAMES_COMPARATOR.compare(o1, o2) : compare;
    };
    public final static Comparator<Island> LEVEL_COMPARATOR = (o1, o2) -> {
        int compare = o2.getIslandLevel().compareTo(o1.getIslandLevel());
        return compare == 0 ? ISLAND_NAMES_COMPARATOR.compare(o1, o2) : compare;
    };
    public final static Comparator<Island> RATING_COMPARATOR = (o1, o2) -> {
        int totalRatingsCompare = Double.compare(o2.getTotalRating() * o2.getRatingAmount(), o1.getTotalRating() * o1.getRatingAmount());

        if (totalRatingsCompare == 0) {
            int ratingsAmountCompare = Integer.compare(o2.getRatingAmount(), o1.getRatingAmount());
            return ratingsAmountCompare == 0 ? ISLAND_NAMES_COMPARATOR.compare(o1, o2) : ratingsAmountCompare;
        }

        return totalRatingsCompare;
    };
    public final static Comparator<Island> PLAYERS_COMPARATOR = (o1, o2) -> {
        int compare = Integer.compare(o2.getAllPlayersInside().size(), o1.getAllPlayersInside().size());
        return compare == 0 ? ISLAND_NAMES_COMPARATOR.compare(o1, o2) : compare;
    };
    public final static Comparator<SuperiorPlayer> ISLAND_ROLES_COMPARATOR = (o1, o2) -> {
        // Comparison is between o2 and o1 as the lower the weight is, the higher the player is.
        int compare = Integer.compare(o2.getPlayerRole().getWeight(), o1.getPlayerRole().getWeight());
        return compare == 0 ? PLAYER_NAMES_COMPARATOR.compare(o1, o2) : compare;
    };

    private SortingComparators() {

    }

    public static void initializeTopIslandMembersSorting() throws IllegalArgumentException {
        TopIslandMembersSorting.NAMES.setComparator(PLAYER_NAMES_COMPARATOR);
        TopIslandMembersSorting.ROLES.setComparator(ISLAND_ROLES_COMPARATOR);
    }


}
