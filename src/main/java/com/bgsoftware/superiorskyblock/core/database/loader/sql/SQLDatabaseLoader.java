package com.bgsoftware.superiorskyblock.core.database.loader.sql;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.database.bridge.GridDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.database.loader.MachineStateDatabaseLoader;
import com.bgsoftware.superiorskyblock.core.database.sql.SQLHelper;
import com.bgsoftware.superiorskyblock.core.database.sql.session.QueryResult;
import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;

import java.sql.ResultSet;

public class SQLDatabaseLoader extends MachineStateDatabaseLoader {

    private final SuperiorSkyblockPlugin plugin;

    public SQLDatabaseLoader(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setState(State state) throws ManagerLoadException {
        if (!plugin.getFactory().hasCustomDatabaseBridge())
            super.setState(state);
    }

    protected void handleInitialize() throws ManagerLoadException {
        if (!SQLHelper.createConnection(plugin)) {
            throw new ManagerLoadException("Couldn't connect to the database.\nMake sure all information is correct.",
                    ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN);
        }

        createIslandsTable();
        createPlayersTable();
        createGridTable();
        createBankTransactionsTable();
        createStackedBlocksTable();

        SQLHelper.select("grid", "", new QueryResult<ResultSet>()
                .onFail(error -> GridDatabaseBridge.insertGrid(plugin.getGrid())));
    }

    @Override
    protected void handlePostInitialize() {
        SQLHelper.createIndex("islands_bans_index", "islands_bans",
                "island", "player");

        SQLHelper.createIndex("block_limits_index", "islands_block_limits",
                "island", "block");

        SQLHelper.createIndex("islands_chests_index", "islands_chests",
                "island", "`index`");

        SQLHelper.createIndex("islands_effects_index", "islands_effects",
                "island", "effect_type");

        SQLHelper.createIndex("entity_limits_index", "islands_entity_limits",
                "island", "entity");

        SQLHelper.createIndex("islands_flags_index", "islands_flags",
                "island", "name");

        SQLHelper.createIndex("islands_generators_index", "islands_generators",
                "island", "environment", "block");

        SQLHelper.createIndex("islands_homes_index", "islands_homes",
                "island", "environment");

        SQLHelper.createIndex("islands_members_index", "islands_members",
                "island", "player");

        SQLHelper.createIndex("islands_missions_index", "islands_missions",
                "island", "name");

        SQLHelper.createIndex("player_permissions_index", "islands_player_permissions",
                "island", "player", "permission");

        SQLHelper.createIndex("islands_ratings_index", "islands_ratings",
                "island", "player");

        SQLHelper.createIndex("role_limits_index", "islands_role_limits",
                "island", "role");

        SQLHelper.createIndex("role_permissions_index", "islands_role_permissions",
                "island", "permission");

        SQLHelper.createIndex("islands_upgrades_index", "islands_upgrades",
                "island", "upgrade");

        SQLHelper.createIndex("visitor_homes_index", "islands_visitor_homes",
                "island", "environment");

        SQLHelper.createIndex("islands_visitors_index", "islands_visitors",
                "island", "player");

        SQLHelper.createIndex("warp_categories_index", "islands_warp_categories",
                "island", "name");

        SQLHelper.createIndex("islands_warps_index", "islands_warps",
                "island", "name");

        SQLHelper.createIndex("players_missions_index", "players_missions",
                "player", "name");
    }

    @Override
    protected void handlePreLoadData() {
        SQLHelper.setJournalMode("MEMORY", QueryResult.EMPTY_QUERY_RESULT);
    }

    @Override
    protected void handlePostLoadData() {
        SQLHelper.setJournalMode("DELETE", QueryResult.EMPTY_QUERY_RESULT);
    }

    @Override
    protected void handleShutdown() {
        SQLHelper.close();
    }

    @SuppressWarnings("unchecked")
    private void createIslandsTable() {
        SQLHelper.createTable("islands",
                new Pair<>("uuid", "UUID PRIMARY KEY"),
                new Pair<>("owner", "UUID"),
                new Pair<>("center", "TEXT"),
                new Pair<>("creation_time", "BIGINT"),
                new Pair<>("island_type", "TEXT"),
                new Pair<>("discord", "TEXT"),
                new Pair<>("paypal", "TEXT"),
                new Pair<>("worth_bonus", "BIG_DECIMAL"),
                new Pair<>("levels_bonus", "BIG_DECIMAL"),
                new Pair<>("locked", "BOOLEAN"),
                new Pair<>("ignored", "BOOLEAN"),
                new Pair<>("name", "TEXT"),
                new Pair<>("description", "TEXT"),
                new Pair<>("generated_schematics", "INTEGER"),
                new Pair<>("unlocked_worlds", "INTEGER"),
                new Pair<>("last_time_updated", "BIGINT"),
                new Pair<>("dirty_chunks", "LONGTEXT"),
                new Pair<>("block_counts", "LONGTEXT")
        );

        SQLHelper.modifyColumnType("islands", "dirty_chunks", "LONGTEXT");
        SQLHelper.modifyColumnType("islands", "block_counts", "LONGTEXT");

        SQLHelper.createTable("islands_banks",
                new Pair<>("island", "UUID PRIMARY KEY"),
                new Pair<>("balance", "BIG_DECIMAL"),
                new Pair<>("last_interest_time", "BIGINT")
        );

        SQLHelper.createTable("islands_bans",
                new Pair<>("island", "UUID"),
                new Pair<>("player", "UUID"),
                new Pair<>("banned_by", "UUID"),
                new Pair<>("banned_time", "BIGINT")
        );

        SQLHelper.createTable("islands_block_limits",
                new Pair<>("island", "UUID"),
                new Pair<>("block", "UNIQUE_TEXT"),
                new Pair<>("`limit`", "INTEGER")
        );

        SQLHelper.createTable("islands_chests",
                new Pair<>("island", "UUID"),
                new Pair<>("`index`", "INTEGER"),
                new Pair<>("contents", "LONGBLOB")
        );

        SQLHelper.modifyColumnType("islands_chests", "contents", "LONGBLOB");

        SQLHelper.createTable("islands_custom_data",
                new Pair<>("island", "UUID PRIMARY KEY"),
                new Pair<>("data", "BLOB")
        );

        SQLHelper.createTable("islands_effects",
                new Pair<>("island", "UUID"),
                new Pair<>("effect_type", "UNIQUE_TEXT"),
                new Pair<>("level", "INTEGER")
        );

        SQLHelper.createTable("islands_entity_limits",
                new Pair<>("island", "UUID"),
                new Pair<>("entity", "UNIQUE_TEXT"),
                new Pair<>("`limit`", "INTEGER")
        );

        SQLHelper.createTable("islands_flags",
                new Pair<>("island", "UUID"),
                new Pair<>("name", "UNIQUE_TEXT"),
                new Pair<>("status", "INTEGER")
        );

        SQLHelper.createTable("islands_generators",
                new Pair<>("island", "UUID"),
                new Pair<>("environment", "VARCHAR(7)"),
                new Pair<>("block", "UNIQUE_TEXT"),
                new Pair<>("rate", "INTEGER")
        );

        SQLHelper.createTable("islands_homes",
                new Pair<>("island", "UUID"),
                new Pair<>("environment", "VARCHAR(7)"),
                new Pair<>("location", "TEXT")
        );

        SQLHelper.createTable("islands_members",
                new Pair<>("island", "UUID"),
                new Pair<>("player", "UUID"),
                new Pair<>("role", "INTEGER"),
                new Pair<>("join_time", "BIGINT")
        );

        SQLHelper.createTable("islands_missions",
                new Pair<>("island", "UUID"),
                new Pair<>("name", "LONG_UNIQUE_TEXT"),
                new Pair<>("finish_count", "INTEGER")
        );

        SQLHelper.modifyColumnType("islands_missions", "name", "LONG_UNIQUE_TEXT");

        SQLHelper.createTable("islands_player_permissions",
                new Pair<>("island", "UUID"),
                new Pair<>("player", "UUID"),
                new Pair<>("permission", "UNIQUE_TEXT"),
                new Pair<>("status", "BOOLEAN")
        );

        SQLHelper.createTable("islands_ratings",
                new Pair<>("island", "UUID"),
                new Pair<>("player", "UUID"),
                new Pair<>("rating", "INTEGER"),
                new Pair<>("rating_time", "BIGINT")
        );

        SQLHelper.createTable("islands_role_limits",
                new Pair<>("island", "UUID"),
                new Pair<>("role", "INTEGER"),
                new Pair<>("`limit`", "INTEGER")
        );

        SQLHelper.createTable("islands_role_permissions",
                new Pair<>("island", "UUID"),
                new Pair<>("role", "INTEGER"),
                new Pair<>("permission", "UNIQUE_TEXT")
        );

        SQLHelper.createTable("islands_settings",
                new Pair<>("island", "UUID PRIMARY KEY"),
                new Pair<>("size", "INTEGER"),
                new Pair<>("bank_limit", "BIG_DECIMAL"),
                new Pair<>("coops_limit", "INTEGER"),
                new Pair<>("members_limit", "INTEGER"),
                new Pair<>("warps_limit", "INTEGER"),
                new Pair<>("crop_growth_multiplier", "DECIMAL"),
                new Pair<>("spawner_rates_multiplier", "DECIMAL"),
                new Pair<>("mob_drops_multiplier", "DECIMAL")
        );

        // Up to 1.9.0.574, decimals would not be saved correctly in MySQL
        // This occurred because the field type was DECIMAL(10,0) instead of DECIMAL(10,2)
        // Updating the column types to "DECIMAL" again should fix the issue.
        // https://github.com/BG-Software-LLC/SuperiorSkyblock2/issues/1021
        SQLHelper.modifyColumnType("islands_settings", "crop_growth_multiplier", "DECIMAL");
        SQLHelper.modifyColumnType("islands_settings", "spawner_rates_multiplier", "DECIMAL");
        SQLHelper.modifyColumnType("islands_settings", "mob_drops_multiplier", "DECIMAL");

        SQLHelper.createTable("islands_upgrades",
                new Pair<>("island", "UUID"),
                new Pair<>("upgrade", "LONG_UNIQUE_TEXT"),
                new Pair<>("level", "INTEGER")
        );

        SQLHelper.modifyColumnType("islands_upgrades", "upgrade", "LONG_UNIQUE_TEXT");
        SQLHelper.removePrimaryKey("islands_upgrades", "island");

        SQLHelper.createTable("islands_visitor_homes",
                new Pair<>("island", "UUID"),
                new Pair<>("environment", "VARCHAR(7)"),
                new Pair<>("location", "TEXT")
        );

        SQLHelper.createTable("islands_visitors",
                new Pair<>("island", "UUID"),
                new Pair<>("player", "UUID"),
                new Pair<>("visit_time", "BIGINT")
        );

        SQLHelper.createTable("islands_warp_categories",
                new Pair<>("island", "UUID"),
                new Pair<>("name", "LONG_UNIQUE_TEXT"),
                new Pair<>("slot", "INTEGER"),
                new Pair<>("icon", "TEXT")
        );

        SQLHelper.modifyColumnType("islands_warp_categories", "name", "LONG_UNIQUE_TEXT");

        SQLHelper.createTable("islands_warps",
                new Pair<>("island", "UUID"),
                new Pair<>("name", "LONG_UNIQUE_TEXT"),
                new Pair<>("category", "TEXT"),
                new Pair<>("location", "TEXT"),
                new Pair<>("private", "BOOLEAN"),
                new Pair<>("icon", "TEXT")
        );

        SQLHelper.modifyColumnType("islands_warps", "name", "LONG_UNIQUE_TEXT");
    }

    @SuppressWarnings("unchecked")
    private void createPlayersTable() {
        SQLHelper.createTable("players",
                new Pair<>("uuid", "UUID PRIMARY KEY"),
                new Pair<>("last_used_name", "TEXT"),
                new Pair<>("last_used_skin", "TEXT"),
                new Pair<>("disbands", "INTEGER"),
                new Pair<>("last_time_updated", "BIGINT")
        );

        SQLHelper.createTable("players_custom_data",
                new Pair<>("player", "UUID PRIMARY KEY"),
                new Pair<>("data", "BLOB")
        );

        SQLHelper.createTable("players_missions",
                new Pair<>("player", "UUID"),
                new Pair<>("name", "LONG_UNIQUE_TEXT"),
                new Pair<>("finish_count", "INTEGER")
        );

        SQLHelper.modifyColumnType("players_missions", "name", "LONG_UNIQUE_TEXT");

        SQLHelper.createTable("players_settings",
                new Pair<>("player", "UUID PRIMARY KEY"),
                new Pair<>("language", "TEXT"),
                new Pair<>("toggled_panel", "BOOLEAN"),
                new Pair<>("border_color", "TEXT"),
                new Pair<>("toggled_border", "BOOLEAN"),
                new Pair<>("island_fly", "BOOLEAN")
        );
    }

    @SuppressWarnings("unchecked")
    private void createGridTable() {
        SQLHelper.createTable("grid",
                new Pair<>("last_island", "TEXT"),
                new Pair<>("max_island_size", "INTEGER"),
                new Pair<>("world", "TEXT")
        );
    }

    @SuppressWarnings("unchecked")
    private void createBankTransactionsTable() {
        SQLHelper.createTable("bank_transactions",
                new Pair<>("island", "UUID"),
                new Pair<>("player", "UUID"),
                new Pair<>("bank_action", "TEXT"),
                new Pair<>("position", "INTEGER"),
                new Pair<>("time", "BIGINT"),
                new Pair<>("failure_reason", "TEXT"),
                new Pair<>("amount", "TEXT")
        );
    }

    private void createStackedBlocksTable() {
        //noinspection unchecked
        SQLHelper.createTable("stacked_blocks",
                new Pair<>("location", "LONG_UNIQUE_TEXT PRIMARY KEY"),
                new Pair<>("block_type", "TEXT"),
                new Pair<>("amount", "INTEGER")
        );
        // Before v1.8.1.363, location column of stacked_blocks was limited to 30 chars.
        // In order to make sure all tables keep the large number, we modify the column to 255-chars long
        // each time the plugin attempts to create the table.
        // https://github.com/BG-Software-LLC/SuperiorSkyblock2/issues/730
        SQLHelper.modifyColumnType("stacked_blocks", "location", "LONG_UNIQUE_TEXT");
    }

}
