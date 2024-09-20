package com.bgsoftware.superiorskyblock.core.stats;

import com.bgsoftware.superiorskyblock.core.profiler.ProfilerSession;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StatsProfilers implements IStatsCollector {

    public static final StatsProfilers INSTANCE = new StatsProfilers();

    private final List<ProfilerSession> collectedProfilers = new LinkedList<>();

    private StatsProfilers() {

    }

    public void submitProfiler(ProfilerSession session) {
        synchronized (this) {
            collectedProfilers.add(session);
        }
    }

    @Override
    public void collect(JsonObject statsObject) {
        List<ProfilerSession> collectedProfilers;

        synchronized (this) {
            if (this.collectedProfilers.isEmpty())
                return;

            collectedProfilers = new LinkedList<>(this.collectedProfilers);
            this.collectedProfilers.clear();
        }

        JsonArray profilers = new JsonArray();

        collectedProfilers.forEach(session -> {
            JsonObject profiler = new JsonObject();
            profiler.addProperty("type", session.getProfileType().name());
            profiler.addProperty("time_elapsed",
                    TimeUnit.NANOSECONDS.toMillis(session.getEndData().time - session.getStartData().time));
            profiler.addProperty("start_tps", session.getStartData().tps);
            profiler.addProperty("end_tps", session.getEndData().tps);
            Object extra = session.getExtra();
            if (extra != null)
                profiler.addProperty("extra", String.valueOf(extra));
            profilers.add(profiler);
        });

        statsObject.add("profilers", profilers);
    }


}
