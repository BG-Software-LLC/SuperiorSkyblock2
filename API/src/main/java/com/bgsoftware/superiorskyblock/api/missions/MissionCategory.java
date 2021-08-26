package com.bgsoftware.superiorskyblock.api.missions;

import java.util.List;

public interface MissionCategory {

    /**
     * Get the name of the category.
     */
    String getName();

    /**
     * Get the slot of the category in the missions menu.
     */
    int getSlot();

    /**
     * Get all the missions in the category.
     */
    List<Mission<?>> getMissions();

}
