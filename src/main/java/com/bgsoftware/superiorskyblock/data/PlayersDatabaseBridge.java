package com.bgsoftware.superiorskyblock.data;

import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.islands.IslandSerializer;

import java.util.Locale;

@SuppressWarnings("unchecked")
public final class PlayersDatabaseBridge {

    private PlayersDatabaseBridge(){
    }

    public static void saveTextureValue(SuperiorPlayer superiorPlayer){
        superiorPlayer.getDatabaseBridge().updateObject("players",
                new Pair<>("textureValue", superiorPlayer.getTextureValue()));
    }

    public static void savePlayerName(SuperiorPlayer superiorPlayer){
        superiorPlayer.getDatabaseBridge().updateObject("players", new Pair<>("name", superiorPlayer.getName()));
    }

    public static void saveUserLocale(SuperiorPlayer superiorPlayer){
        Locale userLocale = superiorPlayer.getUserLocale();
        superiorPlayer.getDatabaseBridge().updateObject("players", new Pair<>("language",
                userLocale.getLanguage() + "-" + userLocale.getCountry()));
    }

    public static void saveIslandLeader(SuperiorPlayer superiorPlayer) {
        superiorPlayer.getDatabaseBridge().updateObject("players",
                new Pair<>("teamLeader", superiorPlayer.getIslandLeader().getUniqueId().toString()));
    }

    public static void savePlayerRole(SuperiorPlayer superiorPlayer) {
        superiorPlayer.getDatabaseBridge().updateObject("players",
                new Pair<>("islandRole", superiorPlayer.getPlayerRole().getId() + ""));
    }

    public static void saveToggledBorder(SuperiorPlayer superiorPlayer) {
        superiorPlayer.getDatabaseBridge().updateObject("players",
                new Pair<>("toggledBorder", superiorPlayer.hasWorldBorderEnabled()));
    }

    public static void saveDisbands(SuperiorPlayer superiorPlayer) {
        superiorPlayer.getDatabaseBridge().updateObject("players",
                new Pair<>("disbands", superiorPlayer.getDisbands()));
    }

    public static void saveToggledPanel(SuperiorPlayer superiorPlayer) {
        superiorPlayer.getDatabaseBridge().updateObject("players",
                new Pair<>("toggledPanel", superiorPlayer.hasToggledPanel()));
    }

    public static void saveIslandFly(SuperiorPlayer superiorPlayer) {
        superiorPlayer.getDatabaseBridge().updateObject("players",
                new Pair<>("islandFly", superiorPlayer.hasIslandFlyEnabled()));
    }

    public static void saveBorderColor(SuperiorPlayer superiorPlayer) {
        superiorPlayer.getDatabaseBridge().updateObject("players",
                new Pair<>("borderColor", superiorPlayer.getBorderColor().name()));
    }

    public static void saveLastTimeStatus(SuperiorPlayer superiorPlayer) {
        superiorPlayer.getDatabaseBridge().updateObject("players",
                new Pair<>("lastTimeStatus", superiorPlayer.getLastTimeStatus() + ""));
    }

    public static void saveMissions(SuperiorPlayer superiorPlayer) {
        superiorPlayer.getDatabaseBridge().updateObject("players", new Pair<>("missions",
                IslandSerializer.serializeMissions(superiorPlayer.getCompletedMissionsWithAmounts())));
    }

    public static void insertPlayer(SuperiorPlayer superiorPlayer){
        Locale userLocale = superiorPlayer.getUserLocale();
        superiorPlayer.getDatabaseBridge().insertObject("players",
                new Pair<>("player", superiorPlayer.getUniqueId().toString()),
                new Pair<>("teamLeader", superiorPlayer.getIslandLeader().getUniqueId().toString()),
                new Pair<>("name", superiorPlayer.getName()),
                new Pair<>("islandRole", superiorPlayer.getPlayerRole().getId() + ""),
                new Pair<>("textureValue", superiorPlayer.getTextureValue()),
                new Pair<>("disbands", superiorPlayer.getDisbands()),
                new Pair<>("toggledPanel", superiorPlayer.hasToggledPanel()),
                new Pair<>("islandFly", superiorPlayer.hasIslandFlyEnabled()),
                new Pair<>("borderColor", superiorPlayer.getBorderColor().name()),
                new Pair<>("lastTimeStatus", superiorPlayer.getLastTimeStatus() + ""),
                new Pair<>("missions", IslandSerializer.serializeMissions(superiorPlayer.getCompletedMissionsWithAmounts())),
                new Pair<>("language", userLocale.getLanguage() + "-" + userLocale.getCountry()),
                new Pair<>("toggledBorder", superiorPlayer.hasWorldBorderEnabled())
        );
    }

    public static void updatePlayer(SuperiorPlayer superiorPlayer){
        Locale userLocale = superiorPlayer.getUserLocale();
        superiorPlayer.getDatabaseBridge().updateObject("players",
                new Pair<>("teamLeader", superiorPlayer.getIslandLeader().getUniqueId().toString()),
                new Pair<>("name", superiorPlayer.getName()),
                new Pair<>("islandRole", superiorPlayer.getPlayerRole().getId() + ""),
                new Pair<>("textureValue", superiorPlayer.getTextureValue()),
                new Pair<>("disbands", superiorPlayer.getDisbands()),
                new Pair<>("toggledPanel", superiorPlayer.hasToggledPanel()),
                new Pair<>("islandFly", superiorPlayer.hasIslandFlyEnabled()),
                new Pair<>("borderColor", superiorPlayer.getBorderColor().name()),
                new Pair<>("lastTimeStatus", superiorPlayer.getLastTimeStatus() + ""),
                new Pair<>("missions", IslandSerializer.serializeMissions(superiorPlayer.getCompletedMissionsWithAmounts())),
                new Pair<>("language", userLocale.getLanguage() + "-" + userLocale.getCountry()),
                new Pair<>("toggledBorder", superiorPlayer.hasWorldBorderEnabled())
        );
    }

    public static void deletePlayer(SuperiorPlayer superiorPlayer){
        superiorPlayer.getDatabaseBridge().deleteObject("players");
    }

}
