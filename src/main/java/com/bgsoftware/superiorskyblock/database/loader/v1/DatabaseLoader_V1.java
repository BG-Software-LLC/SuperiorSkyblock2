package com.bgsoftware.superiorskyblock.database.loader.v1;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.database.DataHandler;
import com.bgsoftware.superiorskyblock.database.loader.DatabaseLoader;
import com.bgsoftware.superiorskyblock.database.loader.v1.attributes.BankTransactionsAttributes;
import com.bgsoftware.superiorskyblock.database.loader.v1.attributes.GridAttributes;
import com.bgsoftware.superiorskyblock.database.loader.v1.attributes.IslandAttributes;
import com.bgsoftware.superiorskyblock.database.loader.v1.attributes.IslandChestAttributes;
import com.bgsoftware.superiorskyblock.database.loader.v1.attributes.IslandWarpAttributes;
import com.bgsoftware.superiorskyblock.database.loader.v1.attributes.PlayerAttributes;
import com.bgsoftware.superiorskyblock.database.loader.v1.attributes.StackedBlockAttributes;
import com.bgsoftware.superiorskyblock.database.loader.v1.attributes.WarpCategoryAttributes;
import com.bgsoftware.superiorskyblock.database.loader.v1.deserializer.IDeserializer;
import com.bgsoftware.superiorskyblock.database.loader.v1.deserializer.JsonDeserializer;
import com.bgsoftware.superiorskyblock.database.loader.v1.deserializer.MultipleDeserializer;
import com.bgsoftware.superiorskyblock.database.loader.v1.deserializer.RawDeserializer;
import com.bgsoftware.superiorskyblock.database.sql.SQLSession;
import com.bgsoftware.superiorskyblock.database.sql.StatementHolder;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.island.permissions.PlayerPermissionNode;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMap;
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public final class DatabaseLoader_V1 implements DatabaseLoader {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final UUID CONSOLE_UUID = new UUID(0, 0);

    private static File databaseFile;
    private static SQLSession sqlSession;

    private final List<PlayerAttributes> loadedPlayers = new ArrayList<>();
    private final List<IslandAttributes> loadedIslands = new ArrayList<>();
    private final List<StackedBlockAttributes> loadedBlocks = new ArrayList<>();
    private final List<BankTransactionsAttributes> loadedBankTransactions = new ArrayList<>();
    private GridAttributes gridAttributes;

    private final IDeserializer deserializer = new MultipleDeserializer(
            new JsonDeserializer(this), new RawDeserializer(this, plugin)
    );

    @Override
    public void loadData() {
        SuperiorSkyblockPlugin.log("&a[Database-Converter] Detected old database - starting to convert data...");

        sqlSession.executeQuery("SELECT * FROM {prefix}players;", resultSet -> {
            while (resultSet.next()) {
                loadedPlayers.add(loadPlayer(resultSet));
            }
        });

        SuperiorSkyblockPlugin.log("&a[Database-Converter] Found " + loadedPlayers.size() + " players in the database.");

        sqlSession.executeQuery("SELECT * FROM {prefix}islands;", resultSet -> {
            while (resultSet.next()) {
                loadedIslands.add(loadIsland(resultSet));
            }
        });

        SuperiorSkyblockPlugin.log("&a[Database-Converter] Found " + loadedIslands.size() + " islands in the database.");

        sqlSession.executeQuery("SELECT * FROM {prefix}stackedBlocks;", resultSet -> {
            while (resultSet.next()) {
                loadedBlocks.add(loadStackedBlock(resultSet));
            }
        });

        SuperiorSkyblockPlugin.log("&a[Database-Converter] Found " + loadedBlocks.size() + " stacked blocks in the database.");

        sqlSession.executeQuery("SELECT * FROM {prefix}bankTransactions;", resultSet -> {
            while (resultSet.next()) {
                loadedBankTransactions.add(loadBankTransaction(resultSet));
            }
        });

        SuperiorSkyblockPlugin.log("&a[Database-Converter] Found " + loadedBankTransactions.size() + " bank transactions in the database.");

        sqlSession.executeQuery("SELECT * FROM {prefix}grid;", resultSet -> {
            if (resultSet.next()) {
                gridAttributes = new GridAttributes()
                        .setValue(GridAttributes.Field.LAST_ISLAND, resultSet.getString("lastIsland"))
                        .setValue(GridAttributes.Field.MAX_ISLAND_SIZE, resultSet.getString("maxIslandSize"))
                        .setValue(GridAttributes.Field.WORLD, resultSet.getString("world"));
            }
        });

        AtomicBoolean failedBackup = new AtomicBoolean(true);

        if(!sqlSession.isUsingMySQL()) {
            sqlSession.close();
            if (databaseFile.renameTo(new File(databaseFile.getParentFile(), "database-bkp.db"))) {
                failedBackup.set(false);
            }
        }

        if(failedBackup.get()) {
            if(!sqlSession.isUsingMySQL()) {
                sqlSession = new SQLSession(plugin, false);
                sqlSession.createConnection();
            }

            failedBackup.set(false);

            sqlSession.executeUpdate("RENAME TABLE {prefix}islands TO {prefix}bkp_islands", failure -> failedBackup.set(true));
            sqlSession.executeUpdate("RENAME TABLE {prefix}players TO {prefix}bkp_players", failure -> failedBackup.set(true));
            sqlSession.executeUpdate("RENAME TABLE {prefix}grid TO {prefix}bkp_grid", failure -> failedBackup.set(true));
            sqlSession.executeUpdate("RENAME TABLE {prefix}stackedBlocks TO {prefix}bkp_stackedBlocks", failure -> failedBackup.set(true));
            sqlSession.executeUpdate("RENAME TABLE {prefix}bankTransactions TO {prefix}bkp_bankTransactions", failure -> failedBackup.set(true));
        }

        if(sqlSession.isUsingMySQL())
            sqlSession.close();

        if (failedBackup.get()) {
            SuperiorSkyblockPlugin.log("&c[Database-Converter] Failed to create a backup for the database file.");
        } else {
            SuperiorSkyblockPlugin.log("&a[Database-Converter] Successfully created a backup for the database.");
        }
    }

    @Override
    public void saveData() {
        savePlayers();
        saveIslands();
        saveStackedBlocks();
        saveBankTransactions();
        saveGrid();
    }

    public static void register(DataHandler dataHandler) {
        if (isDatabaseOldFormat())
            dataHandler.addDatabaseLoader(new DatabaseLoader_V1());
    }

    private static boolean isDatabaseOldFormat() {
        sqlSession = new SQLSession(plugin, true);

        if (!sqlSession.isUsingMySQL()) {
            databaseFile = new File(plugin.getDataFolder(), "database.db");

            if (!databaseFile.exists())
                return false;
        }

        if (!sqlSession.createConnection()) {
            sqlSession.close();
            return false;
        }

        if (!sqlSession.doesTableExist("stackedBlocks")) {
            sqlSession.close();
            return false;
        }

        return true;
    }

    private void savePlayers(){
        SuperiorSkyblockPlugin.log("&a[Database-Converter] Converting players...");

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

    private void saveIslands(){
        long currentTime = System.currentTimeMillis();

        SuperiorSkyblockPlugin.log("&a[Database-Converter] Converting islands...");

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

    private void saveStackedBlocks(){
        SuperiorSkyblockPlugin.log("&a[Database-Converter] Converting stacked blocks...");

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

    private void saveBankTransactions(){
        SuperiorSkyblockPlugin.log("&a[Database-Converter] Converting bank transactions...");

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

    private void saveGrid(){
        if(gridAttributes == null)
            return;

        SuperiorSkyblockPlugin.log("&a[Database-Converter] Converting grid data...");

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
                        .setObject(missionName.toLowerCase())
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
        ((Map<UUID, PlayerPermissionNode>) islandAttributes.getValue(IslandAttributes.Field.PLAYER_PERMISSIONS)).forEach((playerUUID, node) -> {
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

    private PlayerAttributes loadPlayer(ResultSet resultSet) throws SQLException {
        PlayerRole playerRole;

        try {
            playerRole = SPlayerRole.fromId(Integer.parseInt(resultSet.getString("islandRole")));
        } catch (Exception ex) {
            playerRole = SPlayerRole.of(resultSet.getString("islandRole"));
        }

        return new PlayerAttributes()
                .setValue(PlayerAttributes.Field.UUID, resultSet.getString("player"))
                .setValue(PlayerAttributes.Field.ISLAND_LEADER, resultSet.getString("teamLeader"))
                .setValue(PlayerAttributes.Field.LAST_USED_NAME, resultSet.getString("name"))
                .setValue(PlayerAttributes.Field.LAST_USED_SKIN, resultSet.getString("textureValue"))
                .setValue(PlayerAttributes.Field.ISLAND_ROLE, playerRole)
                .setValue(PlayerAttributes.Field.DISBANDS, resultSet.getInt("disbands"))
                .setValue(PlayerAttributes.Field.LAST_TIME_UPDATED, resultSet.getLong("lastTimeStatus"))
                .setValue(PlayerAttributes.Field.COMPLETED_MISSIONS, deserializer.deserializeMissions(resultSet.getString("missions")))
                .setValue(PlayerAttributes.Field.TOGGLED_PANEL, resultSet.getBoolean("toggledPanel"))
                .setValue(PlayerAttributes.Field.ISLAND_FLY, resultSet.getBoolean("islandFly"))
                .setValue(PlayerAttributes.Field.BORDER_COLOR, BorderColor.valueOf(resultSet.getString("borderColor")))
                .setValue(PlayerAttributes.Field.LANGUAGE, resultSet.getString("language"))
                .setValue(PlayerAttributes.Field.TOGGLED_BORDER, resultSet.getBoolean("toggledBorder")
                );
    }

    private IslandAttributes loadIsland(ResultSet resultSet) throws SQLException {
        UUID ownerUUID = UUID.fromString(resultSet.getString("owner"));
        UUID islandUUID;

        String uuidRaw = resultSet.getString("uuid");
        if (uuidRaw == null || uuidRaw.isEmpty()) {
            islandUUID = ownerUUID;
        } else {
            islandUUID = UUID.fromString(uuidRaw);
        }

        int generatedSchematics = 0;
        String generatedSchematicsRaw = resultSet.getString("generatedSchematics");
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
        String unlockedWorldsRaw = resultSet.getString("unlockedWorlds");
        try {
            unlockedWorlds = Integer.parseInt(unlockedWorldsRaw);
        } catch (Exception ex) {
            if (unlockedWorldsRaw.contains("nether"))
                unlockedWorlds |= 1;
            if (unlockedWorldsRaw.contains("the_end"))
                unlockedWorlds |= 2;
        }

        return new IslandAttributes()
                .setValue(IslandAttributes.Field.UUID, islandUUID.toString())
                .setValue(IslandAttributes.Field.OWNER, ownerUUID.toString())
                .setValue(IslandAttributes.Field.CENTER, resultSet.getString("center"))
                .setValue(IslandAttributes.Field.CREATION_TIME, resultSet.getLong("creationTime"))
                .setValue(IslandAttributes.Field.ISLAND_TYPE, resultSet.getString("schemName"))
                .setValue(IslandAttributes.Field.DISCORD, resultSet.getString("discord"))
                .setValue(IslandAttributes.Field.PAYPAL, resultSet.getString("paypal"))
                .setValue(IslandAttributes.Field.WORTH_BONUS, resultSet.getString("bonusWorth"))
                .setValue(IslandAttributes.Field.LEVELS_BONUS, resultSet.getString("bonusLevel"))
                .setValue(IslandAttributes.Field.LOCKED, resultSet.getBoolean("locked"))
                .setValue(IslandAttributes.Field.IGNORED, resultSet.getBoolean("ignored"))
                .setValue(IslandAttributes.Field.NAME, resultSet.getString("name"))
                .setValue(IslandAttributes.Field.DESCRIPTION, resultSet.getString("description"))
                .setValue(IslandAttributes.Field.GENERATED_SCHEMATICS, generatedSchematics)
                .setValue(IslandAttributes.Field.UNLOCKED_WORLDS, unlockedWorlds)
                .setValue(IslandAttributes.Field.LAST_TIME_UPDATED, resultSet.getLong("lastTimeUpdate"))
                .setValue(IslandAttributes.Field.DIRTY_CHUNKS, resultSet.getString("dirtyChunks"))
                .setValue(IslandAttributes.Field.BLOCK_COUNTS, resultSet.getString("blockCounts"))
                .setValue(IslandAttributes.Field.HOMES, deserializer.deserializeHomes(resultSet.getString("teleportLocation")))
                .setValue(IslandAttributes.Field.MEMBERS, deserializer.deserializePlayers(resultSet.getString("members")))
                .setValue(IslandAttributes.Field.BANS, deserializer.deserializePlayers(resultSet.getString("banned")))
                .setValue(IslandAttributes.Field.PLAYER_PERMISSIONS, deserializer.deserializePlayerPerms(resultSet.getString("permissionNodes")))
                .setValue(IslandAttributes.Field.ROLE_PERMISSIONS, deserializer.deserializeRolePerms(resultSet.getString("permissionNodes")))
                .setValue(IslandAttributes.Field.UPGRADES, deserializer.deserializeUpgrades(resultSet.getString("upgrades")))
                .setValue(IslandAttributes.Field.WARPS, deserializer.deserializeWarps(resultSet.getString("warps")))
                .setValue(IslandAttributes.Field.BLOCK_LIMITS, deserializer.deserializeBlockLimits(resultSet.getString("blockLimits")))
                .setValue(IslandAttributes.Field.RATINGS, deserializer.deserializeRatings(resultSet.getString("ratings")))
                .setValue(IslandAttributes.Field.MISSIONS, deserializer.deserializeMissions(resultSet.getString("missions")))
                .setValue(IslandAttributes.Field.ISLAND_FLAGS, deserializer.deserializeIslandFlags(resultSet.getString("settings")))
                .setValue(IslandAttributes.Field.GENERATORS, deserializer.deserializeGenerators(resultSet.getString("generator")))
                .setValue(IslandAttributes.Field.VISITORS, deserializer.deserializeVisitors(resultSet.getString("uniqueVisitors")))
                .setValue(IslandAttributes.Field.ENTITY_LIMITS, deserializer.deserializeEntityLimits(resultSet.getString("entityLimits")))
                .setValue(IslandAttributes.Field.EFFECTS, deserializer.deserializeEffects(resultSet.getString("islandEffects")))
                .setValue(IslandAttributes.Field.ISLAND_CHESTS, deserializer.deserializeIslandChests(resultSet.getString("islandChest")))
                .setValue(IslandAttributes.Field.ROLE_LIMITS, deserializer.deserializeRoleLimits(resultSet.getString("roleLimits")))
                .setValue(IslandAttributes.Field.WARP_CATEGORIES, deserializer.deserializeWarpCategories(resultSet.getString("warpCategories")))
                .setValue(IslandAttributes.Field.BANK_BALANCE, resultSet.getString("islandBank"))
                .setValue(IslandAttributes.Field.BANK_LAST_INTEREST, resultSet.getLong("lastInterest"))
                .setValue(IslandAttributes.Field.VISITOR_HOMES, resultSet.getString("visitorsLocation"))
                .setValue(IslandAttributes.Field.ISLAND_SIZE, resultSet.getInt("islandSize"))
                .setValue(IslandAttributes.Field.TEAM_LIMIT, resultSet.getInt("teamLimit"))
                .setValue(IslandAttributes.Field.WARPS_LIMIT, resultSet.getInt("warpsLimit"))
                .setValue(IslandAttributes.Field.CROP_GROWTH_MULTIPLIER, resultSet.getDouble("cropGrowth"))
                .setValue(IslandAttributes.Field.SPAWNER_RATES_MULTIPLIER, resultSet.getDouble("spawnerRates"))
                .setValue(IslandAttributes.Field.MOB_DROPS_MULTIPLIER, resultSet.getDouble("mobDrops"))
                .setValue(IslandAttributes.Field.COOP_LIMIT, resultSet.getInt("coopLimit"))
                .setValue(IslandAttributes.Field.BANK_LIMIT, resultSet.getString("bankLimit"));
    }

    private StackedBlockAttributes loadStackedBlock(ResultSet resultSet) throws SQLException {
        String world = resultSet.getString("world");
        int x = resultSet.getInt("x");
        int y = resultSet.getInt("y");
        int z = resultSet.getInt("z");
        String amount = resultSet.getString("amount");
        String blockType = resultSet.getString("item");

        return new StackedBlockAttributes()
                .setValue(StackedBlockAttributes.Field.LOCATION, world + ", " + x + ", " + y + ", " + z)
                .setValue(StackedBlockAttributes.Field.BLOCK_TYPE, blockType)
                .setValue(StackedBlockAttributes.Field.AMOUNT, amount);
    }

    private BankTransactionsAttributes loadBankTransaction(ResultSet resultSet) throws SQLException {
        return new BankTransactionsAttributes()
                .setValue(BankTransactionsAttributes.Field.ISLAND, resultSet.getString("island"))
                .setValue(BankTransactionsAttributes.Field.PLAYER, resultSet.getString("player"))
                .setValue(BankTransactionsAttributes.Field.BANK_ACTION, resultSet.getString("bankAction"))
                .setValue(BankTransactionsAttributes.Field.POSITION, resultSet.getInt("position"))
                .setValue(BankTransactionsAttributes.Field.TIME, resultSet.getString("time"))
                .setValue(BankTransactionsAttributes.Field.FAILURE_REASON, resultSet.getString("failureReason"))
                .setValue(BankTransactionsAttributes.Field.AMOUNT, resultSet.getString("amount"));
    }

    public PlayerAttributes getPlayerAttributes(String uuid) {
        return loadedPlayers.stream().filter(playerAttributes ->
                        playerAttributes.getValue(PlayerAttributes.Field.UUID).equals(uuid))
                .findFirst()
                .orElse(null);
    }

}
