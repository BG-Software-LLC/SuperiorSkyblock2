package com.bgsoftware.superiorskyblock.core.database.loader.sql;

import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0.DatabaseUpgrade_V0;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v1.DatabaseUpgrade_V1;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v2.DatabaseUpgrade_V2;
import com.bgsoftware.superiorskyblock.core.database.sql.SQLHelper;
import com.bgsoftware.superiorskyblock.core.database.sql.session.QueryResult;
import com.bgsoftware.superiorskyblock.core.mutable.MutableInt;

import java.sql.ResultSet;

public class SQLDatabase {

    private static final Runnable[] DATABASE_UPGRADES = new Runnable[]{
            DatabaseUpgrade_V0.INSTANCE,
            DatabaseUpgrade_V1.INSTANCE,
            DatabaseUpgrade_V2.INSTANCE
    };

    private SQLDatabase() {

    }

    public static void initializeDatabase() {
        createMetadataTable();
        createIslandsTable();
        createPlayersTable();
        createGridTable();
        createBankTransactionsTable();
        createStackedBlocksTable();
    }

    public static UpgradeResult upgradeDatabase() {
        MutableInt databaseVersionMutable = new MutableInt(0);

        SQLHelper.select("ssb_metadata", "", new QueryResult<ResultSet>()
                .onSuccess(result -> databaseVersionMutable.set(result.getInt("version")))
                .onFail(error -> {
                }));

        int databaseVersion = databaseVersionMutable.get();
        while (databaseVersion < DATABASE_UPGRADES.length) {
            DATABASE_UPGRADES[databaseVersion++].run();
        }

        return new UpgradeResult(databaseVersion > databaseVersionMutable.get(), databaseVersion);
    }

    public static int getCurrentDatabaseVersion() {
        return DATABASE_UPGRADES.length;
    }

    @SuppressWarnings("unchecked")
    private static void createMetadataTable() {
        MutableInt databaseVersion = new MutableInt(0);

        // We check if "islands" table exists. If not, it's a new database,
        // therefore the DB version should be latest. Otherwise, 0.
        SQLHelper.select("islands", "", new QueryResult<ResultSet>()
                .onFail(error -> databaseVersion.set(getCurrentDatabaseVersion())));

        SQLHelper.createTable("ssb_metadata",
                new Pair<>("version", "INTEGER DEFAULT " + databaseVersion.get())
        );
    }

    @SuppressWarnings("unchecked")
    private static void createIslandsTable() {
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
                new Pair<>("block_counts", "LONGTEXT"),
                new Pair<>("entity_counts", "LONGTEXT")
        );

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

        SQLHelper.createTable("islands_upgrades",
                new Pair<>("island", "UUID"),
                new Pair<>("upgrade", "LONG_UNIQUE_TEXT"),
                new Pair<>("level", "INTEGER")
        );

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

        SQLHelper.createTable("islands_warps",
                new Pair<>("island", "UUID"),
                new Pair<>("name", "LONG_UNIQUE_TEXT"),
                new Pair<>("category", "TEXT"),
                new Pair<>("location", "TEXT"),
                new Pair<>("private", "BOOLEAN"),
                new Pair<>("icon", "TEXT")
        );
    }

    @SuppressWarnings("unchecked")
    private static void createPlayersTable() {
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
    private static void createGridTable() {
        SQLHelper.createTable("grid",
                new Pair<>("last_island", "TEXT"),
                new Pair<>("max_island_size", "INTEGER"),
                new Pair<>("world", "TEXT")
        );
    }

    @SuppressWarnings("unchecked")
    private static void createBankTransactionsTable() {
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

    @SuppressWarnings("unchecked")
    private static void createStackedBlocksTable() {
        SQLHelper.createTable("stacked_blocks",
                new Pair<>("location", "LONG_UNIQUE_TEXT PRIMARY KEY"),
                new Pair<>("block_type", "TEXT"),
                new Pair<>("amount", "INTEGER")
        );
    }

    public static class UpgradeResult {

        private final boolean upgraded;
        private final int databaseVersion;

        UpgradeResult(boolean upgraded, int databaseVersion) {
            this.upgraded = upgraded;
            this.databaseVersion = databaseVersion;
        }

        public boolean isUpgraded() {
            return upgraded;
        }

        public int getDatabaseVersion() {
            return databaseVersion;
        }

    }

}
