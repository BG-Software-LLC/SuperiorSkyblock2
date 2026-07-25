package com.bgsoftware.superiorskyblock.core.stats;

import com.google.gson.JsonObject;

public interface IStatsCollector {

    void collect(JsonObject statsObject);

}
