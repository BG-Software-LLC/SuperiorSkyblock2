package com.bgsoftware.superiorskyblock.database.bridge;

import com.bgsoftware.superiorskyblock.api.data.DatabaseFilter;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class PlayersDatabaseBridge {

    private PlayersDatabaseBridge(){
    }

    public static void saveTextureValue(SuperiorPlayer superiorPlayer){
        superiorPlayer.getDatabaseBridge().updateObject("players",
                createFilter("uuid", superiorPlayer),
                new Pair<>("last_used_skin", superiorPlayer.getTextureValue()));
    }

    public static void savePlayerName(SuperiorPlayer superiorPlayer){
        superiorPlayer.getDatabaseBridge().updateObject("players",
                createFilter("uuid", superiorPlayer),
                new Pair<>("last_used_name", superiorPlayer.getName()));
    }

    public static void saveUserLocale(SuperiorPlayer superiorPlayer){
        Locale userLocale = superiorPlayer.getUserLocale();
        superiorPlayer.getDatabaseBridge().updateObject("players_settings",
                createFilter("player", superiorPlayer),
                new Pair<>("language", userLocale.getLanguage() + "-" + userLocale.getCountry()));
    }

    public static void saveToggledBorder(SuperiorPlayer superiorPlayer) {
        superiorPlayer.getDatabaseBridge().updateObject("players_settings",
                createFilter("player", superiorPlayer),
                new Pair<>("toggled_border", superiorPlayer.hasWorldBorderEnabled()));
    }

    public static void saveDisbands(SuperiorPlayer superiorPlayer) {
        superiorPlayer.getDatabaseBridge().updateObject("players",
                createFilter("uuid", superiorPlayer),
                new Pair<>("disbands", superiorPlayer.getDisbands()));
    }

    public static void saveToggledPanel(SuperiorPlayer superiorPlayer) {
        superiorPlayer.getDatabaseBridge().updateObject("players_settings",
                createFilter("player", superiorPlayer),
                new Pair<>("toggled_panel", superiorPlayer.hasToggledPanel()));
    }

    public static void saveIslandFly(SuperiorPlayer superiorPlayer) {
        superiorPlayer.getDatabaseBridge().updateObject("players_settings",
                createFilter("player", superiorPlayer),
                new Pair<>("island_fly", superiorPlayer.hasIslandFlyEnabled()));
    }

    public static void saveBorderColor(SuperiorPlayer superiorPlayer) {
        superiorPlayer.getDatabaseBridge().updateObject("players_settings",
                createFilter("player", superiorPlayer),
                new Pair<>("border_color", superiorPlayer.getBorderColor().name()));
    }

    public static void saveLastTimeStatus(SuperiorPlayer superiorPlayer) {
        superiorPlayer.getDatabaseBridge().updateObject("players",
                createFilter("uuid", superiorPlayer),
                new Pair<>("last_time_updated", superiorPlayer.getLastTimeStatus()));
    }

    public static void saveMission(SuperiorPlayer superiorPlayer, Mission<?> mission, int finishCount) {
        superiorPlayer.getDatabaseBridge().insertObject("players_missions",
                new Pair<>("player", superiorPlayer.getUniqueId().toString()),
                new Pair<>("name", mission.getName().toLowerCase()),
                new Pair<>("finish_count", finishCount));
    }

    public static void removeMission(SuperiorPlayer superiorPlayer, Mission<?> mission) {
        superiorPlayer.getDatabaseBridge().deleteObject("players_missions",
                createFilter("player", superiorPlayer, new Pair<>("name", mission.getName().toLowerCase()))
        );
    }

    public static void insertPlayer(SuperiorPlayer superiorPlayer){
        Locale userLocale = superiorPlayer.getUserLocale();

        superiorPlayer.getDatabaseBridge().insertObject("players",
                new Pair<>("uuid", superiorPlayer.getUniqueId().toString()),
                new Pair<>("last_used_name", superiorPlayer.getName()),
                new Pair<>("last_used_skin", superiorPlayer.getTextureValue()),
                new Pair<>("disbands", superiorPlayer.getDisbands()),
                new Pair<>("last_time_updated", superiorPlayer.getLastTimeStatus())
        );

        superiorPlayer.getDatabaseBridge().insertObject("players_settings",
                new Pair<>("player", superiorPlayer.getUniqueId().toString()),
                new Pair<>("language", userLocale.getLanguage() + "-" + userLocale.getCountry()),
                new Pair<>("toggled_panel", superiorPlayer.hasToggledPanel()),
                new Pair<>("border_color", superiorPlayer.getBorderColor().name()),
                new Pair<>("toggled_border", superiorPlayer.hasWorldBorderEnabled()),
                new Pair<>("island_fly", superiorPlayer.hasIslandFlyEnabled())
        );
    }

    public static void updatePlayer(SuperiorPlayer superiorPlayer){
        Locale userLocale = superiorPlayer.getUserLocale();
        superiorPlayer.getDatabaseBridge().updateObject("players",
                createFilter("uuid", superiorPlayer),
                new Pair<>("last_used_name", superiorPlayer.getName()),
                new Pair<>("last_used_skin", superiorPlayer.getTextureValue()),
                new Pair<>("disbands", superiorPlayer.getDisbands()),
                new Pair<>("last_time_updated", superiorPlayer.getLastTimeStatus())
        );

        superiorPlayer.getDatabaseBridge().updateObject("players_settings",
                createFilter("player", superiorPlayer),
                new Pair<>("language", userLocale.getLanguage() + "-" + userLocale.getCountry()),
                new Pair<>("toggled_panel", superiorPlayer.hasToggledPanel()),
                new Pair<>("border_color", superiorPlayer.getBorderColor().name()),
                new Pair<>("toggled_border", superiorPlayer.hasWorldBorderEnabled()),
                new Pair<>("island_fly", superiorPlayer.hasIslandFlyEnabled())
        );

        superiorPlayer.getDatabaseBridge().deleteObject("players_missions",
                createFilter("player", superiorPlayer));

        for(Map.Entry<Mission<?>, Integer> missionEntry : superiorPlayer.getCompletedMissionsWithAmounts().entrySet()){
            saveMission(superiorPlayer, missionEntry.getKey(), missionEntry.getValue());
        }
    }

    public static void deletePlayer(SuperiorPlayer superiorPlayer){
        superiorPlayer.getDatabaseBridge().deleteObject("players", createFilter("uuid", superiorPlayer));
        superiorPlayer.getDatabaseBridge().deleteObject("players_settings", createFilter("player", superiorPlayer));
        superiorPlayer.getDatabaseBridge().deleteObject("players_missions", createFilter("player", superiorPlayer));
    }

    private static DatabaseFilter createFilter(String id, SuperiorPlayer superiorPlayer, Pair<String, Object>... others){
        List<Pair<String, Object>> filters = new ArrayList<>();
        filters.add(new Pair<>(id, superiorPlayer.getUniqueId().toString()));
        if(others != null)
            filters.addAll(Arrays.asList(others));
        return new DatabaseFilter(filters);
    }

}
