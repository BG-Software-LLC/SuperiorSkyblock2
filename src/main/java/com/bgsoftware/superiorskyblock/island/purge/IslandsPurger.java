package com.bgsoftware.superiorskyblock.island.purge;

import com.bgsoftware.superiorskyblock.api.island.IslandBase;

import java.util.List;

public interface IslandsPurger {

    void scheduleIslandPurge(IslandBase island);

    void unscheduleIslandPurge(IslandBase island);

    boolean isIslandPurgeScheduled(IslandBase island);

    List<IslandBase> getScheduledPurgedIslands();

}
