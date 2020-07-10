package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.database.SQLHelper;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.exceptions.HandlerLoadException;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("WeakerAccess")
public final class DataHandler {

    private final SuperiorSkyblockPlugin plugin;
    private final DatabaseType database;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public DataHandler(SuperiorSkyblockPlugin plugin) throws HandlerLoadException {
        this.plugin = plugin;
        this.database = DatabaseType.fromName(plugin.getSettings().databaseType);

        if(database == DatabaseType.SQLite){
            try {
                File file = new File(plugin.getDataFolder(), "database.db");
                if (!file.exists()) {
                    try {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return;
                    }
                }
            }catch(Exception ex){
                throw new HandlerLoadException(ex, HandlerLoadException.ErrorLevel.SERVER_SHUTDOWN);
            }
        }

        if(!SQLHelper.createConnection(plugin)){
            throw new HandlerLoadException("Couldn't connect to the database.\nMake sure all information is correct.", HandlerLoadException.ErrorLevel.SERVER_SHUTDOWN);
        }

        loadDatabase();
    }

    public void saveDatabase(boolean async) {
        if (async && Bukkit.isPrimaryThread()) {
            Executor.async(() -> saveDatabase(false));
            return;
        }

        try{
            //Saving grid
            SQLHelper.executeUpdate("DELETE FROM {prefix}grid;");
            plugin.getGrid().executeGridInsertStatement(false);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("WeakerAccess")
    public void loadDatabase(){
        //Creating default islands table
        createIslandsTable();

        //Creating default players table
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

        //Creating default grid table
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}grid (" +
                "lastIsland TEXT, " +
                "stackedBlocks TEXT, " +
                "maxIslandSize INTEGER, " +
                "world TEXT, " +
                "dirtyChunks TEXT" +
                ");");

        if(!containsGrid())
            plugin.getGrid().executeGridInsertStatement(false);

        //Creating default stacked-blocks table
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS {prefix}stackedBlocks (" +
                "world TEXT, " +
                "x INTEGER, " +
                "y INTEGER, " +
                "z INTEGER, " +
                "amount TEXT," +
                "item TEXT" +
                ");");

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

        editColumn("members", "islands", "LONGTEXT");
        editColumn("banned", "islands", "LONGTEXT");
        editColumn("ratings", "islands", "LONGTEXT");
        editColumn("uniqueVisitors", "islands", "LONGTEXT");

        SuperiorSkyblockPlugin.log("Starting to load players...");

        SQLHelper.executeQuery("SELECT * FROM {prefix}players;", resultSet -> {
            boolean updateRoles = false;

            while (resultSet.next()) {
                plugin.getPlayers().loadPlayer(resultSet);

                if(!updateRoles) {
                    try {
                        Integer.parseInt(resultSet.getString("islandRole"));
                    } catch (NumberFormatException ex) {
                        updateRoles = true;
                    }
                }
            }

            if(updateRoles){
                for(PlayerRole playerRole : plugin.getPlayers().getRoles()){
                    SQLHelper.executeUpdate("UPDATE {prefix}players SET islandRole = '" + playerRole.getId() + "' WHERE islandRole = '" + playerRole + "';");
                }
            }
        });

        SuperiorSkyblockPlugin.log("Finished players!");
        SuperiorSkyblockPlugin.log("Starting to load islands...");

        SQLHelper.executeQuery("SELECT * FROM {prefix}islands;", resultSet -> {
            while (resultSet.next()) {
                plugin.getGrid().createIsland(resultSet);
            }
        });

        SuperiorSkyblockPlugin.log("Finished islands!");
        SuperiorSkyblockPlugin.log("Starting to load grid...");

        SQLHelper.executeQuery("SELECT * FROM {prefix}grid;", resultSet -> {
            if (resultSet.next()) {
                plugin.getGrid().loadGrid(resultSet);
            }
        });

        SuperiorSkyblockPlugin.log("Finished grid!");
        SuperiorSkyblockPlugin.log("Starting to load stacked blocks...");

        SQLHelper.executeQuery("SELECT * FROM {prefix}stackedBlocks;", resultSet -> {
            while (resultSet.next()) {
                plugin.getGrid().loadStackedBlocks(resultSet);
            }
        });

        SuperiorSkyblockPlugin.log("Finished stacked blocks!");

        /*
         *  Because of a bug caused leaders to be guests, I am looping through all the players and trying to fix it here.
         */

        for(SuperiorPlayer superiorPlayer : plugin.getPlayers().getAllPlayers()){
            if(superiorPlayer.getIslandLeader().getUniqueId().equals(superiorPlayer.getUniqueId()) && superiorPlayer.getIsland() != null && !superiorPlayer.getPlayerRole().isLastRole()){
                SuperiorSkyblockPlugin.log("[WARN] Seems like " + superiorPlayer.getName() + " is an island leader, but have a guest role - fixing it...");
                superiorPlayer.setPlayerRole(SPlayerRole.lastRole());
            }

            if(superiorPlayer.getIsland() != null && !((SIsland) superiorPlayer.getIsland()).checkMember(superiorPlayer)){
                SuperiorSkyblockPlugin.log("[WARN] Seems like " + superiorPlayer.getName() + "'s island had corrupted members. Fixing it...");
                ((SIsland) superiorPlayer.getIsland()).addMemberRaw(superiorPlayer);
            }
        }

    }

    private void createIslandsTable(){
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
                "islandEffects TEXT" +
                ");");
    }

