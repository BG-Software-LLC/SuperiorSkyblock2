package com.bgsoftware.superiorskyblock.database.deserializer;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseFilter;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public final class PlayersDeserializer {

    static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private PlayersDeserializer() {

    }

    public static void deserializeMissions(SuperiorPlayer superiorPlayer, Map<Mission<?>, Integer> completedMissions) {
        loadObject(superiorPlayer, "players_missions", missionsRow -> {
            String name = (String) missionsRow.get("name");
            int finishCount = (int) missionsRow.get("finish_count");

            Mission<?> mission = plugin.getMissions().getMission(name);

            if (mission != null)
                completedMissions.put(mission, finishCount);
        });
    }

    public static void deserializePlayerSettings(SuperiorPlayer superiorPlayer, Consumer<Map<String, Object>> playerSettingsConsumer){
        loadObject(superiorPlayer, "players_settings", playerSettingsConsumer);
    }

    private static void loadObject(SuperiorPlayer superiorPlayer, String table, Consumer<Map<String, Object>> resultConsumer){
        superiorPlayer.getDatabaseBridge().loadObject(table,
                new DatabaseFilter(Collections.singletonList(new Pair<>("player", superiorPlayer.getUniqueId().toString()))),
                resultConsumer);
    }

}
