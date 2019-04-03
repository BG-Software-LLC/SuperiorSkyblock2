package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import com.bgsoftware.superiorskyblock.utils.jnbt.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.NBTInputStream;
import com.bgsoftware.superiorskyblock.utils.jnbt.Tag;
import com.bgsoftware.superiorskyblock.utils.threads.SuperiorThread;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"ResultOfMethodCallIgnored",  "WeakerAccess"})
public final class DataHandler {

    public SuperiorSkyblockPlugin plugin;
    private Connection conn;

    public DataHandler(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;

        File databaseFile = new File(plugin.getDataFolder(), "database.db");

        if(!databaseFile.exists()){
            try {
                databaseFile.getParentFile().mkdirs();
                databaseFile.createNewFile();
            }catch(Exception ex){
                ex.printStackTrace();
                return;
            }
        }

        try{
            String sqlURL = "jdbc:sqlite:" + databaseFile.getAbsolutePath().replace("\\", "/");
            conn = DriverManager.getConnection(sqlURL);
        }catch(Exception ex){
            ex.printStackTrace();
            Bukkit.getScheduler().runTask(plugin, () -> plugin.getServer().getPluginManager().disablePlugin(plugin));
            return;
        }

        loadOldDatabase();
        loadDatabase();
    }

    public void saveDatabase(boolean async) {
        if (async && Bukkit.isPrimaryThread()) {
            new Thread(() -> saveDatabase(false)).start();
            return;
        }

        List<Island> islands = new ArrayList<>();
        plugin.getGrid().getAllIslands().forEach(uuid -> islands.add(plugin.getGrid().getIsland(SSuperiorPlayer.of(uuid))));
        List<SuperiorPlayer> players = plugin.getPlayers().getAllPlayers();

        try{
            //Saving islands
            for(Island island : islands){
                conn.prepareStatement(((SIsland) island).getSaveStatement()).executeUpdate();
            }
            //Saving players
            for(SuperiorPlayer player : players){
                conn.prepareStatement(((SSuperiorPlayer) player).getSaveStatement()).executeUpdate();
            }
            //Saving grid
            conn.prepareStatement(plugin.getGrid().getSaveStatement()).executeUpdate();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("WeakerAccess")
    public void loadDatabase(){
        try{
            //Creating default tables
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS islands (owner VARCHAR PRIMARY KEY, center VARCHAR, teleportLocation VARCHAR, " +
                    "members VARCHAR, banned VARCHAR, permissionNodes VARCHAR, upgrades VARCHAR, warps VARCHAR, islandBank VARCHAR, " +
                    "islandSize INTEGER, blockLimits VARCHAR, teamLimit INTEGER, cropGrowth DECIMAL, spawnerRates DECIMAL," +
                    "mobDrops DECIMAL, discord VARCHAR, paypal VARCHAR);").executeUpdate();
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS players (player VARCHAR PRIMARY KEY, teamLeader VARCHAR, name VARCHAR, " +
                    "islandRole VARCHAR, textureValue VARCHAR);").executeUpdate();
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS grid (lastIsland VARCHAR, stackedBlocks VARCHAR, maxIslandSize INTEGER, world VARCHAR);").executeUpdate();

            addColumnIfNotExists("bonusWorth", "islands", "0");

            ResultSet resultSet = conn.prepareStatement("SELECT * FROM players;").executeQuery();
            while (resultSet.next()){
                plugin.getPlayers().loadPlayer(resultSet);
            }

            resultSet = conn.prepareStatement("SELECT * FROM islands;").executeQuery();
            while (resultSet.next()){
                plugin.getGrid().createIsland(resultSet);
            }

            resultSet = conn.prepareStatement("SELECT * FROM grid;").executeQuery();
            if (resultSet.next()){
                plugin.getGrid().loadGrid(resultSet);
                conn.prepareStatement("DELETE FROM grid;").executeUpdate();
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void closeConnection(){
        try{
            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    public void insertIsland(Island island){
        new SuperiorThread(() -> {
            try{
                if(!containsIsland(island)){
                    conn.prepareStatement(String.format("INSERT INTO islands VALUES('%s','%s','','','','','','','',0,'',0,0.0,0.0,0.0,'','','0');",
                            island.getOwner().getUniqueId(), FileUtil.fromLocation(island.getCenter()))).executeUpdate();
                }
                conn.prepareStatement(((SIsland) island).getSaveStatement()).executeUpdate();
            }catch(Exception ex){
                SuperiorSkyblockPlugin.log("Couldn't insert island of " + island.getOwner().getName() + ".");
                ex.printStackTrace();
            }
        }).start();
    }

    private boolean containsIsland(Island island){
        try{
            return conn.prepareStatement(
                    String.format("SELECT * FROM islands WHERE owner = '%s';", island.getOwner().getUniqueId())).executeQuery().next();
        }catch(Exception ex){
            SuperiorSkyblockPlugin.log("Couldn't check if island " + island.getOwner().getName() + " exists.");
            ex.printStackTrace();
            return false;
        }
    }

    public void deleteIsland(Island island){
        new SuperiorThread(() -> {
            try{
                conn.prepareStatement("DELETE FROM islands WHERE owner = '" + island.getOwner().getUniqueId() + "';").executeUpdate();
            }catch(Exception ex){
                SuperiorSkyblockPlugin.log("Couldn't delete island of " + island.getOwner().getName() + ".");
                ex.printStackTrace();
            }
        }).start();
    }

    public void insertPlayer(SuperiorPlayer player){
        new SuperiorThread(() -> {
            try{
                if(!containsPlayer(player)) {
                    conn.prepareStatement(String.format("INSERT INTO players VALUES('%s','','','','');",
                            player.getUniqueId())).executeUpdate();
                }
                conn.prepareStatement(((SSuperiorPlayer) player).getSaveStatement()).executeUpdate();
            }catch(Exception ex){
                SuperiorSkyblockPlugin.log("Couldn't insert the player " + player.getUniqueId() + ".");
                ex.printStackTrace();
            }
        }).start();
    }

    private boolean containsPlayer(SuperiorPlayer player){
        try{
            return conn.prepareStatement(
                    String.format("SELECT * FROM players WHERE player = '%s';", player.getUniqueId())).executeQuery().next();
        }catch(Exception ex){
            SuperiorSkyblockPlugin.log("Couldn't check if player " + player.getName() + " exists.");
            ex.printStackTrace();
            return false;
        }
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
                File copyFile = new File(plugin.getDataFolder(), "data-backup/islands/" + file.getName());
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
                File copyFile = new File(plugin.getDataFolder(), "data-backup/players/" + file.getName());
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
            File copyFile = new File(plugin.getDataFolder(), "data-backup/grid");
            copyFile.getParentFile().mkdirs();
            gridFile.renameTo(copyFile);
        }

    }

    @SuppressWarnings("SameParameterValue")
    private void addColumnIfNotExists(String column, String table, String def) {
        try{
            conn.prepareStatement("ALTER TABLE " + table + " ADD " + column + " VARCHAR DEFAULT '" + def + "';").executeUpdate();
        }catch(SQLException ex){
            if(!ex.getMessage().contains("duplicate"))
                ex.printStackTrace();
        }
    }

}
