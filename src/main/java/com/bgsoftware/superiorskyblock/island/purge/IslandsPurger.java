package com.bgsoftware.superiorskyblock.island.purge;

import com.bgsoftware.superiorskyblock.api.island.Island;

import java.util.List;

public interface IslandsPurger {

    void scheduleIslandPurge(Island island);

    void unscheduleIslandPurge(Island island);

    boolean isIslandPurgeScheduled(Island island);

    List<Island> getScheduledPurgedIslands();

}
