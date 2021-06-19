package com.bgsoftware.superiorskyblock.data.sql;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.data.GridDatabaseBridge;
import com.bgsoftware.superiorskyblock.utils.exceptions.HandlerLoadException;
import org.bukkit.World;

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

        addMissingColumns();
    }

    public void close() {
        SQLHelper.close();
    }

    private void createSQLiteFile() throws HandlerLoadException {
        try {
            File file = new File(plugin.getDataFolder(), "database.db");
            if (!file.exists()) {
                if (!file.getParentFile().mkdirs())
                    throw new HandlerLoadException("Cannot create parent directories for database file",
                            HandlerLoadException.ErrorLevel.SERVER_SHUTDOWN);
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
                "owner VARCHAR(36) PRIMARY KEY, " +
                "center TEXT, " +
                "teleportLocation TEXT, " +
                "members LONGTEXT, " +
                "banned LONGTEXT, " +
                "permissionNodes TEXT, " +
                "upgrades TEXT, " +
                "warps TEXT, " +
                "islandBank TEXT, " +
                "islandSize INTEGER, " +
                "blockLimits TEXT, " +
                "teamLimit INTEGER, " +
                "cropGrowth DECIMAL, " +
                "spawnerRates DECIMAL," +
                "mobDrops DECIMAL, " +
                "discord TEXT, " +
                "paypal TEXT, " +
                "warpsLimit INTEGER, " +
                "bonusWorth TEXT, " +
                "locked BOOLEAN, " +
                "blockCounts TEXT, " +
                "name TEXT, " +
                "visitorsLocation TEXT, " +
                "description TEXT, " +
                "ratings LONGTEXT, " +
                "missions TEXT, " +
                "settings TEXT, " +
                "ignored BOOLEAN, " +
                "generator TEXT, " +
                "generatedSchematics TEXT, " +
                "schemName TEXT, " +
                "uniqueVisitors LONGTEXT, " +
                "unlockedWorlds TEXT," +
                "lastTimeUpdate INTEGER," +
                "dirtyChunks TEXT," +
                "entityLimits TEXT," +
                "bonusLevel TEXT," +
                "creationTime INTEGER," +
                "coopLimit INTEGER," +
                "islandEffects TEXT," +
                "islandChest LONGTEXT," +
                "uuid VARCHAR(36)," +
                "bankLimit TEXT," +
                "lastInterest INTEGER," +
                "roleLimits TEXT," +
                "warpCategories TEXT" +
                ");");
    }

    private void createPlayersTable() {
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}players (" +
                "player VARCHAR(36) PRIMARY KEY, " +
                "teamLeader VARCHAR(36), " +
                "name TEXT, " +
                "islandRole TEXT, " +
                "textureValue TEXT, " +
                "disbands INTEGER, " +
                "toggledPanel BOOLEAN," +
                "islandFly BOOLEAN," +
                "borderColor TEXT," +
                "lastTimeStatus TEXT," +
                "missions TEXT," +
                "language TEXT," +
                "toggledBorder BOOLEAN" +
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
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}bankTransactions (" +
                "island VARCHAR(36), " +
                "player VARCHAR(36), " +
                "bankAction TEXT, " +
                "position INTEGER, " +
                "time TEXT, " +
                "failureReason TEXT," +
                "amount TEXT" +
                ");");
    }

    private void createStackedBlocksTable() {
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}stackedBlocks (" +
                "world TEXT, " +
                "x INTEGER, " +
                "y INTEGER, " +
                "z INTEGER, " +
                "amount TEXT," +
                "item TEXT" +
                ");");
    }

    private void addMissingColumns() {
        addColumnIfNotExists("bonusWorth", "islands", "'0'", "TEXT");
        addColumnIfNotExists("warpsLimit", "islands", String.valueOf(plugin.getSettings().defaultWarpsLimit), "INTEGER");
        addColumnIfNotExists("disbands", "players", String.valueOf(plugin.getSettings().disbandCount), "INTEGER");
        addColumnIfNotExists("locked", "islands", "0", "BOOLEAN");
        addColumnIfNotExists("blockCounts", "islands", "''", "TEXT");
        addColumnIfNotExists("toggledPanel", "players", "0", "BOOLEAN");
        addColumnIfNotExists("islandFly", "players", "0", "BOOLEAN");
        addColumnIfNotExists("name", "islands", "''", "TEXT");
        addColumnIfNotExists("borderColor", "players", "'BLUE'", "TEXT");
        addColumnIfNotExists("lastTimeStatus", "players", "'-1'", "TEXT");
        addColumnIfNotExists("visitorsLocation", "islands", "''", "TEXT");
        addColumnIfNotExists("description", "islands", "''", "TEXT");
        addColumnIfNotExists("ratings", "islands", "''", "TEXT");
        addColumnIfNotExists("missions", "islands", "''", "TEXT");
        addColumnIfNotExists("missions", "players", "''", "TEXT");
        addColumnIfNotExists("settings", "islands", "'" + getDefaultSettings() + "'", "TEXT");
        addColumnIfNotExists("ignored", "islands", "0", "BOOLEAN");
        addColumnIfNotExists("generator", "islands", "'" + getDefaultGenerator() + "'", "TEXT");
        addColumnIfNotExists("generatedSchematics", "islands", "'normal'", "TEXT");
        addColumnIfNotExists("schemName", "islands", "''", "TEXT");
        addColumnIfNotExists("language", "players", "'en-US'", "TEXT");
        addColumnIfNotExists("uniqueVisitors", "islands", "''", "TEXT");
        addColumnIfNotExists("unlockedWorlds", "islands", "''", "TEXT");
        addColumnIfNotExists("toggledBorder", "players", "1", "BOOLEAN");
        addColumnIfNotExists("lastTimeUpdate", "islands", String.valueOf(System.currentTimeMillis() / 1000), "INTEGER");
        addColumnIfNotExists("dirtyChunks", "grid", "''", "TEXT");
        addColumnIfNotExists("dirtyChunks", "islands", "''", "TEXT");
        addColumnIfNotExists("entityLimits", "islands", "''", "TEXT");
        addColumnIfNotExists("bonusLevel", "islands", "'0'", "TEXT");
        addColumnIfNotExists("creationTime", "islands", (System.currentTimeMillis() / 1000) + "", "INTEGER");
        addColumnIfNotExists("coopLimit", "islands", String.valueOf(plugin.getSettings().defaultCoopLimit), "INTEGER");
        addColumnIfNotExists("islandEffects", "islands", "''", "TEXT");
        addColumnIfNotExists("item", "stackedBlocks", "''", "TEXT");
        addColumnIfNotExists("islandChest", "islands", "''", "LONGTEXT");
        addColumnIfNotExists("uuid", "islands", "''", "VARCHAR(36)");
        addColumnIfNotExists("bankLimit", "islands", "'-2'", "TEXT");
        addColumnIfNotExists("lastInterest", "islands", String.valueOf(System.currentTimeMillis() / 1000), "INTEGER");
        addColumnIfNotExists("roleLimits", "islands", "''", "TEXT");
        addColumnIfNotExists("warpCategories", "islands", "''", "TEXT");

        editColumn("members", "islands", "LONGTEXT");
        editColumn("banned", "islands", "LONGTEXT");
        editColumn("ratings", "islands", "LONGTEXT");
        editColumn("uniqueVisitors", "islands", "LONGTEXT");
    }

    private boolean containsGrid() {
        return SQLHelper.doesConditionExist("SELECT * FROM {prefix}grid;");
    }

    private String getDefaultSettings() {
        StringBuilder stringBuilder = new StringBuilder();
        plugin.getSettings().defaultSettings.forEach(setting -> stringBuilder.append(";").append(setting));
        return stringBuilder.length() == 0 ? stringBuilder.toString() : stringBuilder.substring(1);
    }

    private String getDefaultGenerator() {
        StringBuilder generatorsBuilder = new StringBuilder();
        for (int i = 0; i < plugin.getSettings().defaultGenerator.length; i++) {
            if (plugin.getSettings().defaultGenerator[i] != null) {
                StringBuilder generatorBuilder = new StringBuilder();
                World.Environment environment = World.Environment.values()[i];
                plugin.getSettings().defaultGenerator[i].forEach((key, value) ->
                        generatorBuilder.append(",").append(key).append("=").append(value));
                generatorsBuilder.append(";").append(environment).append(":")
                        .append(generatorBuilder.length() == 0 ? "" : generatorBuilder.toString().substring(1));
            }
        }
        return generatorsBuilder.length() == 0 ? "" : generatorsBuilder.toString().substring(1);
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
                System.out.println("Statement: " + statementStr);
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