    private String getDefaultSettings() {
        StringBuilder stringBuilder = new StringBuilder();
        plugin.getSettings().defaultSettings.forEach(setting -> stringBuilder.append(";").append(setting));
        return stringBuilder.length() == 0 ? stringBuilder.toString() : stringBuilder.substring(1);
    }

    private String getDefaultGenerator() {
        StringBuilder stringBuilder = new StringBuilder();
        plugin.getSettings().defaultGenerator.forEach((key, value) -> stringBuilder.append(",").append(key).append("=").append(value));
        return stringBuilder.length() == 0 ? stringBuilder.toString() : stringBuilder.substring(1);
    }

    public void closeConnection(){
        SQLHelper.close();
    }

    public void insertIsland(Island island){
        ((SIsland) island).executeInsertStatement(true);
    }

    public void deleteIsland(Island island){
        Executor.async(() -> SQLHelper.executeUpdate("DELETE FROM {prefix}islands WHERE owner = '" + island.getOwner().getUniqueId() + "';"));
    }

    public void insertPlayer(SuperiorPlayer player){
        ((SSuperiorPlayer) player).executeInsertStatement(true);
    }

    private boolean containsGrid(){
        return SQLHelper.doesConditionExist("SELECT * FROM {prefix}grid;");
    }

    private void addColumnIfNotExists(String column, String table, String def, String type) {
        String defaultSection = " DEFAULT " + def;

        if(database == DatabaseType.MySQL) {
            column = "COLUMN " + column;
            if(type.equals("TEXT"))
                defaultSection = "";
        }

        String statementStr = "ALTER TABLE {prefix}" + table + " ADD " + column + " " + type + defaultSection + ";";

        SQLHelper.buildStatement(statementStr, PreparedStatement::executeUpdate, ex -> {
            if(!ex.getMessage().toLowerCase().contains("duplicate")) {
                System.out.println("Statement: " + statementStr);
                ex.printStackTrace();
            }
        });
    }

    @SuppressWarnings("all")
    private void editColumn(String column, String table, String newType) {
        if(!isType(column, table, newType)){
            if(database == DatabaseType.SQLite){
                String tmpTable = "__tmp" + table;
                SQLHelper.buildStatement("ALTER TABLE {prefix}" + table + " RENAME TO " + tmpTable + ";", preparedStatement -> {
                    try{
                        preparedStatement.executeUpdate();
                    }catch(Throwable ex){
                        preparedStatement.executeQuery();
                    }
                }, Throwable::printStackTrace);
                createIslandsTable();
                SQLHelper.buildStatement("INSERT INTO {prefix}" + table + "  SELECT * FROM " + tmpTable + ";", PreparedStatement::executeUpdate, Throwable::printStackTrace);
                SQLHelper.buildStatement("DROP TABLE " + tmpTable + ";", PreparedStatement::executeUpdate, Throwable::printStackTrace);
            }
            else {
                String statementStr = "ALTER TABLE {prefix}" + table + " MODIFY COLUMN " + column + " " + newType + ";";
                SQLHelper.buildStatement(statementStr, PreparedStatement::executeUpdate, Throwable::printStackTrace);
            }
        }
    }

    private boolean isType(String column, String table, String type){
        AtomicBoolean sameType = new AtomicBoolean(false);
        if(database == DatabaseType.SQLite){
            SQLHelper.buildStatement("PRAGMA table_info({prefix}" + table + ");", preparedStatement -> {
                try(ResultSet resultSet = preparedStatement.executeQuery()){
                    while(resultSet.next()){
                        if(column.equals(resultSet.getString(2))){
                            sameType.set(type.equals(resultSet.getString(3)));
                            break;
                        }
                    }
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }, Throwable::printStackTrace);
        }
        else{
            //SQLHelper.buildStatement("SELECT data_type FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '{prefix}" + table + "' AND column_name = '" + column + "';", preparedStatement -> {
            SQLHelper.buildStatement("SHOW FIELDS FROM {prefix}" + table + ";", preparedStatement -> {
                try(ResultSet resultSet = preparedStatement.executeQuery()){
                    while (resultSet.next()){
                        if(column.equals(resultSet.getString("Field"))){
                            sameType.set(type.equals(resultSet.getString("Type")));
                            break;
                        }
                    }
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }, Throwable::printStackTrace);
        }
        return sameType.get();
    }

    private enum DatabaseType{

        MySQL,
        SQLite;

        private static DatabaseType fromName(String name){
            return name.equalsIgnoreCase("MySQL") ? MySQL : SQLite;
        }

    }

}
