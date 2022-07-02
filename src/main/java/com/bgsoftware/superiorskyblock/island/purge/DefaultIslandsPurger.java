package com.bgsoftware.superiorskyblock.island.purge;

import com.bgsoftware.superiorskyblock.api.island.IslandBase;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultIslandsPurger implements IslandsPurger {

    private final Set<IslandBase> scheduledIslands = new HashSet<>();

    @Override
    public void scheduleIslandPurge(IslandBase island) {
        this.scheduledIslands.add(island);
    }

    @Override
    public void unscheduleIslandPurge(IslandBase island) {
        this.scheduledIslands.remove(island);
    }

    @Override
    public boolean isIslandPurgeScheduled(IslandBase island) {
        return this.scheduledIslands.contains(island);
    }

    @Override
    public List<IslandBase> getScheduledPurgedIslands() {
        return new SequentialListBuilder<IslandBase>().build(this.scheduledIslands);
    }

}
