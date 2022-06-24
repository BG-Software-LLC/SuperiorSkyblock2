package com.bgsoftware.superiorskyblock.island.purge;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultIslandsPurger implements IslandsPurger {

    private final Set<Island> scheduledIslands = new HashSet<>();

    @Override
    public void scheduleIslandPurge(Island island) {
        this.scheduledIslands.add(island);
    }

    @Override
    public void unscheduleIslandPurge(Island island) {
        this.scheduledIslands.remove(island);
    }

    @Override
    public boolean isIslandPurgeScheduled(Island island) {
        return this.scheduledIslands.contains(island);
    }

    @Override
    public List<Island> getScheduledPurgedIslands() {
        return new SequentialListBuilder<Island>().build(this.scheduledIslands);
    }

}
