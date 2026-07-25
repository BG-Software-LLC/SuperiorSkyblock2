package com.bgsoftware.superiorskyblock.core.stats;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;

public class StatsPlayersCounter implements IStatsCollector {

    public static final StatsPlayersCounter INSTANCE = new StatsPlayersCounter();

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private int lastAllPlayers;

    private StatsPlayersCounter() {

    }

    @Override
    public void collect(JsonObject statsObject) {
        statsObject.addProperty("online_players", Bukkit.getOnlinePlayers().size());

        int currentAllPlayers = plugin.getPlayers().getAllPlayers().size();
        if (currentAllPlayers != this.lastAllPlayers) {
            statsObject.addProperty("all_players", currentAllPlayers);
            this.lastAllPlayers = currentAllPlayers;
        }
    }
}
