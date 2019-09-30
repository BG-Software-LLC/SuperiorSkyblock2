package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.database.CachedResultSet;
import com.bgsoftware.superiorskyblock.database.SQLHelper;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.utils.jnbt.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.NBTInputStream;
import com.bgsoftware.superiorskyblock.utils.jnbt.Tag;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"ResultOfMethodCallIgnored",  "WeakerAccess"})
public final class DataHandler {

    public SuperiorSkyblockPlugin plugin;

    public DataHandler(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;

        try {
            SQLHelper.init(new File(plugin.getDataFolder(), "database.db"));
            loadOldDatabase();
            loadDatabase();
        }catch(Exception ex){
            ex.printStackTrace();
            Executor.sync(Bukkit::shutdown);
        }
    }

    public void saveDatabase(boolean async) {
        if (async && Bukkit.isPrimaryThread()) {
            Executor.async(() -> saveDatabase(false));
            return;
        }

        try{
            //Saving grid
            SQLHelper.executeUpdate("DELETE FROM grid;");
            plugin.getGrid().executeGridInsertStatement(false);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("WeakerAccess")
    public void loadDatabase(){
        //Creating default islands table
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS islands (" +
                "owner VARCHAR PRIMARY KEY, " +
                "center VARCHAR, " +
                "teleportLocation VARCHAR, " +
                "members VARCHAR, " +
                "banned VARCHAR, " +
                "permissionNodes VARCHAR, " +
                "upgrades VARCHAR, " +
                "warps VARCHAR, " +
                "islandBank VARCHAR, " +
                "islandSize INTEGER, " +
                "blockLimits VARCHAR, " +
                "teamLimit INTEGER, " +
                "cropGrowth DECIMAL, " +
                "spawnerRates DECIMAL," +
                "mobDrops DECIMAL, " +
                "discord VARCHAR, " +
                "paypal VARCHAR, " +
                "warpsLimit INTEGER, " +
                "bonusWorth VARCHAR, " +
                "locked BOOLEAN, " +
                "blockCounts VARCHAR, " +
                "name VARCHAR, " +
                "visitorsLocation VARCHAR," +
                "description VARCHAR," +
                "ratings VARCHAR," +
                "missions VARCHAR," +
                "coop VARCHAR" +
                ");");

        //Creating default players table
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS players (" +
                "player VARCHAR PRIMARY KEY, " +
                "teamLeader VARCHAR, " +
                "name VARCHAR, " +
                "islandRole VARCHAR, " +
                "textureValue VARCHAR, " +
                "disbands INTEGER," +
                "toggledPanel BOOLEAN," +
                "islandFly BOOLEAN," +
                "borderColor VARCHAR," +
                "lastTimeStatus VARCHAR," +
                "missions VARCHAR" +
                ");");

        //Creating default grid table
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS grid (" +
                "lastIsland VARCHAR, " +
                "stackedBlocks VARCHAR, " +
                "maxIslandSize INTEGER, " +
                "world VARCHAR" +
                ");");

        if(!containsGrid())
            plugin.getGrid().executeGridInsertStatement(false);

        //Creating default stacked-blocks table
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS stackedBlocks (" +
                "world VARCHAR, " +
                "x INTEGER, " +
                "y INTEGER, " +
                "z INTEGER, " +
                "amount INTEGER" +
                ");");

        addColumnIfNotExists("bonusWorth", "islands", "'0'", "VARCHAR");
        addColumnIfNotExists("warpsLimit", "islands", String.valueOf(plugin.getSettings().defaultWarpsLimit), "INTEGER");
        addColumnIfNotExists("disbands", "players", String.valueOf(plugin.getSettings().disbandCount), "INTEGER");
        addColumnIfNotExists("locked", "islands", "0", "BOOLEAN");
        addColumnIfNotExists("blockCounts", "islands", "''", "VARCHAR");
        addColumnIfNotExists("toggledPanel", "players", "0", "BOOLEAN");
        addColumnIfNotExists("islandFly", "players", "0", "BOOLEAN");
        addColumnIfNotExists("name", "islands", "''", "VARCHAR");
        addColumnIfNotExists("borderColor", "players", "'BLUE'", "VARCHAR");
        addColumnIfNotExists("lastTimeStatus", "players", "'-1'", "VARCHAR");
        addColumnIfNotExists("visitorsLocation", "islands", "''", "VARCHAR");
        addColumnIfNotExists("description", "islands", "''", "VARCHAR");
        addColumnIfNotExists("ratings", "islands", "''", "VARCHAR");
        addColumnIfNotExists("missions", "islands", "''", "VARCHAR");
        addColumnIfNotExists("missions", "players", "''", "VARCHAR");
        addColumnIfNotExists("coop", "islands", "''", "VARCHAR");

        SuperiorSkyblockPlugin.log("Starting to load players...");

        SQLHelper.executeQuery("SELECT * FROM players;", resultSet -> {
            while (resultSet.next()) {
                plugin.getPlayers().loadPlayer(resultSet);
            }
        });

        ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("SuperiorSkyblock DB Thread").build());

        SuperiorSkyblockPlugin.log("Finished players!");
        SuperiorSkyblockPlugin.log("Starting to load islands...");

        SQLHelper.executeQuery("SELECT * FROM islands;", resultSet -> {
            SuperiorSkyblockPlugin.log("Received the result set. Starting to load everything...");
            while (resultSet.next()) {
                CachedResultSet cachedResultSet = new CachedResultSet(resultSet);
                executor.execute(() -> plugin.getGrid().createIsland(cachedResultSet));
            }

            try {
                executor.shutdown();
                executor.awaitTermination(1, TimeUnit.HOURS);
            }catch(Exception ex){
                ex.printStackTrace();
            }

            SuperiorSkyblockPlugin.log("Loading done!");
        });

        SuperiorSkyblockPlugin.log("Finished islands!");
        SuperiorSkyblockPlugin.log("Starting to load grid...");

        SQLHelper.executeQuery("SELECT * FROM grid;", resultSet -> {
            if (resultSet.next()) {
                plugin.getGrid().loadGrid(resultSet);
            }
        });

        SuperiorSkyblockPlugin.log("Finished grid!");
        SuperiorSkyblockPlugin.log("Starting to load stacked blocks...");

        SQLHelper.executeQuery("SELECT * FROM stackedBlocks;", resultSet -> {
            while (resultSet.next()) {
                plugin.getGrid().loadStackedBlocks(resultSet);
            }
        });

        SuperiorSkyblockPlugin.log("Finished stacked blocks!");
    }

    public void closeConnection(){
        SQLHelper.close();
    }

    public void insertIsland(Island island){
        Executor.async(() -> {
            if(!containsIsland(island)){
                ((SIsland) island).executeInsertStatement(true);
            }else {
                ((SIsland) island).executeUpdateStatement(true);
            }
        });
    }

    private boolean containsIsland(Island island){
        return SQLHelper.doesConditionExist(String.format("SELECT * FROM islands WHERE owner = '%s';", island.getOwner().getUniqueId()));
    }

    public void deleteIsland(Island island){
        Executor.async(() -> SQLHelper.executeUpdate("DELETE FROM islands WHERE owner = '" + island.getOwner().getUniqueId() + "';"));
    }

    public void insertPlayer(SuperiorPlayer player){
        if(!containsPlayer(player)) {
            ((SSuperiorPlayer) player).executeInsertStatement(true);
        }else{
            ((SSuperiorPlayer) player).executeUpdateStatement(true);
        }
    }

    private boolean containsPlayer(SuperiorPlayer player){
        return SQLHelper.doesConditionExist(String.format("SELECT * FROM players WHERE player = '%s';", player.getUniqueId()));
    }

    private boolean containsGrid(){
        return SQLHelper.doesConditionExist("SELECT * FROM grid;");
    }

    @SuppressWarnings({"ConstantConditions", "WeakerAccess"})
    public void loadOldDatabase(){
        File dataDir = new File(plugin.getDataFolder(), "data/islands");
        Tag tag;

        if(dataDir.exists()){
            for(File file : dataDir.listFiles()){
                try {
                    try(NBTInputStream stream = new NBTInputStream(new FileInputStream(file))){
                        tag = stream.readTag();
                        plugin.getGrid().createIsland((CompoundTag) tag);
                    }
                }catch(Exception ex){
                    ex.printStackTrace();
                    File copyFile = new File(plugin.getDataFolder(), "data/islands-backup/" + file.getName());
                    copyFile.getParentFile().mkdirs();
                    file.renameTo(copyFile);
                }
                File copyFile = new File(plugin.getDataFolder(), "database-backup/islands/" + file.getName());
                copyFile.getParentFile().mkdirs();
                file.renameTo(copyFile);
            }
            dataDir.delete();
        }

        dataDir = new File(plugin.getDataFolder(), "data/players");

        if(dataDir.exists()){
            for(File file : dataDir.listFiles()){
                try {
                    try(NBTInputStream stream = new NBTInputStream(new FileInputStream(file))){
                        tag = stream.readTag();
                        plugin.getPlayers().loadPlayer((CompoundTag) tag);
                    }
                }catch(Exception ex){
                    ex.printStackTrace();
                    File copyFile = new File(plugin.getDataFolder(), "data/players-backup/" + file.getName());
                    copyFile.getParentFile().mkdirs();
                    file.renameTo(copyFile);
                }
                File copyFile = new File(plugin.getDataFolder(), "database-backup/players/" + file.getName());
                copyFile.getParentFile().mkdirs();
                file.renameTo(copyFile);
            }
            dataDir.delete();
        }

        File gridFile = new File(plugin.getDataFolder(), "data/grid");

        if(gridFile.exists()){
            try{
                try(NBTInputStream stream = new NBTInputStream(new FileInputStream(gridFile))){
                    tag = stream.readTag();
                    plugin.getGrid().loadGrid((CompoundTag) tag);
                }
            }catch(Exception ex){
                ex.printStackTrace();
                File copyFile = new File(plugin.getDataFolder(), "data/grid-backup");
                copyFile.getParentFile().mkdirs();
                gridFile.renameTo(copyFile);
            }
            File copyFile = new File(plugin.getDataFolder(), "database-backup/grid");
            copyFile.getParentFile().mkdirs();
            gridFile.renameTo(copyFile);
        }

    }

    private void addColumnIfNotExists(String column, String table, String def, String type) {
        try(PreparedStatement statement = SQLHelper.buildStatement("ALTER TABLE " + table + " ADD " + column + " " + type + " DEFAULT " + def + ";")){
            statement.executeUpdate();
        }catch(SQLException ex){
            if(!ex.getMessage().contains("duplicate"))
                ex.printStackTrace();
        }
    }

}
