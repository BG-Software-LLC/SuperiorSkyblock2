package com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.database.transaction.DatabaseTransactionsExecutor;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.SQLDatabase;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0.attributes.BankTransactionsAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0.attributes.GridAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0.attributes.IslandAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0.attributes.IslandChestAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0.attributes.IslandWarpAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0.attributes.PlayerAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0.attributes.StackedBlockAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0.attributes.WarpCategoryAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0.deserializer.EmptyParameterGuardDeserializer;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0.deserializer.IDeserializer;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0.deserializer.JsonDeserializer;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0.deserializer.MultipleDeserializer;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0.deserializer.RawDeserializer;
import com.bgsoftware.superiorskyblock.core.database.sql.ResultSetMapBridge;
import com.bgsoftware.superiorskyblock.core.database.sql.SQLHelper;
import com.bgsoftware.superiorskyblock.core.database.sql.session.QueryResult;
import com.bgsoftware.superiorskyblock.core.database.sql.transaction.CustomSQLDatabaseTransaction;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.mutable.MutableObject;
import com.bgsoftware.superiorskyblock.island.privilege.PlayerPrivilegeNode;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class DatabaseConverter {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final UUID CONSOLE_UUID = new UUID(0, 0);

    private static File databaseFile;
    private static boolean isRemoteDatabase;

    private static final List<PlayerAttributes> loadedPlayers = new ArrayList<>();
    private static final List<IslandAttributes> loadedIslands = new ArrayList<>();
    private static final List<StackedBlockAttributes> loadedBlocks = new ArrayList<>();
    private static final List<BankTransactionsAttributes> loadedBankTransactions = new ArrayList<>();
    private static final IDeserializer deserializer = new MultipleDeserializer(
            EmptyParameterGuardDeserializer.getInstance(),
            JsonDeserializer.INSTANCE,
            RawDeserializer.INSTANCE
    );
    private static GridAttributes gridAttributes;

    private DatabaseConverter() {

    }

    public static void tryConvertDatabase() {
        boolean isOldDatabaseFormat = isDatabaseOldFormat();
        if (isOldDatabaseFormat) {
            convertDatabase();
            saveConvertedData();
        }
    }

    private static void convertDatabase() {
        Log.info("[Database-Converter] Detected old database - starting to convert data...");

        SQLHelper.select("players", "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
            while (resultSet.next()) {
                loadedPlayers.add(loadPlayer(new ResultSetMapBridge(resultSet)));
            }
        }).onFail(QueryResult.PRINT_ERROR));

        Log.info("[Database-Converter] Found ", loadedPlayers.size(), " players in the database.");

        SQLHelper.select("islands", "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
            while (resultSet.next()) {
                loadedIslands.add(loadIsland(new ResultSetMapBridge(resultSet)));
            }
        }).onFail(QueryResult.PRINT_ERROR));

        Log.info("[Database-Converter] Found ", loadedIslands.size(), " islands in the database.");

        SQLHelper.select("stackedBlocks", "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
            while (resultSet.next()) {
                loadedBlocks.add(loadStackedBlock(new ResultSetMapBridge(resultSet)));
            }
        }).onFail(QueryResult.PRINT_ERROR));

        Log.info("[Database-Converter] Found ", loadedBlocks.size(), " stacked blocks in the database.");

        // Ignoring errors as the bankTransactions table may not exist.
        AtomicBoolean foundBankTransaction = new AtomicBoolean(false);
        SQLHelper.select("bankTransactions", "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
            foundBankTransaction.set(true);
            while (resultSet.next()) {
                loadedBankTransactions.add(loadBankTransaction(new ResultSetMapBridge(resultSet)));
            }
        }));

        if (foundBankTransaction.get()) {
            Log.info("[Database-Converter] Found ", loadedBankTransactions.size(), " bank transactions in the database.");
        }

        SQLHelper.select("grid", "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
            if (resultSet.next()) {
                gridAttributes = new GridAttributes()
                        .setValue(GridAttributes.Field.LAST_ISLAND, resultSet.getString("lastIsland"))
                        .setValue(GridAttributes.Field.MAX_ISLAND_SIZE, resultSet.getString("maxIslandSize"))
                        .setValue(GridAttributes.Field.WORLD, resultSet.getString("world"));
            }
        }).onFail(QueryResult.PRINT_ERROR));

        MutableObject<Throwable> failedBackupError = new MutableObject<>(null);

        if (!isRemoteDatabase) {
            SQLHelper.close();
            if (!databaseFile.renameTo(new File(databaseFile.getParentFile(), "database-bkp.db"))) {
                failedBackupError.setValue(new RuntimeException("Failed to rename file to database-bkp.db"));
            } else {
                SQLHelper.createConnection(plugin);
                SQLDatabase.initializeDatabase();
            }
        }

        if (failedBackupError.getValue() != null) {
            Log.error(failedBackupError.getValue(), "[Database-Converter] Failed to create a backup for the database file:");
        } else {
            Log.info("[Database-Converter] Successfully created a backup for the database.");
        }
    }

    private static void saveConvertedData() {
        savePlayers();
        saveIslands();
        saveStackedBlocks();
        saveBankTransactions();
        saveGrid();
    }

    private static boolean isDatabaseOldFormat() {
        isRemoteDatabase = isRemoteDatabase();

        if (!isRemoteDatabase) {
            databaseFile = new File(plugin.getDataFolder(), "datastore/database.db");

            if (!databaseFile.exists())
                return false;
        }

        AtomicBoolean isOldFormat = new AtomicBoolean(true);

        SQLHelper.select("stackedBlocks", "", new QueryResult<ResultSet>().onFail(error -> {
            isOldFormat.set(false);
        }));

        return isOldFormat.get();
    }

    private static boolean isRemoteDatabase() {
        switch (plugin.getSettings().getDatabase().getType()) {
            case "MYSQL":
            case "MARIADB":
            case "POSTGRESQL":
                return true;
            default:
                return false;
        }
    }

    private static void savePlayers() {
        Log.info("[Database-Converter] Converting players...");

        CustomSQLDatabaseTransaction playersTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}players VALUES(?,?,?,?,?)");
        CustomSQLDatabaseTransaction playersMissionsTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}players_missions VALUES(?,?,?)");
        CustomSQLDatabaseTransaction playersSettingsTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}players_settings VALUES(?,?,?,?,?,?)");

        for (PlayerAttributes playerAttributes : loadedPlayers) {
            insertPlayer(playerAttributes, playersTransaction, playersMissionsTransaction, playersSettingsTransaction);
        }

        try {
            DatabaseTransactionsExecutor.addTransactions(playersTransaction,
                    playersMissionsTransaction, playersSettingsTransaction).get();
        } catch (InterruptedException | ExecutionException error) {
            error.printStackTrace();
        }
    }

    private static void saveIslands() {
        long currentTime = System.currentTimeMillis();

        Log.info("[Database-Converter] Converting islands...");

        CustomSQLDatabaseTransaction islandsTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}islands VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        CustomSQLDatabaseTransaction islandsBanksTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}islands_banks VALUES(?,?,?)");
        CustomSQLDatabaseTransaction islandsBansTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}islands_bans VALUES(?,?,?,?)");
        CustomSQLDatabaseTransaction islandsBlockLimitsTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}islands_block_limits VALUES(?,?,?)");
        CustomSQLDatabaseTransaction islandsChestsTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}islands_chests VALUES(?,?,?)");
        CustomSQLDatabaseTransaction islandsEffectsTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}islands_effects VALUES(?,?,?)");
        CustomSQLDatabaseTransaction islandsEntityLimitsTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}islands_entity_limits VALUES(?,?,?)");
        CustomSQLDatabaseTransaction islandsFlagsTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}islands_flags VALUES(?,?,?)");
        CustomSQLDatabaseTransaction islandsGeneratorsTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}islands_generators VALUES(?,?,?,?)");
        CustomSQLDatabaseTransaction islandsHomesTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}islands_homes VALUES(?,?,?)");
        CustomSQLDatabaseTransaction islandsMembersTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}islands_members VALUES(?,?,?,?)");
        CustomSQLDatabaseTransaction islandsMissionsTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}islands_missions VALUES(?,?,?)");
        CustomSQLDatabaseTransaction islandsPlayerPermissionsTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}islands_player_permissions VALUES(?,?,?,?)");
        CustomSQLDatabaseTransaction islandsRatingsTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}islands_ratings VALUES(?,?,?,?)");
        CustomSQLDatabaseTransaction islandsRoleLimitsTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}islands_role_limits VALUES(?,?,?)");
        CustomSQLDatabaseTransaction islandsRolePermissionsTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}islands_role_permissions VALUES(?,?,?)");
        CustomSQLDatabaseTransaction islandsSettingsTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}islands_settings VALUES(?,?,?,?,?,?,?,?,?)");
        CustomSQLDatabaseTransaction islandsUpgradesTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}islands_upgrades VALUES(?,?,?)");
        CustomSQLDatabaseTransaction islandsVisitorHomesTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}islands_visitor_homes VALUES(?,?,?)");
        CustomSQLDatabaseTransaction islandsVisitorsTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}islands_visitors VALUES(?,?,?)");
        CustomSQLDatabaseTransaction islandsWarpCategoriesTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}islands_warp_categories VALUES(?,?,?,?)");
        CustomSQLDatabaseTransaction islandsWarpsTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}islands_warps VALUES(?,?,?,?,?,?)");

        for (IslandAttributes islandAttributes : loadedIslands) {
            insertIsland(islandAttributes, currentTime, islandsTransaction, islandsBanksTransaction, islandsBansTransaction,
                    islandsBlockLimitsTransaction, islandsChestsTransaction, islandsEffectsTransaction, islandsEntityLimitsTransaction,
                    islandsFlagsTransaction, islandsGeneratorsTransaction, islandsHomesTransaction, islandsMembersTransaction,
                    islandsMissionsTransaction, islandsPlayerPermissionsTransaction, islandsRatingsTransaction, islandsRoleLimitsTransaction,
                    islandsRolePermissionsTransaction, islandsSettingsTransaction, islandsUpgradesTransaction, islandsVisitorHomesTransaction,
                    islandsVisitorsTransaction, islandsWarpCategoriesTransaction, islandsWarpsTransaction);
        }

        try {
            DatabaseTransactionsExecutor.addTransactions(
                    islandsTransaction,
                    islandsBanksTransaction,
                    islandsBansTransaction,
                    islandsBlockLimitsTransaction,
                    islandsChestsTransaction,
                    islandsEffectsTransaction,
                    islandsEntityLimitsTransaction,
                    islandsFlagsTransaction,
                    islandsGeneratorsTransaction,
                    islandsHomesTransaction,
                    islandsMembersTransaction,
                    islandsMissionsTransaction,
                    islandsPlayerPermissionsTransaction,
                    islandsRatingsTransaction,
                    islandsRoleLimitsTransaction,
                    islandsRolePermissionsTransaction,
                    islandsSettingsTransaction,
                    islandsUpgradesTransaction,
                    islandsVisitorHomesTransaction,
                    islandsVisitorsTransaction,
                    islandsWarpCategoriesTransaction,
                    islandsWarpsTransaction
            ).get();
        } catch (InterruptedException | ExecutionException error) {
            error.printStackTrace();
        }
    }

    private static void saveStackedBlocks() {
        Log.info("[Database-Converter] Converting stacked blocks...");

        CustomSQLDatabaseTransaction stackedBlocksTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}stacked_blocks VALUES(?,?,?)");

        for (StackedBlockAttributes stackedBlockAttributes : loadedBlocks) {
            stackedBlocksTransaction
                    .bindObject(stackedBlockAttributes.getValue(StackedBlockAttributes.Field.LOCATION))
                    .bindObject(stackedBlockAttributes.getValue(StackedBlockAttributes.Field.BLOCK_TYPE))
                    .bindObject(stackedBlockAttributes.getValue(StackedBlockAttributes.Field.AMOUNT))
                    .newBatch();
        }

        try {
            stackedBlocksTransaction.execute().get();
        } catch (InterruptedException | ExecutionException error) {
            error.printStackTrace();
        }
    }

    private static void saveBankTransactions() {
        Log.info("[Database-Converter] Converting bank transactions...");

        CustomSQLDatabaseTransaction bankTransactionsTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}bank_transactions VALUES(?,?,?,?,?,?,?)");

        for (BankTransactionsAttributes bankTransactionsAttributes : loadedBankTransactions) {
            bankTransactionsTransaction
                    .bindObject(bankTransactionsAttributes.getValue(BankTransactionsAttributes.Field.ISLAND))
                    .bindObject(bankTransactionsAttributes.getValue(BankTransactionsAttributes.Field.PLAYER))
                    .bindObject(bankTransactionsAttributes.getValue(BankTransactionsAttributes.Field.BANK_ACTION))
                    .bindObject(bankTransactionsAttributes.getValue(BankTransactionsAttributes.Field.POSITION))
                    .bindObject(bankTransactionsAttributes.getValue(BankTransactionsAttributes.Field.TIME))
                    .bindObject(bankTransactionsAttributes.getValue(BankTransactionsAttributes.Field.FAILURE_REASON))
                    .bindObject(bankTransactionsAttributes.getValue(BankTransactionsAttributes.Field.AMOUNT))
                    .newBatch();
        }

        try {
            bankTransactionsTransaction.execute().get();
        } catch (InterruptedException | ExecutionException error) {
            error.printStackTrace();
        }
    }

    private static void saveGrid() {
        if (gridAttributes == null)
            return;

        Log.info("[Database-Converter] Converting grid data...");

        CustomSQLDatabaseTransaction deleteGridTransaction = new CustomSQLDatabaseTransaction("DELETE FROM {prefix}grid;");
        try {
            deleteGridTransaction.execute().get();
        } catch (InterruptedException | ExecutionException error) {
            error.printStackTrace();
        }

        CustomSQLDatabaseTransaction insertGridTransaction = new CustomSQLDatabaseTransaction(
                "REPLACE INTO {prefix}grid VALUES(?,?,?)");
        insertGridTransaction
                .bindObject(gridAttributes.getValue(GridAttributes.Field.LAST_ISLAND))
                .bindObject(gridAttributes.getValue(GridAttributes.Field.MAX_ISLAND_SIZE))
                .bindObject(gridAttributes.getValue(GridAttributes.Field.WORLD));
        try {
            insertGridTransaction.execute().get();
        } catch (InterruptedException | ExecutionException error) {
            error.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static void insertPlayer(PlayerAttributes playerAttributes,
                                     CustomSQLDatabaseTransaction playersTransaction,
                                     CustomSQLDatabaseTransaction playersMissionsTransaction,
                                     CustomSQLDatabaseTransaction playersSettingsTransaction) {
        String playerUUID = playerAttributes.getValue(PlayerAttributes.Field.UUID);
        playersTransaction.bindObject(playerUUID)
                .bindObject(playerAttributes.getValue(PlayerAttributes.Field.LAST_USED_NAME))
                .bindObject(playerAttributes.getValue(PlayerAttributes.Field.LAST_USED_SKIN))
                .bindObject(playerAttributes.getValue(PlayerAttributes.Field.DISBANDS))
                .bindObject(playerAttributes.getValue(PlayerAttributes.Field.LAST_TIME_UPDATED))
                .newBatch();
        ((Map<String, Integer>) playerAttributes.getValue(PlayerAttributes.Field.COMPLETED_MISSIONS)).forEach((missionName, finishCount) ->
                playersMissionsTransaction.bindObject(playerUUID)
                        .bindObject(missionName.toLowerCase(Locale.ENGLISH))
                        .bindObject(finishCount)
                        .newBatch());
        playersSettingsTransaction.bindObject(playerUUID)
                .bindObject(playerAttributes.getValue(PlayerAttributes.Field.LANGUAGE))
                .bindObject(playerAttributes.getValue(PlayerAttributes.Field.TOGGLED_PANEL))
                .bindObject(((BorderColor) playerAttributes.getValue(PlayerAttributes.Field.BORDER_COLOR)).name())
                .bindObject(playerAttributes.getValue(PlayerAttributes.Field.TOGGLED_BORDER))
                .bindObject(playerAttributes.getValue(PlayerAttributes.Field.ISLAND_FLY))
                .newBatch();
    }

    @SuppressWarnings({"unchecked"})
    private static void insertIsland(IslandAttributes islandAttributes, long currentTime,
                                     CustomSQLDatabaseTransaction islandsTransaction,
                                     CustomSQLDatabaseTransaction islandsBanksTransaction,
                                     CustomSQLDatabaseTransaction islandsBansTransaction,
                                     CustomSQLDatabaseTransaction islandsBlockLimitsTransaction,
                                     CustomSQLDatabaseTransaction islandsChestsTransaction,
                                     CustomSQLDatabaseTransaction islandsEffectsTransaction,
                                     CustomSQLDatabaseTransaction islandsEntityLimitsTransaction,
                                     CustomSQLDatabaseTransaction islandsFlagsTransaction,
                                     CustomSQLDatabaseTransaction islandsGeneratorsTransaction,
                                     CustomSQLDatabaseTransaction islandsHomesTransaction,
                                     CustomSQLDatabaseTransaction islandsMembersTransaction,
                                     CustomSQLDatabaseTransaction islandsMissionsTransaction,
                                     CustomSQLDatabaseTransaction islandsPlayerPermissionsTransaction,
                                     CustomSQLDatabaseTransaction islandsRatingsTransaction,
                                     CustomSQLDatabaseTransaction islandsRoleLimitsTransaction,
                                     CustomSQLDatabaseTransaction islandsRolePermissionsTransaction,
                                     CustomSQLDatabaseTransaction islandsSettingsTransaction,
                                     CustomSQLDatabaseTransaction islandsUpgradesTransaction,
                                     CustomSQLDatabaseTransaction islandsVisitorHomesTransaction,
                                     CustomSQLDatabaseTransaction islandsVisitorsTransaction,
                                     CustomSQLDatabaseTransaction islandsWarpCategoriesTransaction,
                                     CustomSQLDatabaseTransaction islandsWarpsTransaction) {
        String islandUUID = islandAttributes.getValue(IslandAttributes.Field.UUID);
        islandsTransaction.bindObject(islandUUID)
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.OWNER))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.CENTER))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.CREATION_TIME))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.ISLAND_TYPE))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.DISCORD))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.PAYPAL))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.WORTH_BONUS))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.LEVELS_BONUS))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.LOCKED))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.IGNORED))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.NAME))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.DESCRIPTION))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.GENERATED_SCHEMATICS))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.UNLOCKED_WORLDS))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.LAST_TIME_UPDATED))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.DIRTY_CHUNKS))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.BLOCK_COUNTS))
                .newBatch();
        islandsBanksTransaction.bindObject(islandUUID)
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.BANK_BALANCE))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.BANK_LAST_INTEREST))
                .newBatch();
        ((List<PlayerAttributes>) islandAttributes.getValue(IslandAttributes.Field.BANS)).forEach(playerAttributes ->
                islandsBansTransaction.bindObject(islandUUID)
                        .bindObject(playerAttributes.getValue(PlayerAttributes.Field.UUID))
                        .bindObject(CONSOLE_UUID.toString())
                        .bindObject(currentTime)
                        .newBatch());
        ((KeyMap<Integer>) islandAttributes.getValue(IslandAttributes.Field.BLOCK_LIMITS)).forEach((key, limit) ->
                islandsBlockLimitsTransaction.bindObject(islandUUID)
                        .bindObject(key.toString())
                        .bindObject(limit)
                        .newBatch());
        ((List<IslandChestAttributes>) islandAttributes.getValue(IslandAttributes.Field.ISLAND_CHESTS)).forEach(islandChestAttributes ->
                islandsChestsTransaction.bindObject(islandUUID)
                        .bindObject(islandChestAttributes.getValue(IslandChestAttributes.Field.INDEX))
                        .bindObject(islandChestAttributes.getValue(IslandChestAttributes.Field.CONTENTS))
                        .newBatch());
        ((Map<PotionEffectType, Integer>) islandAttributes.getValue(IslandAttributes.Field.EFFECTS)).forEach((type, level) ->
                islandsEffectsTransaction.bindObject(islandUUID)
                        .bindObject(type.getName())
                        .bindObject(level)
                        .newBatch());
        ((KeyMap<Integer>) islandAttributes.getValue(IslandAttributes.Field.ENTITY_LIMITS)).forEach((entity, limit) ->
                islandsEntityLimitsTransaction.bindObject(islandUUID)
                        .bindObject(entity.toString())
                        .bindObject(limit)
                        .newBatch());
        ((Map<IslandFlag, Byte>) islandAttributes.getValue(IslandAttributes.Field.ISLAND_FLAGS)).forEach((islandFlag, status) ->
                islandsFlagsTransaction.bindObject(islandUUID)
                        .bindObject(islandFlag.getName())
                        .bindObject(status)
                        .newBatch());
        runOnEnvironments((KeyMap<Integer>[]) islandAttributes.getValue(IslandAttributes.Field.GENERATORS), (generatorRates, environment) ->
                generatorRates.forEach((block, rate) ->
                        islandsGeneratorsTransaction.bindObject(islandUUID)
                                .bindObject(environment.name())
                                .bindObject(block.toString())
                                .bindObject(rate)
                                .newBatch()));
        runOnEnvironments((String[]) islandAttributes.getValue(IslandAttributes.Field.HOMES), (islandHome, environment) ->
                islandsHomesTransaction.bindObject(islandUUID)
                        .bindObject(environment.name())
                        .bindObject(islandHome)
                        .newBatch());
        ((List<PlayerAttributes>) islandAttributes.getValue(IslandAttributes.Field.MEMBERS)).forEach(playerAttributes ->
                islandsMembersTransaction.bindObject(islandUUID)
                        .bindObject(playerAttributes.getValue(PlayerAttributes.Field.UUID))
                        .bindObject(((PlayerRole) playerAttributes.getValue(PlayerAttributes.Field.ISLAND_ROLE)).getId())
                        .bindObject(currentTime)
                        .newBatch());
        ((Map<String, Integer>) islandAttributes.getValue(IslandAttributes.Field.MISSIONS)).forEach((mission, finishCount) ->
                islandsMissionsTransaction.bindObject(islandUUID)
                        .bindObject(mission)
                        .bindObject(finishCount)
                        .newBatch());
        ((Map<UUID, PlayerPrivilegeNode>) islandAttributes.getValue(IslandAttributes.Field.PLAYER_PERMISSIONS)).forEach((playerUUID, node) -> {
            for (Map.Entry<IslandPrivilege, Boolean> playerPermission : node.getCustomPermissions().entrySet())
                islandsPlayerPermissionsTransaction.bindObject(islandUUID)
                        .bindObject(playerUUID.toString())
                        .bindObject(playerPermission.getKey().getName())
                        .bindObject(playerPermission.getValue())
                        .newBatch();
        });
        ((Map<UUID, Rating>) islandAttributes.getValue(IslandAttributes.Field.RATINGS)).forEach((playerUUID, rating) ->
                islandsRatingsTransaction.bindObject(islandUUID)
                        .bindObject(playerUUID.toString())
                        .bindObject(rating.getValue())
                        .bindObject(currentTime)
                        .newBatch());
        ((Map<PlayerRole, Integer>) islandAttributes.getValue(IslandAttributes.Field.ROLE_LIMITS)).forEach((role, limit) ->
                islandsRoleLimitsTransaction.bindObject(islandUUID)
                        .bindObject(role.getId())
                        .bindObject(limit)
                        .newBatch());
        ((Map<IslandPrivilege, PlayerRole>) islandAttributes.getValue(IslandAttributes.Field.ROLE_PERMISSIONS)).forEach((privilege, role) ->
                islandsRolePermissionsTransaction.bindObject(islandUUID)
                        .bindObject(role.getId())
                        .bindObject(privilege.getName())
                        .newBatch());
        islandsSettingsTransaction.bindObject(islandUUID)
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.ISLAND_SIZE))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.BANK_LIMIT))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.COOP_LIMIT))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.TEAM_LIMIT))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.WARPS_LIMIT))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.CROP_GROWTH_MULTIPLIER))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.SPAWNER_RATES_MULTIPLIER))
                .bindObject(islandAttributes.getValue(IslandAttributes.Field.MOB_DROPS_MULTIPLIER))
                .newBatch();
        ((Map<String, Integer>) islandAttributes.getValue(IslandAttributes.Field.UPGRADES)).forEach((upgradeName, level) ->
                islandsUpgradesTransaction.bindObject(islandUUID)
                        .bindObject(upgradeName)
                        .bindObject(level)
                        .newBatch());
        String visitorHome = islandAttributes.getValue(IslandAttributes.Field.VISITOR_HOMES);
        if (visitorHome != null && !visitorHome.isEmpty())
            islandsVisitorHomesTransaction.bindObject(islandUUID)
                    .bindObject(World.Environment.NORMAL.name())
                    .bindObject(visitorHome)
                    .newBatch();
        ((List<Pair<UUID, Long>>) islandAttributes.getValue(IslandAttributes.Field.VISITORS)).forEach(visitor ->
                islandsVisitorsTransaction.bindObject(islandUUID)
                        .bindObject(visitor.getKey().toString())
                        .bindObject(visitor.getValue())
                        .newBatch());
        ((List<WarpCategoryAttributes>) islandAttributes.getValue(IslandAttributes.Field.WARP_CATEGORIES)).forEach(warpCategoryAttributes ->
                islandsWarpCategoriesTransaction.bindObject(islandUUID)
                        .bindObject(warpCategoryAttributes.getValue(WarpCategoryAttributes.Field.NAME))
                        .bindObject(warpCategoryAttributes.getValue(WarpCategoryAttributes.Field.SLOT))
                        .bindObject(warpCategoryAttributes.getValue(WarpCategoryAttributes.Field.ICON))
                        .newBatch());
        ((List<IslandWarpAttributes>) islandAttributes.getValue(IslandAttributes.Field.WARPS)).forEach(islandWarpAttributes ->
                islandsWarpsTransaction.bindObject(islandUUID)
                        .bindObject(islandWarpAttributes.getValue(IslandWarpAttributes.Field.NAME))
                        .bindObject(islandWarpAttributes.getValue(IslandWarpAttributes.Field.CATEGORY))
                        .bindObject(islandWarpAttributes.getValue(IslandWarpAttributes.Field.LOCATION))
                        .bindObject(islandWarpAttributes.getValue(IslandWarpAttributes.Field.PRIVATE_STATUS))
                        .bindObject(islandWarpAttributes.getValue(IslandWarpAttributes.Field.ICON))
                        .newBatch());
    }

    private static <T> void runOnEnvironments(T[] arr, BiConsumer<T, World.Environment> consumer) {
        for (World.Environment environment : World.Environment.values()) {
            if (arr[environment.ordinal()] != null) {
                consumer.accept(arr[environment.ordinal()], environment);
            }
        }
    }

    private static PlayerAttributes loadPlayer(ResultSetMapBridge resultSet) {
        PlayerRole playerRole;

        try {
            playerRole = SPlayerRole.fromId(Integer.parseInt(resultSet.get("islandRole", "-1")));
        } catch (Exception ex) {
            playerRole = SPlayerRole.of((String) resultSet.get("islandRole"));
        }

        long currentTime = System.currentTimeMillis();

        return new PlayerAttributes()
                .setValue(PlayerAttributes.Field.UUID, resultSet.get("player"))
                .setValue(PlayerAttributes.Field.ISLAND_LEADER, resultSet.get("teamLeader", resultSet.get("player")))
                .setValue(PlayerAttributes.Field.LAST_USED_NAME, resultSet.get("name", "null"))
                .setValue(PlayerAttributes.Field.LAST_USED_SKIN, resultSet.get("textureValue", ""))
                .setValue(PlayerAttributes.Field.ISLAND_ROLE, playerRole)
                .setValue(PlayerAttributes.Field.DISBANDS, resultSet.get("disbands", plugin.getSettings().getDisbandCount()))
                .setValue(PlayerAttributes.Field.LAST_TIME_UPDATED, resultSet.get("lastTimeStatus", currentTime / 1000))
                .setValue(PlayerAttributes.Field.COMPLETED_MISSIONS, deserializer.deserializeMissions(resultSet.get("missions", "")))
                .setValue(PlayerAttributes.Field.TOGGLED_PANEL, resultSet.get("toggledPanel", plugin.getSettings().isDefaultToggledPanel()))
                .setValue(PlayerAttributes.Field.ISLAND_FLY, resultSet.get("islandFly", plugin.getSettings().isDefaultIslandFly()))
                .setValue(PlayerAttributes.Field.BORDER_COLOR, BorderColor.valueOf(resultSet.get("borderColor", plugin.getSettings().getDefaultBorderColor())))
                .setValue(PlayerAttributes.Field.LANGUAGE, resultSet.get("language", plugin.getSettings().getDefaultLanguage()))
                .setValue(PlayerAttributes.Field.TOGGLED_BORDER, resultSet.get("toggledBorder", plugin.getSettings().isDefaultWorldBorder())
                );
    }

    private static IslandAttributes loadIsland(ResultSetMapBridge resultSet) {
        UUID ownerUUID = UUID.fromString((String) resultSet.get("owner"));
        UUID islandUUID;

        String uuidRaw = resultSet.get("uuid", null);
        if (Text.isBlank(uuidRaw)) {
            islandUUID = ownerUUID;
        } else {
            islandUUID = UUID.fromString(uuidRaw);
        }

        int generatedSchematics = 0;
        String generatedSchematicsRaw = resultSet.get("generatedSchematics", "0");
        try {
            generatedSchematics = Integer.parseInt(generatedSchematicsRaw);
        } catch (Exception ex) {
            if (generatedSchematicsRaw.contains("normal"))
                generatedSchematics |= 8;
            if (generatedSchematicsRaw.contains("nether"))
                generatedSchematics |= 4;
            if (generatedSchematicsRaw.contains("the_end"))
                generatedSchematics |= 3;
        }

        int unlockedWorlds = 0;
        String unlockedWorldsRaw = resultSet.get("unlockedWorlds", "0");
        try {
            unlockedWorlds = Integer.parseInt(unlockedWorldsRaw);
        } catch (Exception ex) {
            if (unlockedWorldsRaw.contains("nether"))
                unlockedWorlds |= 1;
            if (unlockedWorldsRaw.contains("the_end"))
                unlockedWorlds |= 2;
        }

        long currentTime = System.currentTimeMillis();

        return new IslandAttributes()
                .setValue(IslandAttributes.Field.UUID, islandUUID.toString())
                .setValue(IslandAttributes.Field.OWNER, ownerUUID.toString())
                .setValue(IslandAttributes.Field.CENTER, (String) resultSet.get("center"))
                .setValue(IslandAttributes.Field.CREATION_TIME, resultSet.get("creationTime", currentTime / 1000))
                .setValue(IslandAttributes.Field.ISLAND_TYPE, resultSet.get("schemName", ""))
                .setValue(IslandAttributes.Field.DISCORD, resultSet.get("discord", "None"))
                .setValue(IslandAttributes.Field.PAYPAL, resultSet.get("paypal", "None"))
                .setValue(IslandAttributes.Field.WORTH_BONUS, resultSet.get("bonusWorth", ""))
                .setValue(IslandAttributes.Field.LEVELS_BONUS, resultSet.get("bonusLevel", ""))
                .setValue(IslandAttributes.Field.LOCKED, resultSet.get("locked", false))
                .setValue(IslandAttributes.Field.IGNORED, resultSet.get("ignored", false))
                .setValue(IslandAttributes.Field.NAME, resultSet.get("name", ""))
                .setValue(IslandAttributes.Field.DESCRIPTION, resultSet.get("description", ""))
                .setValue(IslandAttributes.Field.GENERATED_SCHEMATICS, generatedSchematics)
                .setValue(IslandAttributes.Field.UNLOCKED_WORLDS, unlockedWorlds)
                .setValue(IslandAttributes.Field.LAST_TIME_UPDATED, resultSet.get("lastTimeUpdate", currentTime / 1000))
                .setValue(IslandAttributes.Field.DIRTY_CHUNKS, deserializer.deserializeDirtyChunks(resultSet.get("dirtyChunks", "")))
                .setValue(IslandAttributes.Field.BLOCK_COUNTS, deserializer.deserializeBlockCounts(resultSet.get("blockCounts", "")))
                .setValue(IslandAttributes.Field.HOMES, deserializer.deserializeHomes(resultSet.get("teleportLocation", "")))
                .setValue(IslandAttributes.Field.MEMBERS, deserializer.deserializePlayers(resultSet.get("members", "")))
                .setValue(IslandAttributes.Field.BANS, deserializer.deserializePlayers(resultSet.get("banned", "")))
                .setValue(IslandAttributes.Field.PLAYER_PERMISSIONS, deserializer.deserializePlayerPerms(resultSet.get("permissionNodes", "")))
                .setValue(IslandAttributes.Field.ROLE_PERMISSIONS, deserializer.deserializeRolePerms(resultSet.get("permissionNodes", "")))
                .setValue(IslandAttributes.Field.UPGRADES, deserializer.deserializeUpgrades(resultSet.get("upgrades", "")))
                .setValue(IslandAttributes.Field.WARPS, deserializer.deserializeWarps(resultSet.get("warps", "")))
                .setValue(IslandAttributes.Field.BLOCK_LIMITS, deserializer.deserializeBlockLimits(resultSet.get("blockLimits", "")))
                .setValue(IslandAttributes.Field.RATINGS, deserializer.deserializeRatings(resultSet.get("ratings", "")))
                .setValue(IslandAttributes.Field.MISSIONS, deserializer.deserializeMissions(resultSet.get("missions", "")))
                .setValue(IslandAttributes.Field.ISLAND_FLAGS, deserializer.deserializeIslandFlags(resultSet.get("settings", "")))
                .setValue(IslandAttributes.Field.GENERATORS, deserializer.deserializeGenerators(resultSet.get("generator", "")))
                .setValue(IslandAttributes.Field.VISITORS, deserializer.deserializeVisitors(resultSet.get("uniqueVisitors", "")))
                .setValue(IslandAttributes.Field.ENTITY_LIMITS, deserializer.deserializeEntityLimits(resultSet.get("entityLimits", "")))
                .setValue(IslandAttributes.Field.EFFECTS, deserializer.deserializeEffects(resultSet.get("islandEffects", "")))
                .setValue(IslandAttributes.Field.ISLAND_CHESTS, deserializer.deserializeIslandChests(resultSet.get("islandChest", "")))
                .setValue(IslandAttributes.Field.ROLE_LIMITS, deserializer.deserializeRoleLimits(resultSet.get("roleLimits", "")))
                .setValue(IslandAttributes.Field.WARP_CATEGORIES, deserializer.deserializeWarpCategories(resultSet.get("warpCategories", "")))
                .setValue(IslandAttributes.Field.BANK_BALANCE, resultSet.get("islandBank", ""))
                .setValue(IslandAttributes.Field.BANK_LAST_INTEREST, resultSet.get("lastInterest", currentTime / 1000))
                .setValue(IslandAttributes.Field.VISITOR_HOMES, resultSet.get("visitorsLocation", ""))
                .setValue(IslandAttributes.Field.ISLAND_SIZE, resultSet.get("islandSize", -1))
                .setValue(IslandAttributes.Field.TEAM_LIMIT, resultSet.get("teamLimit", -1))
                .setValue(IslandAttributes.Field.WARPS_LIMIT, resultSet.get("warpsLimit", -1))
                .setValue(IslandAttributes.Field.CROP_GROWTH_MULTIPLIER, resultSet.get("cropGrowth", -1D))
                .setValue(IslandAttributes.Field.SPAWNER_RATES_MULTIPLIER, resultSet.get("spawnerRates", -1D))
                .setValue(IslandAttributes.Field.MOB_DROPS_MULTIPLIER, resultSet.get("mobDrops", -1D))
                .setValue(IslandAttributes.Field.COOP_LIMIT, resultSet.get("coopLimit", -1))
                .setValue(IslandAttributes.Field.BANK_LIMIT, resultSet.get("bankLimit", "-2"));
    }

    private static StackedBlockAttributes loadStackedBlock(ResultSetMapBridge resultSet) {
        String world = (String) resultSet.get("world");
        int x = (int) resultSet.get("x");
        int y = (int) resultSet.get("y");
        int z = (int) resultSet.get("z");
        String amount = (String) resultSet.get("amount");
        String blockType = (String) resultSet.get("item");

        return new StackedBlockAttributes()
                .setValue(StackedBlockAttributes.Field.LOCATION, world + ", " + x + ", " + y + ", " + z)
                .setValue(StackedBlockAttributes.Field.BLOCK_TYPE, blockType)
                .setValue(StackedBlockAttributes.Field.AMOUNT, amount);
    }

    private static BankTransactionsAttributes loadBankTransaction(ResultSetMapBridge resultSet) {
        return new BankTransactionsAttributes()
                .setValue(BankTransactionsAttributes.Field.ISLAND, resultSet.get("island"))
                .setValue(BankTransactionsAttributes.Field.PLAYER, resultSet.get("player"))
                .setValue(BankTransactionsAttributes.Field.BANK_ACTION, resultSet.get("bankAction"))
                .setValue(BankTransactionsAttributes.Field.POSITION, resultSet.get("position"))
                .setValue(BankTransactionsAttributes.Field.TIME, resultSet.get("time"))
                .setValue(BankTransactionsAttributes.Field.FAILURE_REASON, resultSet.get("failureReason"))
                .setValue(BankTransactionsAttributes.Field.AMOUNT, resultSet.get("amount"));
    }

    public static PlayerAttributes getPlayerAttributes(String uuid) {
        return loadedPlayers.stream().filter(playerAttributes ->
                        playerAttributes.getValue(PlayerAttributes.Field.UUID).equals(uuid))
                .findFirst()
                .orElse(null);
    }

}
