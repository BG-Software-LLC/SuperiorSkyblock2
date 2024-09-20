package com.bgsoftware.superiorskyblock.core.stats;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;

public class StatsPlayersCounter implements IStatsCollector {

    public static final StatsPlayersCounter INSTANCE = new StatsPlayersCounter();

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private int lastOnlinePlayers;
    private int lastAllPlayers;

    private StatsPlayersCounter() {

    }

    @Override
    public void collect(JsonObject statsObject) {
        int currentOnlinePlayers = Bukkit.getOnlinePlayers().size();
        int currentAllPlayers = plugin.getPlayers().getAllPlayers().size();

        if (currentOnlinePlayers != this.lastOnlinePlayers) {
            statsObject.addProperty("online_players", currentOnlinePlayers);
            this.lastOnlinePlayers = currentOnlinePlayers;
        }

        if (currentAllPlayers != this.lastAllPlayers) {
            statsObject.addProperty("all_players", currentAllPlayers);
            this.lastAllPlayers = currentAllPlayers;
        }
    }
}
