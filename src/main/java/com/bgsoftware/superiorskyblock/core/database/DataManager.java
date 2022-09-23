package com.bgsoftware.superiorskyblock.core.database;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Manager;
import com.bgsoftware.superiorskyblock.core.database.bridge.GridDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.database.cache.CachedIslandInfo;
import com.bgsoftware.superiorskyblock.core.database.cache.CachedPlayerInfo;
import com.bgsoftware.superiorskyblock.core.database.cache.DatabaseCache;
import com.bgsoftware.superiorskyblock.core.database.loader.DatabaseLoader;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.SQLDatabaseLoader;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.DatabaseLoader_V1;
import com.bgsoftware.superiorskyblock.core.database.serialization.IslandsDeserializer;
import com.bgsoftware.superiorskyblock.core.database.serialization.PlayersDeserializer;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("WeakerAccess")
public class DataManager extends Manager {

    private final List<DatabaseLoader> databaseLoaders = new LinkedList<>();

    public DataManager(SuperiorSkyblockPlugin plugin) {
        super(plugin);
    }

    @Override
    public void loadData() throws ManagerLoadException {
        loadDatabaseLoaders();

        runState(DatabaseLoader.State.INITIALIZE);

        runState(DatabaseLoader.State.POST_INITIALIZE);

        runState(DatabaseLoader.State.PRE_LOAD_DATA);

        loadPlayers();
        loadIslands();
        loadGrid();

        runState(DatabaseLoader.State.POST_LOAD_DATA);

        /*
         *  Because of a bug caused leaders to be guests, I am looping through all the players and trying to fix it here.
         */

        for (SuperiorPlayer superiorPlayer : plugin.getPlayers().getAllPlayers()) {
            if (superiorPlayer.getIslandLeader().getUniqueId().equals(superiorPlayer.getUniqueId()) && superiorPlayer.getIsland() != null && !superiorPlayer.getPlayerRole().isLastRole()) {
                SuperiorSkyblockPlugin.log("[WARN] Seems like " + superiorPlayer.getName() + " is an island leader, but have a guest role - fixing it...");
                superiorPlayer.setPlayerRole(SPlayerRole.lastRole());
            }
        }
    }

    public void addDatabaseLoader(DatabaseLoader databaseLoader) {
        this.databaseLoaders.add(databaseLoader);
    }

    public void saveDatabase(boolean async) {
        if (async && Bukkit.isPrimaryThread()) {
            BukkitExecutor.async(() -> saveDatabase(false));
            return;
        }

        try {
            //Saving grid
            GridDatabaseBridge.deleteGrid(plugin.getGrid());
            GridDatabaseBridge.insertGrid(plugin.getGrid());
        } catch (Exception ex) {
            ex.printStackTrace();
            PluginDebugger.debug(ex);
        }
    }

    public void closeConnection() {
        for (DatabaseLoader databaseLoader : databaseLoaders) {
            try {
                databaseLoader.setState(DatabaseLoader.State.SHUTDOWN);
            } catch (Throwable ignored) {
            }
        }
    }

    private void loadDatabaseLoaders() {
        addDatabaseLoader(new CopyOldDatabase());
        addDatabaseLoader(new DatabaseLoader_V1());
        addDatabaseLoader(new SQLDatabaseLoader());
    }

    private void loadPlayers() {
        SuperiorSkyblockPlugin.log("Starting to load players...");

        DatabaseBridge playersLoader = plugin.getFactory().createDatabaseBridge((SuperiorPlayer) null);

        DatabaseCache<CachedPlayerInfo> databaseCache = new DatabaseCache<>();
        AtomicInteger playersCount = new AtomicInteger();
        long startTime = System.currentTimeMillis();

        PlayersDeserializer.deserializeMissions(playersLoader, databaseCache);
        PlayersDeserializer.deserializePlayerSettings(playersLoader, databaseCache);
        PlayersDeserializer.deserializePersistentDataContainer(playersLoader, databaseCache);

        playersLoader.loadAllObjects("players", resultSet -> {
            plugin.getPlayers().loadPlayer(databaseCache, new DatabaseResult(resultSet));
            playersCount.incrementAndGet();
        });

        long endTime = System.currentTimeMillis();

        SuperiorSkyblockPlugin.log("Finished loading " + playersCount.get() + " players (Took " + (endTime - startTime) + "ms)");
    }

    private void loadIslands() {
        SuperiorSkyblockPlugin.log("Starting to load islands...");

        DatabaseBridge islandsLoader = plugin.getFactory().createDatabaseBridge((Island) null);

        DatabaseCache<CachedIslandInfo> databaseCache = new DatabaseCache<>();
        AtomicInteger islandsCount = new AtomicInteger();
        long startTime = System.currentTimeMillis();

        IslandsDeserializer.deserializeIslandHomes(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeMembers(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeBanned(islandsLoader, databaseCache);
        IslandsDeserializer.deserializePlayerPermissions(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeRolePermissions(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeUpgrades(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeWarps(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeBlockLimits(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeRatings(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeMissions(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeIslandFlags(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeGenerators(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeVisitors(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeEntityLimits(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeEffects(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeIslandChest(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeRoleLimits(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeWarpCategories(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeIslandBank(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeVisitorHomes(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeIslandSettings(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeBankTransactions(islandsLoader, databaseCache);
        IslandsDeserializer.deserializePersistentDataContainer(islandsLoader, databaseCache);

        islandsLoader.loadAllObjects("islands", resultSet -> {
            plugin.getGrid().createIsland(databaseCache, new DatabaseResult(resultSet));
            islandsCount.incrementAndGet();
        });

        long endTime = System.currentTimeMillis();

        SuperiorSkyblockPlugin.log("Finished loading " + islandsCount.get() + " islands (Took " + (endTime - startTime) + "ms)");
    }


    private void loadGrid() {
        SuperiorSkyblockPlugin.log("Starting to load grid...");

        DatabaseBridge gridLoader = plugin.getFactory().createDatabaseBridge((GridManager) null);

        gridLoader.loadAllObjects("grid",
                resultSet -> plugin.getGrid().loadGrid(new DatabaseResult(resultSet)));

        SuperiorSkyblockPlugin.log("Finished grid!");
    }

    private void runState(DatabaseLoader.State state) throws ManagerLoadException {
        for (DatabaseLoader databaseLoader : databaseLoaders) {
            databaseLoader.setState(state);
        }
    }

    private class CopyOldDatabase implements DatabaseLoader {

        @Override
        public void setState(State state) throws ManagerLoadException {
            if (state == State.INITIALIZE) {
                File oldDataFile = new File(plugin.getDataFolder(), "database.db");
                if (oldDataFile.exists()) {
                    File newDataFile = new File(plugin.getDataFolder(), "datastore/database.db");
                    newDataFile.getParentFile().mkdirs();
                    if (!oldDataFile.renameTo(newDataFile))
                        throw new ManagerLoadException("Failed to move old database file", ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN);
                }
            }

        }
    }

}
