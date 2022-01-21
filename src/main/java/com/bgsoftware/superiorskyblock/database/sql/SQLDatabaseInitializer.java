package com.bgsoftware.superiorskyblock.database.sql;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.database.bridge.GridDatabaseBridge;
import com.bgsoftware.superiorskyblock.handler.HandlerLoadException;

import java.io.File;

public final class SQLDatabaseInitializer {

    private static final SQLDatabaseInitializer instance = new SQLDatabaseInitializer();
    private SuperiorSkyblockPlugin plugin;

    private SQLDatabaseInitializer() {

    }

    public static SQLDatabaseInitializer getInstance() {
        return instance;
    }

    public void init(SuperiorSkyblockPlugin plugin) throws HandlerLoadException {
        this.plugin = plugin;

        DatabaseType database = DatabaseType.fromName(plugin.getSettings().getDatabase().getType());

        if (database == DatabaseType.SQLite)
            createSQLiteFile();

        if (!SQLHelper.createConnection(plugin)) {
            throw new HandlerLoadException("Couldn't connect to the database.\nMake sure all information is correct.",
                    HandlerLoadException.ErrorLevel.SERVER_SHUTDOWN);
        }

        createIslandsTable();
        createPlayersTable();
        createGridTable();
        createBankTransactionsTable();
        createStackedBlocksTable();

        if (!containsGrid())
            GridDatabaseBridge.insertGrid(plugin.getGrid());
    }

