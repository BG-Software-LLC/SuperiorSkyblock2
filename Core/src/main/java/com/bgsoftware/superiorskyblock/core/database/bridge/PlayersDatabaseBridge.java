package com.bgsoftware.superiorskyblock.core.database.bridge;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.data.DatabaseFilter;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.database.DBColumn;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class PlayersDatabaseBridge {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Map<UUID, Map<FutureSave, Set<Object>>> SAVE_METHODS_TO_BE_EXECUTED = new ConcurrentHashMap<>();
    private static final LazyReference<DatabaseBridge> GLOBAL_PLAYERS_BRIDGE = new LazyReference<DatabaseBridge>() {
        @Override
        protected DatabaseBridge create() {
            DatabaseBridge databaseBridge = plugin.getFactory().createDatabaseBridge((SuperiorPlayer) null);
            databaseBridge.setDatabaseBridgeMode(DatabaseBridgeMode.SAVE_DATA);
            return databaseBridge;
        }
    };

    private PlayersDatabaseBridge() {
    }

    public static DatabaseBridge getGlobalPlayersBridge() {
        return GLOBAL_PLAYERS_BRIDGE.get();
    }

    public static void saveTextureValue(SuperiorPlayer superiorPlayer) {
        updatePlayerValue(superiorPlayer, "last_used_skin", superiorPlayer.getTextureValue());
    }

    public static void savePlayerName(SuperiorPlayer superiorPlayer) {
        updatePlayerValue(superiorPlayer, "last_used_name", superiorPlayer.getName());
    }

    public static void saveUserLocale(SuperiorPlayer superiorPlayer) {
        Locale userLocale = superiorPlayer.getUserLocale();
        updatePlayerSettingsValue(superiorPlayer, "language", userLocale.getLanguage() + "-" + userLocale.getCountry());
    }

    public static void saveToggledBorder(SuperiorPlayer superiorPlayer) {
        updatePlayerSettingsValue(superiorPlayer, "toggled_border", superiorPlayer.hasWorldBorderEnabled());
    }

    public static void saveDisbands(SuperiorPlayer superiorPlayer) {
        updatePlayerValue(superiorPlayer, "disbands", superiorPlayer.getDisbands());
    }

    public static void saveToggledPanel(SuperiorPlayer superiorPlayer) {
        updatePlayerSettingsValue(superiorPlayer, "toggled_panel", superiorPlayer.hasToggledPanel());
    }

    public static void saveIslandFly(SuperiorPlayer superiorPlayer) {
        updatePlayerSettingsValue(superiorPlayer, "island_fly", superiorPlayer.hasIslandFlyEnabled());
    }

    public static void saveBorderColor(SuperiorPlayer superiorPlayer) {
        updatePlayerSettingsValue(superiorPlayer, "border_color", superiorPlayer.getBorderColor().name());
    }

    public static void saveLastTimeStatus(SuperiorPlayer superiorPlayer) {
        updatePlayerValue(superiorPlayer, "last_time_updated", superiorPlayer.getLastTimeStatus());
    }

    public static void saveMission(SuperiorPlayer superiorPlayer, Mission<?> mission, int finishCount) {
        runOperationIfRunning(superiorPlayer.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("players_missions",
                        pool.obtain().withNameAndValue("player", superiorPlayer.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("name", mission.getName().toLowerCase(Locale.ENGLISH)),
                        pool.obtain().withNameAndValue("finish_count", finishCount)
                );
            }
        });
    }

    public static void removeMission(SuperiorPlayer superiorPlayer, Mission<?> mission) {
        runOperationIfRunning(superiorPlayer.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                DBColumn column = pool.obtain().withNameAndValue("name", mission.getName().toLowerCase(Locale.ENGLISH));
                databaseBridge.deleteObject("players_missions", createFilter(pool, "player", superiorPlayer, column));
            }
        });
    }

    public static void savePersistentDataContainer(SuperiorPlayer superiorPlayer) {
        runOperationIfRunning(superiorPlayer.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("players_custom_data",
                        pool.obtain().withNameAndValue("player", superiorPlayer.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("data", superiorPlayer.getPersistentDataContainer().serialize())
                );
            }
        });
    }

    public static void removePersistentDataContainer(SuperiorPlayer superiorPlayer) {
        runOperationIfRunning(superiorPlayer.getDatabaseBridge(), databaseBridge ->
                databaseBridge.deleteObject("players_custom_data", createFilter("player", superiorPlayer)));
    }

    public static void insertPlayer(SuperiorPlayer superiorPlayer) {
        runOperationIfRunning(superiorPlayer.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("players",
                        pool.obtain().withNameAndValue("uuid", superiorPlayer.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("last_used_name", superiorPlayer.getName()),
                        pool.obtain().withNameAndValue("last_used_skin", superiorPlayer.getTextureValue()),
                        pool.obtain().withNameAndValue("disbands", superiorPlayer.getDisbands()),
                        pool.obtain().withNameAndValue("last_time_updated", superiorPlayer.getLastTimeStatus())
                );
            }
        });
        insertPlayerSettings(superiorPlayer);
    }

    public static void insertPlayerSettings(SuperiorPlayer superiorPlayer) {
        Locale userLocale = superiorPlayer.getUserLocale();
        runOperationIfRunning(superiorPlayer.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("players_settings",
                        pool.obtain().withNameAndValue("player", superiorPlayer.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("language", userLocale.getLanguage() + "-" + userLocale.getCountry()),
                        pool.obtain().withNameAndValue("toggled_panel", superiorPlayer.hasToggledPanel()),
                        pool.obtain().withNameAndValue("border_color", superiorPlayer.getBorderColor().name()),
                        pool.obtain().withNameAndValue("toggled_border", superiorPlayer.hasWorldBorderEnabled()),
                        pool.obtain().withNameAndValue("island_fly", superiorPlayer.hasIslandFlyEnabled())
                );
            }
        });
    }

    public static void replacePlayer(SuperiorPlayer originalPlayer, SuperiorPlayer newPlayer) {
        DatabaseBridge playersReplacer = getGlobalPlayersBridge();

        try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
            DBColumn uuidColumn = pool.obtain().withNameAndValue("uuid", newPlayer.getUniqueId().toString());
            DatabaseFilter uuidFilter = createFilter("uuid", originalPlayer);

            DBColumn playerColumn = pool.obtain().withNameAndValue("player", newPlayer.getUniqueId().toString());
            DatabaseFilter playerFilter = createFilter("player", originalPlayer);

            // We go through all possible tables (both island and players) and replace the player uuids.
            playersReplacer.updateObject("players", uuidFilter, uuidColumn);
            playersReplacer.updateObject("players_settings", playerFilter, playerColumn);


            playersReplacer.updateObject("bank_transactions", playerFilter, playerColumn);
            playersReplacer.updateObject("islands_bans", playerFilter, playerColumn);
            playersReplacer.updateObject("islands_bans", createFilter("banned_by", originalPlayer),
                    pool.obtain().withNameAndValue("banned_by", newPlayer.getUniqueId().toString()));
            playersReplacer.updateObject("islands_player_permissions", playerFilter, playerColumn);
            playersReplacer.updateObject("islands_ratings", playerFilter, playerColumn);
            playersReplacer.updateObject("islands_visitors", playerFilter, playerColumn);

            if (newPlayer.hasIsland()) {
                playersReplacer.updateObject("islands", createFilter("owner", originalPlayer),
                        pool.obtain().withNameAndValue("owner", newPlayer.getUniqueId().toString()));
                playersReplacer.updateObject("islands_members", playerFilter, playerColumn);
            }

            if (!newPlayer.isPersistentDataContainerEmpty())
                playersReplacer.updateObject("players_custom_data", playerFilter, playerColumn);
            if (!newPlayer.getCompletedMissions().isEmpty())
                playersReplacer.updateObject("players_missions", playerFilter, playerColumn);
        }
    }

    public static void deletePlayer(SuperiorPlayer superiorPlayer) {
        DatabaseBridge playersReplacer = getGlobalPlayersBridge();

        DatabaseFilter uuidFilter = createFilter("uuid", superiorPlayer);
        DatabaseFilter playerFilter = createFilter("player", superiorPlayer);

        // We go through all possible tables (both island and players) and delete the player record.
        playersReplacer.deleteObject("players", uuidFilter);
        playersReplacer.deleteObject("players_settings", playerFilter);

        playersReplacer.deleteObject("bank_transactions", playerFilter);
        playersReplacer.deleteObject("islands_bans", playerFilter);
        playersReplacer.deleteObject("islands_bans", createFilter("banned_by", superiorPlayer));
        playersReplacer.deleteObject("islands_player_permissions", playerFilter);
        playersReplacer.deleteObject("islands_ratings", playerFilter);
        playersReplacer.deleteObject("islands_visitors", playerFilter);

        if (superiorPlayer.hasIsland()) {
            playersReplacer.deleteObject("islands", createFilter("owner", superiorPlayer));
            playersReplacer.deleteObject("islands_members", playerFilter);
        }
        if (!superiorPlayer.isPersistentDataContainerEmpty())
            playersReplacer.deleteObject("players_custom_data", playerFilter);
        if (!superiorPlayer.getCompletedMissions().isEmpty())
            playersReplacer.deleteObject("players_missions", playerFilter);
    }

    public static void markPersistentDataContainerToBeSaved(SuperiorPlayer superiorPlayer) {
        Set<Object> varsForPersistentData = SAVE_METHODS_TO_BE_EXECUTED.computeIfAbsent(superiorPlayer.getUniqueId(), u -> new EnumMap<>(FutureSave.class))
                .computeIfAbsent(FutureSave.PERSISTENT_DATA, e -> new HashSet<>());
        if (varsForPersistentData.isEmpty())
            varsForPersistentData.add(new Object());
    }

    public static boolean isModified(SuperiorPlayer superiorPlayer) {
        return SAVE_METHODS_TO_BE_EXECUTED.containsKey(superiorPlayer.getUniqueId());
    }

    public static void executeFutureSaves(SuperiorPlayer superiorPlayer) {
        Map<FutureSave, Set<Object>> futureSaves = SAVE_METHODS_TO_BE_EXECUTED.remove(superiorPlayer.getUniqueId());
        if (futureSaves != null) {
            for (Map.Entry<FutureSave, Set<Object>> futureSaveEntry : futureSaves.entrySet()) {
                switch (futureSaveEntry.getKey()) {
                    case PERSISTENT_DATA: {
                        if (superiorPlayer.isPersistentDataContainerEmpty())
                            removePersistentDataContainer(superiorPlayer);
                        else
                            savePersistentDataContainer(superiorPlayer);
                        break;
                    }
                }
            }
        }
    }

    public static void executeFutureSaves(SuperiorPlayer superiorPlayer, FutureSave futureSave) {
        Map<FutureSave, Set<Object>> futureSaves = SAVE_METHODS_TO_BE_EXECUTED.get(superiorPlayer.getUniqueId());

        if (futureSaves == null)
            return;

        Set<Object> values = futureSaves.remove(futureSave);

        if (values == null)
            return;

        if (futureSaves.isEmpty())
            SAVE_METHODS_TO_BE_EXECUTED.remove(superiorPlayer.getUniqueId());

        switch (futureSave) {
            case PERSISTENT_DATA: {
                if (superiorPlayer.isPersistentDataContainerEmpty())
                    removePersistentDataContainer(superiorPlayer);
                else
                    savePersistentDataContainer(superiorPlayer);
                break;
            }
        }
    }

    private static void updatePlayerValue(SuperiorPlayer superiorPlayer, String columnName, Object value) {
        runOperationIfRunning(superiorPlayer.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Wrapper<DBColumn> wrapper = ObjectsPools.DB_COLUMN.obtain()) {
                DBColumn column = wrapper.getHandle().withNameAndValue(columnName, value);
                databaseBridge.updateObject("players", createFilter("uuid", superiorPlayer), column);
            }
        });
    }

    private static void updatePlayerSettingsValue(SuperiorPlayer superiorPlayer, String columnName, Object value) {
        runOperationIfRunning(superiorPlayer.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Wrapper<DBColumn> wrapper = ObjectsPools.DB_COLUMN.obtain()) {
                DBColumn column = wrapper.getHandle().withNameAndValue(columnName, value);
                databaseBridge.updateObject("players_settings", createFilter("player", superiorPlayer), column);
            }
        });
    }

    private static DatabaseFilter createFilter(String id, SuperiorPlayer superiorPlayer) {
        return DatabaseFilter.fromFilter(id, superiorPlayer.getUniqueId().toString());
    }

    private static DatabaseFilter createFilter(ObjectsPools.Batch<DBColumn> pool, String id, SuperiorPlayer superiorPlayer, DBColumn column) {
        List<Pair<String, Object>> filters = new LinkedList<>();
        filters.add(pool.obtain().withNameAndValue(id, superiorPlayer.getUniqueId().toString()));
        filters.add(column);
        return DatabaseFilter.fromFilters(filters);
    }

    private static void runOperationIfRunning(DatabaseBridge databaseBridge, Consumer<DatabaseBridge> databaseBridgeConsumer) {
        if (databaseBridge.getDatabaseBridgeMode() == DatabaseBridgeMode.SAVE_DATA)
            databaseBridgeConsumer.accept(databaseBridge);
    }

    public enum FutureSave {

        PERSISTENT_DATA

    }

}
