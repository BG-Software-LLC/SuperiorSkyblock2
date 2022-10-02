package com.bgsoftware.superiorskyblock.core.database.loader.v1;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.Mutable;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.database.loader.MachineStateDatabaseLoader;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.attributes.BankTransactionsAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.attributes.GridAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.attributes.IslandAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.attributes.IslandChestAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.attributes.IslandWarpAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.attributes.PlayerAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.attributes.StackedBlockAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.attributes.WarpCategoryAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.deserializer.EmptyParameterGuardDeserializer;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.deserializer.IDeserializer;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.deserializer.JsonDeserializer;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.deserializer.MultipleDeserializer;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.deserializer.RawDeserializer;
import com.bgsoftware.superiorskyblock.core.database.sql.ResultSetMapBridge;
import com.bgsoftware.superiorskyblock.core.database.sql.SQLHelper;
import com.bgsoftware.superiorskyblock.core.database.sql.StatementHolder;
import com.bgsoftware.superiorskyblock.core.database.sql.session.QueryResult;
import com.bgsoftware.superiorskyblock.core.database.sql.session.SQLSession;
import com.bgsoftware.superiorskyblock.core.database.sql.session.impl.SQLiteSession;
import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;
import com.bgsoftware.superiorskyblock.core.logging.Log;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class DatabaseLoader_V1 extends MachineStateDatabaseLoader {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final UUID CONSOLE_UUID = new UUID(0, 0);

    private static File databaseFile;
    private static SQLSession session;
    private static boolean isRemoteDatabase;

    private final List<PlayerAttributes> loadedPlayers = new ArrayList<>();
    private final List<IslandAttributes> loadedIslands = new ArrayList<>();
    private final List<StackedBlockAttributes> loadedBlocks = new ArrayList<>();
    private final List<BankTransactionsAttributes> loadedBankTransactions = new ArrayList<>();
    private final IDeserializer deserializer = new MultipleDeserializer(
            EmptyParameterGuardDeserializer.getInstance(),
            new JsonDeserializer(this),
            new RawDeserializer(this, plugin)
    );
    private GridAttributes gridAttributes;

    private boolean isOldDatabaseFormat;

    @Override
    public void setState(State state) throws ManagerLoadException {
        if (state == State.INITIALIZE)
            isOldDatabaseFormat = isDatabaseOldFormat();

        if (isOldDatabaseFormat)
            super.setState(state);
    }

    @Override
    protected void handleInitialize() {
        Log.info("[Database-Converter] Detected old database - starting to convert data...");

        session.select("players", "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
            while (resultSet.next()) {
                loadedPlayers.add(loadPlayer(new ResultSetMapBridge(resultSet)));
            }
        }).onFail(QueryResult.PRINT_ERROR));

        Log.info("[Database-Converter] Found ", loadedPlayers.size(), " players in the database.");

        session.select("islands", "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
            while (resultSet.next()) {
                loadedIslands.add(loadIsland(new ResultSetMapBridge(resultSet)));
            }
        }).onFail(QueryResult.PRINT_ERROR));

        Log.info("[Database-Converter] Found ", loadedIslands.size(), " islands in the database.");

        session.select("stackedBlocks", "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
            while (resultSet.next()) {
                loadedBlocks.add(loadStackedBlock(new ResultSetMapBridge(resultSet)));
            }
        }).onFail(QueryResult.PRINT_ERROR));

        Log.info("[Database-Converter] Found ", loadedBlocks.size(), " stacked blocks in the database.");

        // Ignoring errors as the bankTransactions table may not exist.
        AtomicBoolean foundBankTransaction = new AtomicBoolean(false);
        session.select("bankTransactions", "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
            foundBankTransaction.set(true);
            while (resultSet.next()) {
                loadedBankTransactions.add(loadBankTransaction(new ResultSetMapBridge(resultSet)));
            }
        }));

        if (foundBankTransaction.get()) {
            Log.info("[Database-Converter] Found ", loadedBankTransactions.size(), " bank transactions in the database.");
        }

        session.select("grid", "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
            if (resultSet.next()) {
                gridAttributes = new GridAttributes()
                        .setValue(GridAttributes.Field.LAST_ISLAND, resultSet.getString("lastIsland"))
                        .setValue(GridAttributes.Field.MAX_ISLAND_SIZE, resultSet.getString("maxIslandSize"))
                        .setValue(GridAttributes.Field.WORLD, resultSet.getString("world"));
            }
        }).onFail(QueryResult.PRINT_ERROR));

        Mutable<Throwable> failedBackupError = new Mutable<>(null);

        if (!isRemoteDatabase) {
            session.closeConnection();
            if (databaseFile.renameTo(new File(databaseFile.getParentFile(), "database-bkp.db"))) {
                failedBackupError.setValue(new RuntimeException("Failed to rename file to database-bkp.db"));
            }
        }

        if (failedBackupError.getValue() != null) {
            if (!isRemoteDatabase) {
                session = new SQLiteSession(plugin, false);
                session.createConnection();
            }

            failedBackupError.setValue(null);

            session.renameTable("islands", "bkp_islands", new QueryResult<Void>()
                    .onFail(failedBackupError::setValue));

            session.renameTable("players", "bkp_players", new QueryResult<Void>()
                    .onFail(failedBackupError::setValue));

            session.renameTable("grid", "bkp_grid", new QueryResult<Void>()
                    .onFail(failedBackupError::setValue));

            session.renameTable("stackedBlocks", "bkp_stackedBlocks", new QueryResult<Void>()
                    .onFail(failedBackupError::setValue));

            session.renameTable("bankTransactions", "bkp_bankTransactions", new QueryResult<Void>()
                    .onFail(failedBackupError::setValue));
        }

        if (isRemoteDatabase)
            session.closeConnection();

        if (failedBackupError.getValue() != null) {
            Log.error(failedBackupError.getValue(), "[Database-Converter] Failed to create a backup for the database file:");
        } else {
            Log.info("[Database-Converter] Successfully created a backup for the database.");
        }
    }

    @Override
    protected void handlePostInitialize() {
        savePlayers();
        saveIslands();
        saveStackedBlocks();
        saveBankTransactions();
        saveGrid();
    }

    @Override
    protected void handlePreLoadData() {
        // Do nothing.
    }

    @Override
    protected void handlePostLoadData() {
        // Do nothing.
    }

    @Override
    protected void handleShutdown() {
        // Do nothing.
    }

    private static boolean isDatabaseOldFormat() {
        isRemoteDatabase = isRemoteDatabase();

        if (!isRemoteDatabase) {
            databaseFile = new File(plugin.getDataFolder(), "datastore/database.db");

            if (!databaseFile.exists())
                return false;
        }

        session = SQLHelper.createSession(plugin, false);

        if (!session.createConnection()) {
            return false;
        }

        AtomicBoolean isOldFormat = new AtomicBoolean(true);

        session.select("stackedBlocks", "", new QueryResult<ResultSet>().onFail(error -> {
            session.closeConnection();
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

    private void savePlayers() {
        Log.info("[Database-Converter] Converting players...");

        StatementHolder playersQuery = new StatementHolder("REPLACE INTO {prefix}players VALUES(?,?,?,?,?)");
        StatementHolder playersMissionsQuery = new StatementHolder("REPLACE INTO {prefix}players_missions VALUES(?,?,?)");
        StatementHolder playersSettingsQuery = new StatementHolder("REPLACE INTO {prefix}players_settings VALUES(?,?,?,?,?,?)");

        for (PlayerAttributes playerAttributes : loadedPlayers) {
            insertPlayer(playerAttributes, playersQuery, playersMissionsQuery, playersSettingsQuery);
        }

        playersQuery.executeBatch(false);
        playersMissionsQuery.executeBatch(false);
        playersSettingsQuery.executeBatch(false);
    }

    private void saveIslands() {
        long currentTime = System.currentTimeMillis();

        Log.info("[Database-Converter] Converting islands...");

        StatementHolder islandsQuery = new StatementHolder("REPLACE INTO {prefix}islands VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        StatementHolder islandsBanksQuery = new StatementHolder("REPLACE INTO {prefix}islands_banks VALUES(?,?,?)");
        StatementHolder islandsBansQuery = new StatementHolder("REPLACE INTO {prefix}islands_bans VALUES(?,?,?,?)");
        StatementHolder islandsBlockLimitsQuery = new StatementHolder("REPLACE INTO {prefix}islands_block_limits VALUES(?,?,?)");
        StatementHolder islandsChestsQuery = new StatementHolder("REPLACE INTO {prefix}islands_chests VALUES(?,?,?)");
        StatementHolder islandsEffectsQuery = new StatementHolder("REPLACE INTO {prefix}islands_effects VALUES(?,?,?)");
        StatementHolder islandsEntityLimitsQuery = new StatementHolder("REPLACE INTO {prefix}islands_entity_limits VALUES(?,?,?)");
        StatementHolder islandsFlagsQuery = new StatementHolder("REPLACE INTO {prefix}islands_flags VALUES(?,?,?)");
        StatementHolder islandsGeneratorsQuery = new StatementHolder("REPLACE INTO {prefix}islands_generators VALUES(?,?,?,?)");
        StatementHolder islandsHomesQuery = new StatementHolder("REPLACE INTO {prefix}islands_homes VALUES(?,?,?)");
        StatementHolder islandsMembersQuery = new StatementHolder("REPLACE INTO {prefix}islands_members VALUES(?,?,?,?)");
        StatementHolder islandsMissionsQuery = new StatementHolder("REPLACE INTO {prefix}islands_missions VALUES(?,?,?)");
        StatementHolder islandsPlayerPermissionsQuery = new StatementHolder("REPLACE INTO {prefix}islands_player_permissions VALUES(?,?,?,?)");
        StatementHolder islandsRatingsQuery = new StatementHolder("REPLACE INTO {prefix}islands_ratings VALUES(?,?,?,?)");
        StatementHolder islandsRoleLimitsQuery = new StatementHolder("REPLACE INTO {prefix}islands_role_limits VALUES(?,?,?)");
        StatementHolder islandsRolePermissionsQuery = new StatementHolder("REPLACE INTO {prefix}islands_role_permissions VALUES(?,?,?)");
        StatementHolder islandsSettingsQuery = new StatementHolder("REPLACE INTO {prefix}islands_settings VALUES(?,?,?,?,?,?,?,?,?)");
        StatementHolder islandsUpgradesQuery = new StatementHolder("REPLACE INTO {prefix}islands_upgrades VALUES(?,?,?)");
        StatementHolder islandsVisitorHomesQuery = new StatementHolder("REPLACE INTO {prefix}islands_visitor_homes VALUES(?,?,?)");
        StatementHolder islandsVisitorsQuery = new StatementHolder("REPLACE INTO {prefix}islands_visitors VALUES(?,?,?)");
        StatementHolder islandsWarpCategoriesQuery = new StatementHolder("REPLACE INTO {prefix}islands_warp_categories VALUES(?,?,?,?)");
        StatementHolder islandsWarpsQuery = new StatementHolder("REPLACE INTO {prefix}islands_warps VALUES(?,?,?,?,?,?)");

        for (IslandAttributes islandAttributes : loadedIslands) {
            insertIsland(islandAttributes, currentTime, islandsQuery, islandsBanksQuery, islandsBansQuery,
                    islandsBlockLimitsQuery, islandsChestsQuery, islandsEffectsQuery, islandsEntityLimitsQuery,
                    islandsFlagsQuery, islandsGeneratorsQuery, islandsHomesQuery, islandsMembersQuery,
                    islandsMissionsQuery, islandsPlayerPermissionsQuery, islandsRatingsQuery, islandsRoleLimitsQuery,
                    islandsRolePermissionsQuery, islandsSettingsQuery, islandsUpgradesQuery, islandsVisitorHomesQuery,
                    islandsVisitorsQuery, islandsWarpCategoriesQuery, islandsWarpsQuery);
        }

        islandsQuery.executeBatch(false);
        islandsBanksQuery.executeBatch(false);
        islandsBansQuery.executeBatch(false);
        islandsBlockLimitsQuery.executeBatch(false);
        islandsChestsQuery.executeBatch(false);
        islandsEffectsQuery.executeBatch(false);
        islandsEntityLimitsQuery.executeBatch(false);
        islandsFlagsQuery.executeBatch(false);
        islandsGeneratorsQuery.executeBatch(false);
        islandsHomesQuery.executeBatch(false);
        islandsMembersQuery.executeBatch(false);
        islandsMissionsQuery.executeBatch(false);
        islandsPlayerPermissionsQuery.executeBatch(false);
        islandsRatingsQuery.executeBatch(false);
        islandsRoleLimitsQuery.executeBatch(false);
        islandsRolePermissionsQuery.executeBatch(false);
        islandsSettingsQuery.executeBatch(false);
        islandsUpgradesQuery.executeBatch(false);
        islandsVisitorHomesQuery.executeBatch(false);
        islandsVisitorsQuery.executeBatch(false);
        islandsWarpCategoriesQuery.executeBatch(false);
        islandsWarpsQuery.executeBatch(false);
    }

    private void saveStackedBlocks() {
        Log.info("[Database-Converter] Converting stacked blocks...");

        StatementHolder insertQuery = new StatementHolder("REPLACE INTO {prefix}stacked_blocks VALUES(?,?,?)");

        for (StackedBlockAttributes stackedBlockAttributes : loadedBlocks) {
            insertQuery
                    .setObject(stackedBlockAttributes.getValue(StackedBlockAttributes.Field.LOCATION))
                    .setObject(stackedBlockAttributes.getValue(StackedBlockAttributes.Field.BLOCK_TYPE))
                    .setObject(stackedBlockAttributes.getValue(StackedBlockAttributes.Field.AMOUNT))
                    .addBatch();
        }

        insertQuery.executeBatch(false);
    }

    private void saveBankTransactions() {
        Log.info("[Database-Converter] Converting bank transactions...");

        StatementHolder insertQuery = new StatementHolder("REPLACE INTO {prefix}bank_transactions VALUES(?,?,?,?,?,?,?)");

        for (BankTransactionsAttributes bankTransactionsAttributes : loadedBankTransactions) {
            insertQuery
                    .setObject(bankTransactionsAttributes.getValue(BankTransactionsAttributes.Field.ISLAND))
                    .setObject(bankTransactionsAttributes.getValue(BankTransactionsAttributes.Field.PLAYER))
                    .setObject(bankTransactionsAttributes.getValue(BankTransactionsAttributes.Field.BANK_ACTION))
                    .setObject(bankTransactionsAttributes.getValue(BankTransactionsAttributes.Field.POSITION))
                    .setObject(bankTransactionsAttributes.getValue(BankTransactionsAttributes.Field.TIME))
                    .setObject(bankTransactionsAttributes.getValue(BankTransactionsAttributes.Field.FAILURE_REASON))
                    .setObject(bankTransactionsAttributes.getValue(BankTransactionsAttributes.Field.AMOUNT))
                    .addBatch();
        }

        insertQuery.executeBatch(false);
    }

    private void saveGrid() {
        if (gridAttributes == null)
            return;

        Log.info("[Database-Converter] Converting grid data...");

        new StatementHolder("DELETE FROM {prefix}grid;").execute(false);
        new StatementHolder("REPLACE INTO {prefix}grid VALUES(?,?,?)")
                .setObject(gridAttributes.getValue(GridAttributes.Field.LAST_ISLAND))
                .setObject(gridAttributes.getValue(GridAttributes.Field.MAX_ISLAND_SIZE))
                .setObject(gridAttributes.getValue(GridAttributes.Field.WORLD))
                .execute(false);
    }

    @SuppressWarnings("unchecked")
    private void insertPlayer(PlayerAttributes playerAttributes,
                              StatementHolder playersQuery,
                              StatementHolder playersMissionsQuery,
                              StatementHolder playersSettingsQuery) {
        String playerUUID = playerAttributes.getValue(PlayerAttributes.Field.UUID);
        playersQuery.setObject(playerUUID)
                .setObject(playerAttributes.getValue(PlayerAttributes.Field.LAST_USED_NAME))
                .setObject(playerAttributes.getValue(PlayerAttributes.Field.LAST_USED_SKIN))
                .setObject(playerAttributes.getValue(PlayerAttributes.Field.DISBANDS))
                .setObject(playerAttributes.getValue(PlayerAttributes.Field.LAST_TIME_UPDATED))
                .addBatch();
        ((Map<String, Integer>) playerAttributes.getValue(PlayerAttributes.Field.COMPLETED_MISSIONS)).forEach((missionName, finishCount) ->
                playersMissionsQuery.setObject(playerUUID)
                        .setObject(missionName.toLowerCase(Locale.ENGLISH))
                        .setObject(finishCount)
                        .addBatch());
        playersSettingsQuery.setObject(playerUUID)
                .setObject(playerAttributes.getValue(PlayerAttributes.Field.LANGUAGE))
                .setObject(playerAttributes.getValue(PlayerAttributes.Field.TOGGLED_PANEL))
                .setObject(((BorderColor) playerAttributes.getValue(PlayerAttributes.Field.BORDER_COLOR)).name())
                .setObject(playerAttributes.getValue(PlayerAttributes.Field.TOGGLED_BORDER))
                .setObject(playerAttributes.getValue(PlayerAttributes.Field.ISLAND_FLY))
                .addBatch();
    }

    @SuppressWarnings({"unchecked"})
    private void insertIsland(IslandAttributes islandAttributes, long currentTime,
                              StatementHolder islandsQuery, StatementHolder islandsBanksQuery,
                              StatementHolder islandsBansQuery, StatementHolder islandsBlockLimitsQuery,
                              StatementHolder islandsChestsQuery, StatementHolder islandsEffectsQuery,
                              StatementHolder islandsEntityLimitsQuery, StatementHolder islandsFlagsQuery,
                              StatementHolder islandsGeneratorsQuery, StatementHolder islandsHomesQuery,
                              StatementHolder islandsMembersQuery, StatementHolder islandsMissionsQuery,
                              StatementHolder islandsPlayerPermissionsQuery, StatementHolder islandsRatingsQuery,
                              StatementHolder islandsRoleLimitsQuery, StatementHolder islandsRolePermissionsQuery,
                              StatementHolder islandsSettingsQuery, StatementHolder islandsUpgradesQuery,
                              StatementHolder islandsVisitorHomesQuery, StatementHolder islandsVisitorsQuery,
                              StatementHolder islandsWarpCategoriesQuery, StatementHolder islandsWarpsQuery) {
        String islandUUID = islandAttributes.getValue(IslandAttributes.Field.UUID);
        islandsQuery.setObject(islandUUID)
                .setObject(islandAttributes.getValue(IslandAttributes.Field.OWNER))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.CENTER))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.CREATION_TIME))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.ISLAND_TYPE))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.DISCORD))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.PAYPAL))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.WORTH_BONUS))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.LEVELS_BONUS))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.LOCKED))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.IGNORED))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.NAME))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.DESCRIPTION))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.GENERATED_SCHEMATICS))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.UNLOCKED_WORLDS))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.LAST_TIME_UPDATED))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.DIRTY_CHUNKS))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.BLOCK_COUNTS))
                .addBatch();
        islandsBanksQuery.setObject(islandUUID)
                .setObject(islandAttributes.getValue(IslandAttributes.Field.BANK_BALANCE))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.BANK_LAST_INTEREST))
                .addBatch();
        ((List<PlayerAttributes>) islandAttributes.getValue(IslandAttributes.Field.BANS)).forEach(playerAttributes ->
                islandsBansQuery.setObject(islandUUID)
                        .setObject(playerAttributes.getValue(PlayerAttributes.Field.UUID))
                        .setObject(CONSOLE_UUID.toString())
                        .setObject(currentTime)
                        .addBatch());
        ((KeyMap<Integer>) islandAttributes.getValue(IslandAttributes.Field.BLOCK_LIMITS)).forEach((key, limit) ->
                islandsBlockLimitsQuery.setObject(islandUUID)
                        .setObject(key.toString())
                        .setObject(limit)
                        .addBatch());
        ((List<IslandChestAttributes>) islandAttributes.getValue(IslandAttributes.Field.ISLAND_CHESTS)).forEach(islandChestAttributes ->
                islandsChestsQuery.setObject(islandUUID)
                        .setObject(islandChestAttributes.getValue(IslandChestAttributes.Field.INDEX))
                        .setObject(islandChestAttributes.getValue(IslandChestAttributes.Field.CONTENTS))
                        .addBatch());
        ((Map<PotionEffectType, Integer>) islandAttributes.getValue(IslandAttributes.Field.EFFECTS)).forEach((type, level) ->
                islandsEffectsQuery.setObject(islandUUID)
                        .setObject(type.getName())
                        .setObject(level)
                        .addBatch());
        ((KeyMap<Integer>) islandAttributes.getValue(IslandAttributes.Field.ENTITY_LIMITS)).forEach((entity, limit) ->
                islandsEntityLimitsQuery.setObject(islandUUID)
                        .setObject(entity.toString())
                        .setObject(limit)
                        .addBatch());
        ((Map<IslandFlag, Byte>) islandAttributes.getValue(IslandAttributes.Field.ISLAND_FLAGS)).forEach((islandFlag, status) ->
                islandsFlagsQuery.setObject(islandUUID)
                        .setObject(islandFlag.getName())
                        .setObject(status)
                        .addBatch());
        runOnEnvironments((KeyMap<Integer>[]) islandAttributes.getValue(IslandAttributes.Field.GENERATORS), (generatorRates, environment) ->
                generatorRates.forEach((block, rate) ->
                        islandsGeneratorsQuery.setObject(islandUUID)
                                .setObject(environment.name())
                                .setObject(block.toString())
                                .setObject(rate)
                                .addBatch()));
        runOnEnvironments((String[]) islandAttributes.getValue(IslandAttributes.Field.HOMES), (islandHome, environment) ->
                islandsHomesQuery.setObject(islandUUID)
                        .setObject(environment.name())
                        .setObject(islandHome)
                        .addBatch());
        ((List<PlayerAttributes>) islandAttributes.getValue(IslandAttributes.Field.MEMBERS)).forEach(playerAttributes ->
                islandsMembersQuery.setObject(islandUUID)
                        .setObject(playerAttributes.getValue(PlayerAttributes.Field.UUID))
                        .setObject(((PlayerRole) playerAttributes.getValue(PlayerAttributes.Field.ISLAND_ROLE)).getId())
                        .setObject(currentTime)
                        .addBatch());
        ((Map<String, Integer>) islandAttributes.getValue(IslandAttributes.Field.MISSIONS)).forEach((mission, finishCount) ->
                islandsMissionsQuery.setObject(islandUUID)
                        .setObject(mission)
                        .setObject(finishCount)
                        .addBatch());
        ((Map<UUID, PlayerPrivilegeNode>) islandAttributes.getValue(IslandAttributes.Field.PLAYER_PERMISSIONS)).forEach((playerUUID, node) -> {
            for (Map.Entry<IslandPrivilege, Boolean> playerPermission : node.getCustomPermissions().entrySet())
                islandsPlayerPermissionsQuery.setObject(islandUUID)
                        .setObject(playerUUID.toString())
                        .setObject(playerPermission.getKey().getName())
                        .setObject(playerPermission.getValue())
                        .addBatch();
        });
        ((Map<UUID, Rating>) islandAttributes.getValue(IslandAttributes.Field.RATINGS)).forEach((playerUUID, rating) ->
                islandsRatingsQuery.setObject(islandUUID)
                        .setObject(playerUUID.toString())
                        .setObject(rating.getValue())
                        .setObject(currentTime)
                        .addBatch());
        ((Map<PlayerRole, Integer>) islandAttributes.getValue(IslandAttributes.Field.ROLE_LIMITS)).forEach((role, limit) ->
                islandsRoleLimitsQuery.setObject(islandUUID)
                        .setObject(role.getId())
                        .setObject(limit)
                        .addBatch());
        ((Map<IslandPrivilege, PlayerRole>) islandAttributes.getValue(IslandAttributes.Field.ROLE_PERMISSIONS)).forEach((privilege, role) ->
                islandsRolePermissionsQuery.setObject(islandUUID)
                        .setObject(role.getId())
                        .setObject(privilege.getName())
                        .addBatch());
        islandsSettingsQuery.setObject(islandUUID)
                .setObject(islandAttributes.getValue(IslandAttributes.Field.ISLAND_SIZE))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.BANK_LIMIT))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.COOP_LIMIT))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.TEAM_LIMIT))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.WARPS_LIMIT))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.CROP_GROWTH_MULTIPLIER))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.SPAWNER_RATES_MULTIPLIER))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.MOB_DROPS_MULTIPLIER))
                .addBatch();
        ((Map<String, Integer>) islandAttributes.getValue(IslandAttributes.Field.UPGRADES)).forEach((upgradeName, level) ->
                islandsUpgradesQuery.setObject(islandUUID)
                        .setObject(upgradeName)
                        .setObject(level)
                        .addBatch());
        String visitorHome = islandAttributes.getValue(IslandAttributes.Field.VISITOR_HOMES);
        if (visitorHome != null && !visitorHome.isEmpty())
            islandsVisitorHomesQuery.setObject(islandUUID)
                    .setObject(World.Environment.NORMAL.name())
                    .setObject(visitorHome)
                    .addBatch();
        ((List<Pair<UUID, Long>>) islandAttributes.getValue(IslandAttributes.Field.VISITORS)).forEach(visitor ->
                islandsVisitorsQuery.setObject(islandUUID)
                        .setObject(visitor.getKey().toString())
                        .setObject(visitor.getValue())
                        .addBatch());
        ((List<WarpCategoryAttributes>) islandAttributes.getValue(IslandAttributes.Field.WARP_CATEGORIES)).forEach(warpCategoryAttributes ->
                islandsWarpCategoriesQuery.setObject(islandUUID)
                        .setObject(warpCategoryAttributes.getValue(WarpCategoryAttributes.Field.NAME))
                        .setObject(warpCategoryAttributes.getValue(WarpCategoryAttributes.Field.SLOT))
                        .setObject(warpCategoryAttributes.getValue(WarpCategoryAttributes.Field.ICON))
                        .addBatch());
        ((List<IslandWarpAttributes>) islandAttributes.getValue(IslandAttributes.Field.WARPS)).forEach(islandWarpAttributes ->
                islandsWarpsQuery.setObject(islandUUID)
                        .setObject(islandWarpAttributes.getValue(IslandWarpAttributes.Field.NAME))
                        .setObject(islandWarpAttributes.getValue(IslandWarpAttributes.Field.CATEGORY))
                        .setObject(islandWarpAttributes.getValue(IslandWarpAttributes.Field.LOCATION))
                        .setObject(islandWarpAttributes.getValue(IslandWarpAttributes.Field.PRIVATE_STATUS))
                        .setObject(islandWarpAttributes.getValue(IslandWarpAttributes.Field.ICON))
                        .addBatch());
    }

    private <T> void runOnEnvironments(T[] arr, BiConsumer<T, World.Environment> consumer) {
        for (World.Environment environment : World.Environment.values()) {
            if (arr[environment.ordinal()] != null) {
                consumer.accept(arr[environment.ordinal()], environment);
            }
        }
    }

    private PlayerAttributes loadPlayer(ResultSetMapBridge resultSet) {
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

    private IslandAttributes loadIsland(ResultSetMapBridge resultSet) {
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

    private StackedBlockAttributes loadStackedBlock(ResultSetMapBridge resultSet) {
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

    private BankTransactionsAttributes loadBankTransaction(ResultSetMapBridge resultSet) {
        return new BankTransactionsAttributes()
                .setValue(BankTransactionsAttributes.Field.ISLAND, resultSet.get("island"))
                .setValue(BankTransactionsAttributes.Field.PLAYER, resultSet.get("player"))
                .setValue(BankTransactionsAttributes.Field.BANK_ACTION, resultSet.get("bankAction"))
                .setValue(BankTransactionsAttributes.Field.POSITION, resultSet.get("position"))
                .setValue(BankTransactionsAttributes.Field.TIME, resultSet.get("time"))
                .setValue(BankTransactionsAttributes.Field.FAILURE_REASON, resultSet.get("failureReason"))
                .setValue(BankTransactionsAttributes.Field.AMOUNT, resultSet.get("amount"));
    }

    public PlayerAttributes getPlayerAttributes(String uuid) {
        return loadedPlayers.stream().filter(playerAttributes ->
                        playerAttributes.getValue(PlayerAttributes.Field.UUID).equals(uuid))
                .findFirst()
                .orElse(null);
    }

}