    public void createIndexes() {
        SQLHelper.executeUpdate("CREATE UNIQUE INDEX islands_bans_index ON " +
                "{prefix}islands_bans (island,player);", ignoreError -> {
        });

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX block_limits_index ON " +
                "{prefix}islands_block_limits (island,block);", ignoreError -> {
        });

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX islands_chests_index ON " +
                "{prefix}islands_chests (island,`index`);", ignoreError -> {
        });

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX islands_effects_index ON " +
                "{prefix}islands_effects (island,effect_type);", ignoreError -> {
        });

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX entity_limits_index ON " +
                "{prefix}islands_entity_limits (island,entity);", ignoreError -> {
        });

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX islands_flags_index ON " +
                "{prefix}islands_flags (island,name);", ignoreError -> {
        });

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX islands_generators_index ON " +
                "{prefix}islands_generators (island,environment,block);", ignoreError -> {
        });

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX islands_homes_index ON " +
                "{prefix}islands_homes (island,environment);", ignoreError -> {
        });

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX islands_members_index ON " +
                "{prefix}islands_members (island,player);", ignoreError -> {
        });

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX islands_missions_index ON " +
                "{prefix}islands_missions (island,name);", ignoreError -> {
        });

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX player_permissions_index ON " +
                "{prefix}islands_player_permissions (island,player,permission);", ignoreError -> {
        });

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX islands_ratings_index ON " +
                "{prefix}islands_ratings (island,player);", ignoreError -> {
        });

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX role_limits_index ON " +
                "{prefix}islands_role_limits (island,role);", ignoreError -> {
        });

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX role_permissions_index ON " +
                "{prefix}islands_role_permissions (island,permission);", ignoreError -> {
        });

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX islands_upgrades_index ON " +
                "{prefix}islands_upgrades (island,upgrade);", ignoreError -> {
        });

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX visitor_homes_index ON " +
                "{prefix}islands_visitor_homes (island,environment);", ignoreError -> {
        });

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX islands_visitors_index ON " +
                "{prefix}islands_visitors (island,player);", ignoreError -> {
        });

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX warp_categories_index ON " +
                "{prefix}islands_warp_categories (island,name);", ignoreError -> {
        });

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX islands_warps_index ON " +
                "{prefix}islands_warps (island,name);", ignoreError -> {
        });

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX players_missions_index ON " +
                "{prefix}players_missions (player,name);", ignoreError -> {
        });
    }

    public void close() {
        SQLHelper.close();
    }

    public void setJournalMode(String jounralMode) {
        SQLHelper.executeQuery(String.format("PRAGMA journal_mode=%s;", jounralMode), result -> {
        });
    }

    private void createSQLiteFile() throws HandlerLoadException {
        try {
            File file = new File(plugin.getDataFolder(), "database.db");
            if (!file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
                if (!file.createNewFile())
                    throw new HandlerLoadException("Cannot create database file",
                            HandlerLoadException.ErrorLevel.SERVER_SHUTDOWN);
            }
        } catch (Exception ex) {
            throw new HandlerLoadException(ex, HandlerLoadException.ErrorLevel.SERVER_SHUTDOWN);
        }
    }

    private void createIslandsTable() {
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands (" +
                "uuid UUID PRIMARY KEY, " +
                "owner UUID, " +
                "center TEXT, " +
                "creation_time BIGINT, " +
                "island_type TEXT, " +
                "discord TEXT, " +
                "paypal TEXT, " +
                "worth_bonus BIG_DECIMAL, " +
                "levels_bonus BIG_DECIMAL, " +
                "locked BOOLEAN, " +
                "ignored BOOLEAN, " +
                "name TEXT, " +
                "description TEXT, " +
                "generated_schematics INTEGER, " +
                "unlocked_worlds INTEGER, " +
                "last_time_updated BIGINT, " +
                "dirty_chunks TEXT, " +
                "block_counts TEXT" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_banks (" +
                "island UUID PRIMARY KEY, " +
                "balance BIG_DECIMAL, " +
                "last_interest_time BIGINT" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_bans (" +
                "island UUID, " +
                "player UUID, " +
                "banned_by UUID, " +
                "banned_time BIGINT" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_block_limits (" +
                "island UUID, " +
                "block UNIQUE_TEXT, " +
                "`limit` INTEGER" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_chests (" +
                "island UUID, " +
                "`index` INTEGER, " +
                "contents LONGTEXT" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_effects (" +
                "island UUID, " +
                "effect_type UNIQUE_TEXT, " +
                "level INTEGER" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_entity_limits (" +
                "island UUID, " +
                "entity UNIQUE_TEXT, " +
                "`limit` INTEGER" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_flags (" +
                "island UUID, " +
                "name UNIQUE_TEXT, " +
                "status INTEGER" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_generators (" +
                "island UUID, " +
                "environment VARCHAR(7), " +
                "block UNIQUE_TEXT, " +
                "rate INTEGER" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_homes (" +
                "island UUID, " +
                "environment VARCHAR(7), " +
                "location TEXT" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_members (" +
                "island UUID, " +
                "player UUID, " +
                "role INTEGER, " +
                "join_time BIGINT" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_missions (" +
                "island UUID, " +
                "name LONG_UNIQUE_TEXT, " +
                "finish_count INTEGER" +
                ");");

        SQLHelper.executeUpdate("ALTER TABLE {prefix}islands_missions MODIFY COLUMN name LONG_UNIQUE_TEXT",
                error -> {
                });

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_player_permissions (" +
                "island UUID, " +
                "player UUID, " +
                "permission UNIQUE_TEXT, " +
                "status BOOLEAN" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_ratings (" +
                "island UUID, " +
                "player UUID, " +
                "rating INTEGER, " +
                "rating_time BIGINT" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_role_limits (" +
                "island UUID, " +
                "role INTEGER, " +
                "`limit` INTEGER" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_role_permissions (" +
                "island UUID, " +
                "role INTEGER, " +
                "permission UNIQUE_TEXT" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_settings (" +
                "island UUID PRIMARY KEY, " +
                "size INTEGER, " +
                "bank_limit BIG_DECIMAL, " +
                "coops_limit INTEGER, " +
                "members_limit INTEGER, " +
                "warps_limit INTEGER, " +
                "crop_growth_multiplier DECIMAL, " +
                "spawner_rates_multiplier DECIMAL, " +
                "mob_drops_multiplier DECIMAL" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_upgrades (" +
                "island UUID, " +
                "upgrade LONG_UNIQUE_TEXT, " +
                "level INTEGER" +
                ");");

        SQLHelper.executeUpdate("ALTER TABLE {prefix}islands_upgrades MODIFY COLUMN upgrade LONG_UNIQUE_TEXT",
                error -> {
                });

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_visitor_homes (" +
                "island UUID, " +
                "environment VARCHAR(7), " +
                "location TEXT" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_visitors (" +
                "island UUID, " +
                "player UUID, " +
                "visit_time BIGINT" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_warp_categories (" +
                "island UUID, " +
                "name LONG_UNIQUE_TEXT, " +
                "slot INTEGER, " +
                "icon TEXT" +
                ");");

        SQLHelper.executeUpdate("ALTER TABLE {prefix}islands_warp_categories MODIFY COLUMN name LONG_UNIQUE_TEXT",
                error -> {
                });

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_warps (" +
                "island UUID, " +
                "name LONG_UNIQUE_TEXT, " +
                "category TEXT, " +
                "location TEXT, " +
                "private BOOLEAN, " +
                "icon TEXT" +
                ");");

        SQLHelper.executeUpdate("ALTER TABLE {prefix}islands_warps MODIFY COLUMN name LONG_UNIQUE_TEXT",
                error -> {
                });
    }

    private void createPlayersTable() {
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}players (" +
                "uuid UUID PRIMARY KEY, " +
                "last_used_name TEXT, " +
                "last_used_skin TEXT, " +
                "disbands INTEGER, " +
                "last_time_updated BIGINT" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}players_missions (" +
                "player UUID, " +
                "name LONG_UNIQUE_TEXT, " +
                "finish_count INTEGER" +
                ");");

        SQLHelper.executeUpdate("ALTER TABLE {prefix}players_missions MODIFY COLUMN name LONG_UNIQUE_TEXT",
                error -> {
                });

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}players_settings (" +
                "player UUID PRIMARY KEY, " +
                "language TEXT, " +
                "toggled_panel BOOLEAN, " +
                "border_color TEXT, " +
                "toggled_border BOOLEAN, " +
                "island_fly BOOLEAN" +
                ");");
    }

    private void createGridTable() {
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}grid (" +
                "last_island TEXT, " +
                "max_island_size INTEGER, " +
                "world TEXT" +
                ");");
    }

    private void createBankTransactionsTable() {
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}bank_transactions (" +
                "island UUID, " +
                "player UUID, " +
                "bank_action TEXT, " +
                "position INTEGER, " +
                "time BIGINT, " +
                "failure_reason TEXT," +
                "amount TEXT" +
                ");");
    }

    private void createStackedBlocksTable() {
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}stacked_blocks (" +
                "location LONG_UNIQUE_TEXT PRIMARY KEY, " +
                "block_type TEXT, " +
                "amount INTEGER" +
                ");");
        // Before v1.8.1.363, location column of stacked_blocks was limited to 30 chars.
        // In order to make sure all tables keep the large number, we modify the column to 255-chars long
        // each time the plugin attempts to create the table.
        // https://github.com/BG-Software-LLC/SuperiorSkyblock2/issues/730
        SQLHelper.executeUpdate("ALTER TABLE {prefix}stacked_blocks MODIFY COLUMN location LONG_UNIQUE_TEXT",
                error -> {
                });
    }

    private boolean containsGrid() {
        return SQLHelper.doesConditionExist("SELECT * FROM {prefix}grid;");
    }

    private enum DatabaseType {

        MySQL,
        SQLite;

        private static DatabaseType fromName(String name) {
            return name.equalsIgnoreCase("MySQL") ? MySQL : SQLite;
        }

    }

}
