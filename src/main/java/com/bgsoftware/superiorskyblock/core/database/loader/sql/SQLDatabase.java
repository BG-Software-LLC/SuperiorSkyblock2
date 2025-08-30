package com.bgsoftware.superiorskyblock.core.database.loader.sql;

import com.bgsoftware.common.databasebridge.sql.query.Column;
import com.bgsoftware.common.databasebridge.sql.query.QueryResult;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0.DatabaseUpgrade_V0;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v1.DatabaseUpgrade_V1;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v2.DatabaseUpgrade_V2;
import com.bgsoftware.superiorskyblock.core.database.sql.DBSession;
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

        DBSession.select("ssb_metadata", "", new QueryResult<ResultSet>()
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

    private static void createMetadataTable() {
        MutableInt databaseVersion = new MutableInt(0);

        // We check if "islands" table exists. If not, it's a new database,
        // therefore the DB version should be latest. Otherwise, 0.
        DBSession.select("islands", "", new QueryResult<ResultSet>()
                .onFail(error -> databaseVersion.set(getCurrentDatabaseVersion())));

        DBSession.createTable("ssb_metadata",
                new Column("version", "INTEGER DEFAULT " + databaseVersion.get())
        );
    }

    private static void createIslandsTable() {
        DBSession.createTable("islands",
                new Column("uuid", "UUID PRIMARY KEY"),
                new Column("owner", "UUID"),
                new Column("center", "TEXT"),
                new Column("creation_time", "BIGINT"),
                new Column("island_type", "TEXT"),
                new Column("discord", "TEXT"),
                new Column("paypal", "TEXT"),
                new Column("worth_bonus", "BIG_DECIMAL"),
                new Column("levels_bonus", "BIG_DECIMAL"),
                new Column("locked", "BOOLEAN"),
                new Column("ignored", "BOOLEAN"),
                new Column("name", "TEXT"),
                new Column("description", "TEXT"),
                new Column("generated_schematics", "INTEGER"),
                new Column("unlocked_worlds", "INTEGER"),
                new Column("last_time_updated", "BIGINT"),
                new Column("dirty_chunks", "LONGTEXT"),
                new Column("block_counts", "LONGTEXT"),
                new Column("entity_counts", "LONGTEXT")
        );

        DBSession.createTable("islands_banks",
                new Column("island", "UUID PRIMARY KEY"),
                new Column("balance", "BIG_DECIMAL"),
                new Column("last_interest_time", "BIGINT")
        );

        DBSession.createTable("islands_bans",
                new Column("island", "UUID"),
                new Column("player", "UUID"),
                new Column("banned_by", "UUID"),
                new Column("banned_time", "BIGINT")
        );

        DBSession.createTable("islands_block_limits",
                new Column("island", "UUID"),
                new Column("block", "UNIQUE_TEXT"),
                new Column("`limit`", "INTEGER")
        );

        DBSession.createTable("islands_chests",
                new Column("island", "UUID"),
                new Column("`index`", "INTEGER"),
                new Column("contents", "LONGBLOB")
        );

        DBSession.createTable("islands_custom_data",
                new Column("island", "UUID PRIMARY KEY"),
                new Column("data", "BLOB")
        );

        DBSession.createTable("islands_effects",
                new Column("island", "UUID"),
                new Column("effect_type", "UNIQUE_TEXT"),
                new Column("level", "INTEGER")
        );

        DBSession.createTable("islands_entity_limits",
                new Column("island", "UUID"),
                new Column("entity", "UNIQUE_TEXT"),
                new Column("`limit`", "INTEGER")
        );

        DBSession.createTable("islands_flags",
                new Column("island", "UUID"),
                new Column("name", "UNIQUE_TEXT"),
                new Column("status", "INTEGER")
        );

        DBSession.createTable("islands_generators",
                new Column("island", "UUID"),
                new Column("environment", "VARCHAR(7)"),
                new Column("block", "UNIQUE_TEXT"),
                new Column("rate", "INTEGER")
        );

        DBSession.createTable("islands_homes",
                new Column("island", "UUID"),
                new Column("environment", "VARCHAR(7)"),
                new Column("location", "TEXT")
        );

        DBSession.createTable("islands_members",
                new Column("island", "UUID"),
                new Column("player", "UUID"),
                new Column("role", "INTEGER"),
                new Column("join_time", "BIGINT")
        );

        DBSession.createTable("islands_missions",
                new Column("island", "UUID"),
                new Column("name", "LONG_UNIQUE_TEXT"),
                new Column("finish_count", "INTEGER")
        );

        DBSession.createTable("islands_player_permissions",
                new Column("island", "UUID"),
                new Column("player", "UUID"),
                new Column("permission", "UNIQUE_TEXT"),
                new Column("status", "BOOLEAN")
        );

        DBSession.createTable("islands_ratings",
                new Column("island", "UUID"),
                new Column("player", "UUID"),
                new Column("rating", "INTEGER"),
                new Column("rating_time", "BIGINT")
        );

        DBSession.createTable("islands_role_limits",
                new Column("island", "UUID"),
                new Column("role", "INTEGER"),
                new Column("`limit`", "INTEGER")
        );

        DBSession.createTable("islands_role_permissions",
                new Column("island", "UUID"),
                new Column("role", "INTEGER"),
                new Column("permission", "UNIQUE_TEXT")
        );

        DBSession.createTable("islands_settings",
                new Column("island", "UUID PRIMARY KEY"),
                new Column("size", "INTEGER"),
                new Column("bank_limit", "BIG_DECIMAL"),
                new Column("coops_limit", "INTEGER"),
                new Column("members_limit", "INTEGER"),
                new Column("warps_limit", "INTEGER"),
                new Column("crop_growth_multiplier", "DECIMAL"),
                new Column("spawner_rates_multiplier", "DECIMAL"),
                new Column("mob_drops_multiplier", "DECIMAL")
        );

        DBSession.createTable("islands_upgrades",
                new Column("island", "UUID"),
                new Column("upgrade", "LONG_UNIQUE_TEXT"),
                new Column("level", "INTEGER")
        );

        DBSession.createTable("islands_visitor_homes",
                new Column("island", "UUID"),
                new Column("environment", "VARCHAR(7)"),
                new Column("location", "TEXT")
        );

        DBSession.createTable("islands_visitors",
                new Column("island", "UUID"),
                new Column("player", "UUID"),
                new Column("visit_time", "BIGINT")
        );

        DBSession.createTable("islands_warp_categories",
                new Column("island", "UUID"),
                new Column("name", "LONG_UNIQUE_TEXT"),
                new Column("slot", "INTEGER"),
                new Column("icon", "TEXT")
        );

        DBSession.createTable("islands_warps",
                new Column("island", "UUID"),
                new Column("name", "LONG_UNIQUE_TEXT"),
                new Column("category", "TEXT"),
                new Column("location", "TEXT"),
                new Column("private", "BOOLEAN"),
                new Column("icon", "TEXT")
        );
    }

    private static void createPlayersTable() {
        DBSession.createTable("players",
                new Column("uuid", "UUID PRIMARY KEY"),
                new Column("last_used_name", "TEXT"),
                new Column("last_used_skin", "TEXT"),
                new Column("disbands", "INTEGER"),
                new Column("last_time_updated", "BIGINT")
        );

        DBSession.createTable("players_custom_data",
                new Column("player", "UUID PRIMARY KEY"),
                new Column("data", "BLOB")
        );

        DBSession.createTable("players_missions",
                new Column("player", "UUID"),
                new Column("name", "LONG_UNIQUE_TEXT"),
                new Column("finish_count", "INTEGER")
        );

        DBSession.createTable("players_settings",
                new Column("player", "UUID PRIMARY KEY"),
                new Column("language", "TEXT"),
                new Column("toggled_panel", "BOOLEAN"),
                new Column("border_color", "TEXT"),
                new Column("toggled_border", "BOOLEAN"),
                new Column("island_fly", "BOOLEAN")
        );
    }

    private static void createGridTable() {
        DBSession.createTable("grid",
                new Column("last_island", "TEXT"),
                new Column("max_island_size", "INTEGER"),
                new Column("world", "TEXT")
        );
    }

    private static void createBankTransactionsTable() {
        DBSession.createTable("bank_transactions",
                new Column("island", "UUID"),
                new Column("player", "UUID"),
                new Column("bank_action", "TEXT"),
                new Column("position", "INTEGER"),
                new Column("time", "BIGINT"),
                new Column("failure_reason", "TEXT"),
                new Column("amount", "TEXT")
        );
    }

    private static void createStackedBlocksTable() {
        DBSession.createTable("stacked_blocks",
                new Column("location", "LONG_UNIQUE_TEXT PRIMARY KEY"),
                new Column("block_type", "TEXT"),
                new Column("amount", "INTEGER")
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
