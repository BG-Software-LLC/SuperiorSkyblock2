package com.bgsoftware.superiorskyblock.data;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.Map;
import java.util.function.Consumer;

public final class PlayersDeserializer {

    static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private PlayersDeserializer() {

    }

    public static void deserializeMissions(SuperiorPlayer superiorPlayer, Map<Mission<?>, Integer> completedMissions) {
        superiorPlayer.getDatabaseBridge().loadObject("players_missions", missionsRow -> {
            String name = (String) missionsRow.get("name");
            int finishCount = (int) missionsRow.get("finish_count");

            Mission<?> mission = plugin.getMissions().getMission(name);

            if (mission != null)
                completedMissions.put(mission, finishCount);
        });
    }

    public static void deserializePlayerSettings(SuperiorPlayer superiorPlayer, Consumer<Map<String, Object>> playerSettingsConsumer){
        superiorPlayer.getDatabaseBridge().loadObject("players_settings", playerSettingsConsumer);
    }

}
