package com.bgsoftware.superiorskyblock.core.stats;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class StatsClient {

    private static final int VERSION = 1;
    private static final long TASK_DELAY = 1;
    private static final List<IStatsCollector> STATS_COLLECTORS = new LinkedList<>();

    private static StatsClient INSTANCE;

    static {
        STATS_COLLECTORS.add(StatsIslandsCounter.INSTANCE);
        STATS_COLLECTORS.add(StatsPlayersCounter.INSTANCE);
        STATS_COLLECTORS.add(StatsProfilers.INSTANCE);
        STATS_COLLECTORS.add(StatsSchematicsSizes.INSTANCE);
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder()
            .setNameFormat("SuperiorSkyblock Stats").build());

    private ScheduledFuture<?> scheduledTask;

    private StatsClient() {

    }

    public void start() {
        this.scheduledTask = scheduler.scheduleAtFixedRate(this::collectStatsTask, 0, TASK_DELAY, TimeUnit.HOURS);
    }

    public void shutdown() {
        if (this.scheduledTask != null) {
            this.scheduledTask.cancel(true);
            this.scheduledTask = null;
        }

        if (!this.scheduler.isShutdown())
            this.scheduler.shutdownNow();
    }

    private void collectStatsTask() {
        JsonObject statsObject = new JsonObject();
        STATS_COLLECTORS.forEach(collector -> collector.collect(statsObject));
        submitStats(statsObject);
    }

    private void submitStats(JsonObject statsObject) {
        if (statsObject.entrySet().isEmpty())
            return;

        statsObject.addProperty("version", VERSION);

        // TODO: Submit to API
    }

    public static StatsClient getInstance() {
        if (INSTANCE == null)
            INSTANCE = new StatsClient();

        return INSTANCE;
    }

    public static Optional<StatsClient> getIfExists() {
        return Optional.ofNullable(INSTANCE);
    }

}
