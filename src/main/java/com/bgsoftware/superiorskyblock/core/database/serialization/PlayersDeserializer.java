package com.bgsoftware.superiorskyblock.core.database.serialization;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import com.bgsoftware.superiorskyblock.core.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.core.database.cache.CachedPlayerInfo;
import com.bgsoftware.superiorskyblock.core.database.cache.DatabaseCache;

import java.util.Optional;
import java.util.UUID;

public class PlayersDeserializer {

    static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private PlayersDeserializer() {

    }

    public static void deserializeMissions(DatabaseBridge databaseBridge, DatabaseCache<CachedPlayerInfo> databaseCache) {
        databaseBridge.loadAllObjects("players_missions", missionsRow -> {
            DatabaseResult missions = new DatabaseResult(missionsRow);

            Optional<String> player = missions.getString("player");

            if (!player.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load player mission of null player, skipping...");
                return;
            }

            UUID uuid = UUID.fromString(player.get());
            CachedPlayerInfo cachedPlayerInfo = databaseCache.computeIfAbsentInfo(uuid, CachedPlayerInfo::new);

            Optional<String> name = missions.getString("name");

            if (!name.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load player mission of null mission, skipping...");
                return;
            }

            Optional<Integer> finishCount = missions.getInt("finish_count");

            if (!finishCount.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load player mission of invalid finish count, skipping...");
                return;
            }

            Mission<?> mission = plugin.getMissions().getMission(name.get());

            if (mission != null)
                cachedPlayerInfo.completedMissions.put(mission, finishCount.get());
        });
    }

    public static void deserializePlayerSettings(DatabaseBridge databaseBridge, DatabaseCache<CachedPlayerInfo> databaseCache) {
        databaseBridge.loadAllObjects("players_settings", playerSettingsRow -> {
            DatabaseResult playerSettings = new DatabaseResult(playerSettingsRow);

            Optional<String> player = playerSettings.getString("player");

            if (!player.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load player settings of null player, skipping...");
                return;
            }

            UUID uuid = UUID.fromString(player.get());
            CachedPlayerInfo cachedPlayerInfo = databaseCache.computeIfAbsentInfo(uuid, CachedPlayerInfo::new);

            cachedPlayerInfo.toggledPanel = playerSettings.getBoolean("toggled_panel")
                    .orElse(plugin.getSettings().isDefaultToggledPanel());
            cachedPlayerInfo.islandFly = playerSettings.getBoolean("island_fly")
                    .orElse(plugin.getSettings().isDefaultIslandFly());
            cachedPlayerInfo.borderColor = playerSettings.getEnum("border_color", BorderColor.class)
                    .orElse(BorderColor.BLUE);
            cachedPlayerInfo.userLocale = playerSettings.getString("language").map(PlayerLocales::getLocale)
                    .orElse(PlayerLocales.getDefaultLocale());
            cachedPlayerInfo.worldBorderEnabled = playerSettings.getBoolean("toggled_border")
                    .orElse(plugin.getSettings().isDefaultWorldBorder());
        });
    }

    public static void deserializePersistentDataContainer(DatabaseBridge databaseBridge, DatabaseCache<CachedPlayerInfo> databaseCache) {
        databaseBridge.loadAllObjects("players_custom_data", customDataRow -> {
            DatabaseResult customData = new DatabaseResult(customDataRow);

            Optional<UUID> uuid = customData.getUUID("player");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load custom data for null players, skipping...");
                return;
            }

            byte[] persistentData = customData.getBlob("data").orElse(new byte[0]);

            if (persistentData.length == 0)
                return;

            CachedPlayerInfo cachedPlayerInfo = databaseCache.computeIfAbsentInfo(uuid.get(), CachedPlayerInfo::new);
            cachedPlayerInfo.persistentData = persistentData;
        });
    }

}
