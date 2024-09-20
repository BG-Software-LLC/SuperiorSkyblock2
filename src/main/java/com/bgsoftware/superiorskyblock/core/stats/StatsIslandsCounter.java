package com.bgsoftware.superiorskyblock.core.stats;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.google.gson.JsonObject;

public class StatsIslandsCounter implements IStatsCollector {

    public static final StatsIslandsCounter INSTANCE = new StatsIslandsCounter();

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private int lastIslandsCount = 0;

    private StatsIslandsCounter() {

    }

    @Override
    public void collect(JsonObject statsObject) {
        int currentIslandsCount = plugin.getGrid().getIslands().size();
        if (currentIslandsCount != this.lastIslandsCount) {
            statsObject.addProperty("islands_count", currentIslandsCount);
            this.lastIslandsCount = currentIslandsCount;
        }
    }

}
