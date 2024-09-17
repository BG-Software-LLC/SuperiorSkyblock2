package com.bgsoftware.superiorskyblock.core.stats;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.google.gson.JsonObject;

public class StatsIslandsCounter implements IStatsCollector {

    public static final StatsIslandsCounter INSTANCE = new StatsIslandsCounter();

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private StatsIslandsCounter() {

    }

    @Override
    public void collect(JsonObject statsObject) {
        statsObject.addProperty("islands_count", plugin.getGrid().getIslands().size());
    }

}
