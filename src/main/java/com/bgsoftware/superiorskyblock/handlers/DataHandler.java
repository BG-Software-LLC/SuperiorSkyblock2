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
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"ResultOfMethodCallIgnored", "WeakerAccess"})
public final class DataHandler {

    public SuperiorSkyblockPlugin plugin;
    private Connection conn;

    public DataHandler(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;

        File databaseFile = new File(plugin.getDataFolder(), "database.db");

        if (!databaseFile.exists()) {
            try {
                databaseFile.getParentFile().mkdirs();
                databaseFile.createNewFile();
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        }

        try {
            Class.forName("org.sqlite.JDBC");
            String sqlURL = "jdbc:sqlite:" + databaseFile.getAbsolutePath().replace("\\", "/");
            conn = DriverManager.getConnection(sqlURL);
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            Bukkit.getScheduler().runTask(plugin, () -> plugin.getServer().getPluginManager().disablePlugin(plugin));
            return;
        }

        loadOldDatabase();
        loadDatabase();
    }

    public Connection getConnection() {
        return conn;
    }

    public void saveDatabase(boolean async) {
        if (async && Bukkit.isPrimaryThread()) {
            new Thread(() -> saveDatabase(false)).start();
            return;
        }

        List<Island> islands = new ArrayList<>();
        plugin.getGrid().getAllIslands().forEach(uuid -> islands.add(plugin.getGrid().getIsland(SSuperiorPlayer.of(uuid))));
        List<SuperiorPlayer> players = plugin.getPlayers().getAllPlayers();

        try {
            //Saving islands
            for (Island island : islands) {
                SIsland sIsland = (SIsland) island;
                if (sIsland != null)
                    sIsland.executeUpdateStatement();
            }

            //Saving players
            for (SuperiorPlayer player : players)
                ((SSuperiorPlayer) player).executeUpdateStatement();

            // Saving stacked blocks
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM stackedBlocks;")) {
                ps.executeUpdate();
            }
            plugin.getGrid().executeStackedBlocksInsertStatement(conn);

            //Saving grid
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM grid;")) {
                ps.executeUpdate();
            }
            plugin.getGrid().executeGridInsertStatement(conn);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void loadDatabase() {
        //Creating default tables and loading data...
        try {
            try (PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS islands (owner VARCHAR(36) PRIMARY KEY, center VARCHAR, teleportLocation VARCHAR, " +
                    "members VARCHAR, banned VARCHAR, permissionNodes VARCHAR, upgrades VARCHAR, warps VARCHAR, islandBank VARCHAR, " +
                    "islandSize INTEGER, blockLimits VARCHAR, teamLimit INTEGER, cropGrowth DECIMAL, spawnerRates DECIMAL," +
                    "mobDrops DECIMAL, discord VARCHAR, paypal VARCHAR, warpsLimit INTEGER);")) {
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS players (player VARCHAR(36) PRIMARY KEY, teamLeader VARCHAR(36), name VARCHAR(16), " +
                    "islandRole VARCHAR, textureValue VARCHAR, disbans INTEGER);")) {
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS grid (lastIsland VARCHAR, stackedBlocks VARCHAR, maxIslandSize INTEGER, world VARCHAR(36));")) {
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS stackedBlocks (world VARCHAR(36), x INTEGER, y INTEGER, z INTEGER, amount INTEGER);")) {
                ps.executeUpdate();
            }

            addColumnIfNotExists("bonusWorth", "islands", "0", "VARCHAR");
            addColumnIfNotExists("warpsLimit", "islands", String.valueOf(plugin.getSettings().defaultWarpsLimit), "INTEGER");
            addColumnIfNotExists("disbands", "players", String.valueOf(plugin.getSettings().disbandCount), "INTEGER");

            try (PreparedStatement ps = conn.prepareStatement("SELECT player,teamLeader,name,textureValue,islandRole,disbands FROM players;");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    plugin.getPlayers().loadPlayer(rs);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM islands;");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    plugin.getGrid().createIsland(rs);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement("SELECT lastIsland,stackedBlocks,maxIslandSize,world FROM grid;");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    plugin.getGrid().loadGrid(rs);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement("SELECT world,x,y,z,amount FROM stackedBlocks;");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    plugin.getGrid().loadStackedBlocks(rs);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void insertIsland(Island island) {
        new SuperiorThread(() -> {
            if (!containsIsland(island)) {
                try (PreparedStatement ps = conn.prepareStatement(String.format("INSERT INTO islands VALUES('%s','%s','','','','','','','',0,'',0,0.0,0.0,0.0,'','','0',%d);",
                        island.getOwner().getUniqueId(), FileUtil.fromLocation(island.getCenter()), plugin.getSettings().defaultWarpsLimit))) {
                    ps.executeUpdate();
                    ((SIsland) island).executeUpdateStatement();
                } catch (SQLException ex) {
                    SuperiorSkyblockPlugin.log("Couldn't insert island of " + island.getOwner().getName() + ".");
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    private boolean containsIsland(Island island) {
        try (PreparedStatement ps = conn.prepareStatement(
                String.format("SELECT * FROM islands WHERE owner = '%s';", island.getOwner().getUniqueId()));
             ResultSet rs = ps.executeQuery()) {
            return rs.next();
        } catch (Exception ex) {
            SuperiorSkyblockPlugin.log("Couldn't check if island " + island.getOwner().getName() + " exists.");
            ex.printStackTrace();
            return false;
        }
    }

    public void deleteIsland(Island island) {
        new SuperiorThread(() -> {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM islands WHERE owner=?;")) {
                ps.setString(1, island.getOwner().getUniqueId().toString());
                ps.executeUpdate();
            } catch (Exception ex) {
                SuperiorSkyblockPlugin.log("Couldn't delete island of " + island.getOwner().getName() + ".");
                ex.printStackTrace();
            }
        }).start();
    }

    public void insertPlayer(SuperiorPlayer player) {
        if (!containsPlayer(player)) {
            ((SSuperiorPlayer) player).executeInsertStatement();
        } else {
            ((SSuperiorPlayer) player).executeUpdateStatement();
        }
    }

    private boolean containsPlayer(SuperiorPlayer player) {
        try (PreparedStatement ps = conn.prepareStatement(
                String.format("SELECT 1 FROM players WHERE player = '%s';", player.getUniqueId()));
             ResultSet rs = ps.executeQuery()) {
            return rs.next();
        } catch (Exception ex) {
            SuperiorSkyblockPlugin.log("Couldn't check if player " + player.getName() + " exists.");
            ex.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void loadOldDatabase() {
        File dataDir = new File(plugin.getDataFolder(), "data/islands");
        Tag tag;

        if (dataDir.exists()) {
            for (File file : dataDir.listFiles()) {
                try (NBTInputStream stream = new NBTInputStream(new FileInputStream(file))) {
                    tag = stream.readTag();
                    plugin.getGrid().createIsland((CompoundTag) tag);
                } catch (IOException ex) {
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

        if (dataDir.exists()) {
            for (File file : dataDir.listFiles()) {
                try (NBTInputStream stream = new NBTInputStream(new FileInputStream(file))) {
                    tag = stream.readTag();
                    plugin.getPlayers().loadPlayer((CompoundTag) tag);
                } catch (IOException ex) {
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

        if (gridFile.exists()) {
            try (NBTInputStream stream = new NBTInputStream(new FileInputStream(gridFile))) {
                tag = stream.readTag();
                plugin.getGrid().loadGrid((CompoundTag) tag);
            } catch (IOException ex) {
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
        try (PreparedStatement ps = conn.prepareStatement("ALTER TABLE " + table + " ADD " + column + " " + type + " DEFAULT '" + def + "';")) {
            ps.executeUpdate();
        } catch (SQLException ex) {
            if (!ex.getMessage().contains("duplicate"))
                ex.printStackTrace();
        }
    }
}