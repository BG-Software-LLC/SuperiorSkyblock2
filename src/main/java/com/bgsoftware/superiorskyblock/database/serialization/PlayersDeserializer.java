package com.bgsoftware.superiorskyblock.database.serialization;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.database.cache.CachedPlayerInfo;
import com.bgsoftware.superiorskyblock.database.cache.DatabaseCache;
import com.bgsoftware.superiorskyblock.lang.PlayerLocales;

import java.util.UUID;

public final class PlayersDeserializer {

    static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private PlayersDeserializer() {

    }

    public static void deserializeMissions(DatabaseBridge databaseBridge, DatabaseCache<CachedPlayerInfo> databaseCache) {
        databaseBridge.loadAllObjects("players_missions", missionsRow -> {
            UUID uuid = UUID.fromString((String) missionsRow.get("player"));
            CachedPlayerInfo cachedPlayerInfo = databaseCache.addCachedInfo(uuid, new CachedPlayerInfo());

            String name = (String) missionsRow.get("name");
            int finishCount = (int) missionsRow.get("finish_count");

            Mission<?> mission = plugin.getMissions().getMission(name);

            if (mission != null)
                cachedPlayerInfo.completedMissions.put(mission, finishCount);
        });
    }

    public static void deserializePlayerSettings(DatabaseBridge databaseBridge, DatabaseCache<CachedPlayerInfo> databaseCache) {
        databaseBridge.loadAllObjects("players_settings", playerSettingsRow -> {
            DatabaseResult playerSettings = new DatabaseResult(playerSettingsRow);

            UUID uuid = UUID.fromString(playerSettings.getString("player"));
            CachedPlayerInfo cachedPlayerInfo = databaseCache.addCachedInfo(uuid, new CachedPlayerInfo());

            cachedPlayerInfo.toggledPanel = playerSettings.getBoolean("toggled_panel");
            cachedPlayerInfo.islandFly = playerSettings.getBoolean("island_fly");
            cachedPlayerInfo.borderColor = BorderColor.safeValue(playerSettings.getString("border_color"), BorderColor.BLUE);
            cachedPlayerInfo.userLocale = PlayerLocales.getLocale(playerSettings.getString("language"));
            cachedPlayerInfo.worldBorderEnabled = playerSettings.getBoolean("toggled_border");
        });
    }

}
