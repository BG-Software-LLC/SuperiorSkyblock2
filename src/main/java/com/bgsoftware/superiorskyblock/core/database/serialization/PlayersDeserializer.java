package com.bgsoftware.superiorskyblock.core.database.serialization;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.core.database.cache.DatabaseCache;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;

import java.util.Optional;
import java.util.UUID;

public class PlayersDeserializer {

    static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private PlayersDeserializer() {

    }

    public static void deserializeMissions(DatabaseBridge databaseBridge, DatabaseCache<SuperiorPlayer.Builder> databaseCache) {
        databaseBridge.loadAllObjects("players_missions", missionsRow -> {
            DatabaseResult missions = new DatabaseResult(missionsRow);

            Optional<String> player = missions.getString("player");

            if (!player.isPresent()) {
                Log.warn("Cannot load player mission of null player, skipping...");
                return;
            }

            UUID uuid = UUID.fromString(player.get());
            SuperiorPlayer.Builder builder = databaseCache.computeIfAbsentInfo(uuid, SuperiorPlayer::newBuilder);

            Optional<String> name = missions.getString("name");

            if (!name.isPresent()) {
                Log.warn("Cannot load player mission of null mission for ", uuid, ", skipping...");
                return;
            }

            Optional<Integer> finishCount = missions.getInt("finish_count");

            if (!finishCount.isPresent()) {
                Log.warn("Cannot load player mission of invalid finish count for ", uuid, ", skipping...");
                return;
            }

            Mission<?> mission = plugin.getMissions().getMission(name.get());

            if (mission != null)
                builder.setCompletedMission(mission, finishCount.get());
        });
    }

    public static void deserializePlayerSettings(DatabaseBridge databaseBridge, DatabaseCache<SuperiorPlayer.Builder> databaseCache) {
        databaseBridge.loadAllObjects("players_settings", playerSettingsRow -> {
            DatabaseResult playerSettings = new DatabaseResult(playerSettingsRow);

            Optional<String> player = playerSettings.getString("player");

            if (!player.isPresent()) {
                Log.warn("&cCannot load player settings of null player, skipping...");
                return;
            }

            UUID uuid = UUID.fromString(player.get());
            SuperiorPlayer.Builder builder = databaseCache.computeIfAbsentInfo(uuid, SuperiorPlayer::newBuilder);
            playerSettings.getBoolean("toggled_panel").ifPresent(builder::setToggledPanel);
            playerSettings.getBoolean("island_fly").ifPresent(builder::setIslandFly);
            playerSettings.getEnum("border_color", BorderColor.class).ifPresent(builder::setBorderColor);
            playerSettings.getString("language").map(PlayerLocales::getLocale).ifPresent(builder::setLocale);
            playerSettings.getBoolean("toggled_border").ifPresent(builder::setWorldBorderEnabled);
        });
    }

    public static void deserializePersistentDataContainer(DatabaseBridge databaseBridge, DatabaseCache<SuperiorPlayer.Builder> databaseCache) {
        databaseBridge.loadAllObjects("players_custom_data", customDataRow -> {
            DatabaseResult customData = new DatabaseResult(customDataRow);

            Optional<UUID> uuid = customData.getUUID("player");
            if (!uuid.isPresent()) {
                Log.warn("&cCannot load custom data for null players, skipping...");
                return;
            }

            byte[] persistentData = customData.getBlob("data").orElse(new byte[0]);

            if (persistentData.length == 0)
                return;

            SuperiorPlayer.Builder builder = databaseCache.computeIfAbsentInfo(uuid.get(), SuperiorPlayer::newBuilder);
            builder.setPersistentData(persistentData);
        });
    }

}
