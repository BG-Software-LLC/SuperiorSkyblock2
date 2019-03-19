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
    private String sqlURL = "";

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

        sqlURL = "jdbc:sqlite:" + databaseFile.getAbsolutePath().replace("\\", "/");

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

        try (Connection conn = DriverManager.getConnection(sqlURL)) {
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
        try (Connection conn = DriverManager.getConnection(sqlURL)){
            //Creating default tables
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS islands (owner VARCHAR PRIMARY KEY, center VARCHAR, teleportLocation VARCHAR, " +
                    "members VARCHAR, banned VARCHAR, permissionNodes VARCHAR, upgrades VARCHAR, warps VARCHAR, islandBank VARCHAR, " +
                    "islandSize INTEGER, blockLimits VARCHAR, teamLimit INTEGER, cropGrowth DECIMAL, spawnerRates DECIMAL," +
                    "mobDrops DECIMAL, discord VARCHAR, paypal VARCHAR);").executeUpdate();
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS players (player VARCHAR PRIMARY KEY, teamLeader VARCHAR, name VARCHAR, " +
                    "islandRole VARCHAR, textureValue VARCHAR);").executeUpdate();
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS grid (lastIsland VARCHAR, stackedBlocks VARCHAR, maxIslandSize INTEGER);").executeUpdate();

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

    public void insertIsland(Island island){
        new SuperiorThread(() -> {
            try (Connection conn = DriverManager.getConnection(sqlURL)){
                conn.prepareStatement(String.format("INSERT INTO islands VALUES('%s','%s','','','','','','','',0,'',0,0.0,0.0,0.0,'','');",
                        island.getOwner().getUniqueId(), FileUtil.fromLocation(island.getCenter()))).executeUpdate();
                conn.prepareStatement(((SIsland) island).getSaveStatement()).executeUpdate();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }).start();
    }

    public void deleteIsland(Island island){
        new SuperiorThread(() -> {
            try (Connection conn = DriverManager.getConnection(sqlURL)){
                conn.prepareStatement("DELETE FROM islands WHERE owner='" + island.getOwner().getUniqueId() + "';").executeUpdate();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }).start();
    }

    public void insertPlayer(SuperiorPlayer player){
        new SuperiorThread(() -> {
            try (Connection conn = DriverManager.getConnection(sqlURL)){
                conn.prepareStatement(String.format("INSERT INTO players VALUES('%s','','','','');", player.getUniqueId())).executeUpdate();
                conn.prepareStatement(((SSuperiorPlayer) player).getSaveStatement()).executeUpdate();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }).start();
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
                file.delete();
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
                file.delete();
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
            gridFile.delete();
        }

    }

    private void addColumnIfNotExists(Connection conn, String column, String table, String def) throws SQLException {
        ResultSet resultSet = conn.prepareStatement("SELECT * FROM " + table + " LIMIT 1;").executeQuery();
        try{
            resultSet.findColumn(column);
        }catch(SQLException ex){
            conn.prepareStatement("ALTER TABLE " + table + " ADD " + column + " VARCHAR DEFAULT '" + def + "';").executeUpdate();
        }
    }

}
