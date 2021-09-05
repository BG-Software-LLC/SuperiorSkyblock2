package com.bgsoftware.superiorskyblock.utils.islands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.SortingType;

public final class SortingTypes {

    private SortingTypes(){

    }

    public static SortingType BY_WORTH = SortingType.getByName("WORTH");
    public static SortingType BY_LEVEL = SortingType.getByName("LEVEL");
    public static SortingType BY_RATING = SortingType.getByName("RATING");
    public static SortingType BY_PLAYERS = SortingType.getByName("PLAYERS");

    public static SortingType getDefaultSorting(){
        return SortingType.getByName(SuperiorSkyblockPlugin.getPlugin().getSettings().getIslandTopOrder());
    }

}
