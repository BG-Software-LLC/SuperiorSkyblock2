package com.bgsoftware.superiorskyblock.island.top;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;

import java.util.Comparator;

public class SortingTypes {

    public static final SortingType BY_WORTH = register("WORTH", SortingComparators.WORTH_COMPARATOR, false);
    public static final SortingType BY_LEVEL = register("LEVEL", SortingComparators.LEVEL_COMPARATOR, false);
    public static final SortingType BY_RATING = register("RATING", SortingComparators.RATING_COMPARATOR, false);
    public static final SortingType BY_PLAYERS = register("PLAYERS", SortingComparators.PLAYERS_COMPARATOR, false);

    private SortingTypes() {

    }

    public static void registerSortingTypes() {
        // Do nothing, only trigger all the register calls
    }

    private static SortingType register(String name, Comparator<Island> comparator, boolean handleEqualsIslands) {
        SortingType.register(name, comparator, handleEqualsIslands);
        return SortingType.getByName(name);
    }

    public static SortingType getDefaultSorting() {
        return SortingType.getByName(SuperiorSkyblockPlugin.getPlugin().getSettings().getIslandTopOrder());
    }

}
