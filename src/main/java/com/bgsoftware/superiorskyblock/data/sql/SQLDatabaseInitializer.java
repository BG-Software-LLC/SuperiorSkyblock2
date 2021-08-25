package com.bgsoftware.superiorskyblock.data.sql;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.data.GridDatabaseBridge;
import com.bgsoftware.superiorskyblock.utils.exceptions.HandlerLoadException;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SQLDatabaseInitializer {

    private static final SQLDatabaseInitializer instance = new SQLDatabaseInitializer();

    public static SQLDatabaseInitializer getInstance() {
        return instance;
    }

    private SQLDatabaseInitializer() {

    }

    private DatabaseType database = DatabaseType.SQLite;
    private SuperiorSkyblockPlugin plugin;

    public void init(SuperiorSkyblockPlugin plugin) throws HandlerLoadException {
        this.plugin = plugin;

        this.database = DatabaseType.fromName(plugin.getSettings().databaseType);

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

    public void createIndexes(){
        SQLHelper.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS islands_bans_index ON " +
                "{prefix}islands_bans (island,player);");

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS block_limits_index ON " +
                "{prefix}islands_block_limits (island,block);");

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS islands_chests_index ON " +
                "{prefix}islands_chests (island,`index`);");

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS islands_effects_index ON " +
                "{prefix}islands_effects (island,effect_type);");

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS entity_limits_index ON " +
                "{prefix}islands_entity_limits (island,entity);");

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS islands_flags_index ON " +
                "{prefix}islands_flags (island,name);");

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS islands_generators_index ON " +
                "{prefix}islands_generators (island,environment,block);");

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS islands_homes_index ON " +
                "{prefix}islands_homes (island,environment);");

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS islands_members_index ON " +
                "{prefix}islands_members (island,player);");

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS islands_missions_index ON " +
                "{prefix}islands_missions (island,name);");

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS player_permissions_index ON " +
                "{prefix}islands_player_permissions (island,player,permission);");

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS islands_ratings_index ON " +
                "{prefix}islands_ratings (island,player);");

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS role_limits_index ON " +
                "{prefix}islands_role_limits (island,role);");

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS role_permissions_index ON " +
                "{prefix}islands_role_permissions (island,permission);");

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS islands_upgrades_index ON " +
                "{prefix}islands_upgrades (island,upgrade);");

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS visitor_homes_index ON " +
                "{prefix}islands_visitor_homes (island,environment);");

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS islands_visitors_index ON " +
                "{prefix}islands_visitors (island,player);");

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS warp_categories_index ON " +
                "{prefix}islands_warp_categories (island,name);");

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS islands_warps_index ON " +
                "{prefix}islands_warps (island,name);");

        SQLHelper.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS players_missions_index ON " +
                "{prefix}players_missions (player,name);");
    }

    public void close() {
        SQLHelper.close();
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
                "creation_time INTEGER, " +
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
                "last_time_updated INTEGER, " +
                "dirty_chunks TEXT, " +
                "block_counts TEXT" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_banks (" +
                "island UUID PRIMARY KEY, " +
                "balance BIG_DECIMAL, " +
                "last_interest_time INTEGER" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_bans (" +
                "island UUID, " +
                "player UUID, " +
                "banned_by UUID, " +
                "banned_time INTEGER" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_block_limits (" +
                "island UUID, " +
                "block TEXT, " +
                "`limit` INTEGER" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_chests (" +
                "island UUID, " +
                "`index` INTEGER, " +
                "contents TEXT" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_effects (" +
                "island UUID, " +
                "effect_type TEXT, " +
                "level INTEGER" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_entity_limits (" +
                "island UUID, " +
                "entity TEXT, " +
                "`limit` INTEGER" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_flags (" +
                "island UUID, " +
                "name TEXT, " +
                "status INTEGER" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_generators (" +
                "island UUID, " +
                "environment TEXT, " +
                "block TEXT, " +
                "rate INTEGER" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_homes (" +
                "island UUID, " +
                "environment TEXT, " +
                "location TEXT" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_members (" +
                "island UUID, " +
                "player UUID, " +
                "role INTEGER, " +
                "join_time INTEGER" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_missions (" +
                "island UUID, " +
                "name TEXT, " +
                "finish_count INTEGER" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_player_permissions (" +
                "island UUID, " +
                "player UUID, " +
                "permission TEXT, " +
                "status BOOLEAN" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_ratings (" +
                "island UUID, " +
                "player UUID, " +
                "rating INTEGER, " +
                "rating_time INTEGER" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_role_limits (" +
                "island UUID, " +
                "role INTEGER, " +
                "`limit` INTEGER" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_role_permissions (" +
                "island UUID, " +
                "role INTEGER, " +
                "permission TEXT" +
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
                "upgrade TEXT, " +
                "level INTEGER" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_visitor_homes (" +
                "island UUID, " +
                "environment TEXT, " +
                "location TEXT" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_visitors (" +
                "island UUID, " +
                "player UUID, " +
                "visit_time INTEGER" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_warp_categories (" +
                "island UUID, " +
                "name TEXT, " +
                "slot INTEGER, " +
                "icon TEXT" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}islands_warps (" +
                "island UUID, " +
                "name TEXT, " +
                "category TEXT, " +
                "location TEXT, " +
                "private BOOLEAN, " +
                "icon TEXT" +
                ");");
    }

    private void createPlayersTable() {
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}players (" +
                "uuid UUID PRIMARY KEY, " +
                "last_used_name TEXT, " +
                "last_used_skin TEXT, " +
                "disbands INTEGER, " +
                "last_time_updated INTEGER" +
                ");");

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}players_missions (" +
                "player UUID, " +
                "name TEXT, " +
                "finish_count INTEGER" +
                ");");

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
                "lastIsland TEXT, " +
                "stackedBlocks TEXT, " +
                "maxIslandSize INTEGER, " +
                "world TEXT, " +
                "dirtyChunks TEXT" +
                ");");
    }

    private void createBankTransactionsTable() {
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}bank_transactions (" +
                "island UUID, " +
                "player UUID, " +
                "bank_action TEXT, " +
                "position INTEGER, " +
                "time INTEGER, " +
                "failure_reason TEXT," +
                "amount TEXT" +
                ");");
    }

    private void createStackedBlocksTable() {
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}stacked_blocks (" +
                "location TEXT PRIMARY KEY, " +
                "block_type TEXT, " +
                "amount INTEGER" +
                ");");
    }

    private boolean containsGrid() {
        return SQLHelper.doesConditionExist("SELECT * FROM {prefix}grid;");
    }

    private void addColumnIfNotExists(String column, String table, String def, String type) {
        String defaultSection = " DEFAULT " + def;

        if (database == DatabaseType.MySQL) {
            column = "COLUMN " + column;
            if (type.equals("TEXT") || type.equals("LONGTEXT"))
                defaultSection = "";
        }

        String statementStr = "ALTER TABLE {prefix}" + table + " ADD " + column + " " + type + defaultSection + ";";

        SQLHelper.buildStatement(statementStr, PreparedStatement::executeUpdate, ex -> {
            if (!ex.getMessage().toLowerCase().contains("duplicate")) {
                SuperiorSkyblockPlugin.log("&cAn error occurred while running statement: " + statementStr);
                ex.printStackTrace();
            }
        });
    }

    private void editColumn(String column, String table, String newType) {
        if (!isType(column, table, newType)) {
            if (database == DatabaseType.SQLite) {
                String tmpTable = "__tmp" + table;
                SQLHelper.buildStatement("ALTER TABLE {prefix}" + table + " RENAME TO " + tmpTable + ";", preparedStatement -> {
                    try {
                        preparedStatement.executeUpdate();
                    } catch (Throwable ex) {
                        preparedStatement.executeQuery();
                    }
                }, Throwable::printStackTrace);
                createIslandsTable();
                SQLHelper.buildStatement("INSERT INTO {prefix}" + table + "  SELECT * FROM " + tmpTable + ";", PreparedStatement::executeUpdate, Throwable::printStackTrace);
                SQLHelper.buildStatement("DROP TABLE " + tmpTable + ";", PreparedStatement::executeUpdate, Throwable::printStackTrace);
            } else {
                String statementStr = "ALTER TABLE {prefix}" + table + " MODIFY COLUMN " + column + " " + newType + ";";
                SQLHelper.buildStatement(statementStr, PreparedStatement::executeUpdate, Throwable::printStackTrace);
            }
        }
    }

    private boolean isType(String column, String table, String type) {
        AtomicBoolean sameType = new AtomicBoolean(false);
        if (database == DatabaseType.SQLite) {
            SQLHelper.buildStatement("PRAGMA table_info({prefix}" + table + ");", preparedStatement -> {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        if (column.equals(resultSet.getString(2))) {
                            sameType.set(type.equals(resultSet.getString(3)));
                            break;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }, Throwable::printStackTrace);
        } else {
            SQLHelper.buildStatement("SHOW FIELDS FROM {prefix}" + table + ";", preparedStatement -> {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        if (column.equals(resultSet.getString("Field"))) {
                            sameType.set(type.equals(resultSet.getString("Type")));
                            break;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }, Throwable::printStackTrace);
        }
        return sameType.get();
    }

    private enum DatabaseType {

        MySQL,
        SQLite;

        private static DatabaseType fromName(String name) {
            return name.equalsIgnoreCase("MySQL") ? MySQL : SQLite;
        }

    }

}
