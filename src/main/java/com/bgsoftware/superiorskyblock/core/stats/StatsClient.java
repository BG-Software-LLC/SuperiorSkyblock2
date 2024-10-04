package com.bgsoftware.superiorskyblock.core.stats;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class StatsClient {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Gson GSON = new Gson();

    private static final int VERSION = 1;
    private static final long TASK_DELAY = 1;
    private static final byte MINIMUM_ONLINE_PLAYERS = 10;
    private static final List<IStatsCollector> STATS_COLLECTORS = new LinkedList<>();

    private static final String API_ENDPOINT = "https://api.bg-software.com/v1/stats/";

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
        this.scheduledTask = scheduler.scheduleAtFixedRate(this::collectStatsTask, 1, TASK_DELAY, TimeUnit.MINUTES);
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
        UUID serverUUID = plugin.getGrid().getServerUUID();
        if (serverUUID == null) {
            shutdown();
            return;
        }

        if (Bukkit.getOnlinePlayers().size() < MINIMUM_ONLINE_PLAYERS)
            return;

        try {
            JsonObject statsObject = new JsonObject();
            STATS_COLLECTORS.forEach(collector -> collector.collect(statsObject));
            submitStats(statsObject, serverUUID);
        } catch (Exception error) {
            Log.error(error, "An error occurred while uploading stats:");
        }
    }

    private void submitStats(JsonObject statsObject, UUID serverUUID) throws IOException {
        if (statsObject.entrySet().isEmpty())
            return;

        statsObject.addProperty("version", VERSION);
        statsObject.addProperty("server", serverUUID.toString());

        HttpsURLConnection conn = (HttpsURLConnection) new URL(API_ENDPOINT).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()))) {
            writer.write(GSON.toJson(statsObject));
        }

        int statusCode = conn.getResponseCode();
        if (statusCode != 200)
            throw new RuntimeException("Received error code when submitting stats: " + statusCode);

        JsonObject response;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            response = GSON.fromJson(reader.readLine(), JsonObject.class);
        }

        if (response.has("error"))
            throw new RuntimeException("Received error when submitting stats: " + response.get("error").getAsString());

        if (!response.has("status"))
            throw new RuntimeException("Received invalid response when submitting stats: " + response);

        String status = response.get("status").getAsString();
        if (!status.equalsIgnoreCase("success"))
            throw new RuntimeException("Received invalid status when submitting stats: " + status);
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
