package com.bgsoftware.superiorskyblock.database;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.database.bridge.GridDatabaseBridge;
import com.bgsoftware.superiorskyblock.database.loader.DatabaseLoader;
import com.bgsoftware.superiorskyblock.database.loader.v1.DatabaseLoader_V1;
import com.bgsoftware.superiorskyblock.database.sql.SQLDatabaseInitializer;
import com.bgsoftware.superiorskyblock.handler.AbstractHandler;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.island.bank.SBankTransaction;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.handler.HandlerLoadException;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public final class DataHandler extends AbstractHandler {

    private final List<DatabaseLoader> databaseLoaders = new ArrayList<>();

    public DataHandler(SuperiorSkyblockPlugin plugin) {
        super(plugin);
    }

    @Override
    public void loadData() {
        throw new UnsupportedOperationException("Not supported for DataHandler.");
    }

    @Override
    public void loadDataWithException() throws HandlerLoadException {
        loadDatabaseLoaders();

        for(DatabaseLoader databaseLoader : databaseLoaders) {
            try {
                databaseLoader.loadData();
            }catch (Exception error) {
                throw new HandlerLoadException(error, "&cUnexpected error occurred while converting data:", HandlerLoadException.ErrorLevel.SERVER_SHUTDOWN);
            }
        }

        if (!plugin.getFactory().hasCustomDatabaseBridge()) {
            SQLDatabaseInitializer.getInstance().init(plugin);
        }

        for(DatabaseLoader databaseLoader : databaseLoaders) {
            try {
                databaseLoader.saveData();
            }catch (Exception error) {
                throw new HandlerLoadException(error, "&cUnexpected error occurred while saving data:", HandlerLoadException.ErrorLevel.SERVER_SHUTDOWN);
            }
        }

        if (!plugin.getFactory().hasCustomDatabaseBridge()) {
            SQLDatabaseInitializer.getInstance().createIndexes();
        }

        loadPlayers();
        loadIslands();
        loadGrid();
        loadBankTransactions();

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

    public void addDatabaseLoader(DatabaseLoader databaseLoader){
        this.databaseLoaders.add(databaseLoader);
    }

    public void saveDatabase(boolean async) {
        if (async && Bukkit.isPrimaryThread()) {
            Executor.async(() -> saveDatabase(false));
            return;
        }

        try {
            //Saving grid
            GridDatabaseBridge.deleteGrid(plugin.getGrid());
            GridDatabaseBridge.insertGrid(plugin.getGrid());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void closeConnection() {
        if (!plugin.getFactory().hasCustomDatabaseBridge()) {
            SQLDatabaseInitializer.getInstance().close();
        }
    }

    private void loadDatabaseLoaders() {
        DatabaseLoader_V1.register(this);
    }

    private void loadPlayers() {
        SuperiorSkyblockPlugin.log("Starting to load players...");

        DatabaseBridge playersLoader = plugin.getFactory().createDatabaseBridge((SuperiorPlayer) null);

        playersLoader.loadAllObjects("players", resultSet ->
                plugin.getPlayers().loadPlayer(new DatabaseResult(resultSet)));

        SuperiorSkyblockPlugin.log("Finished players!");
    }

    private void loadIslands() {
        SuperiorSkyblockPlugin.log("Starting to load islands...");

        DatabaseBridge islandsLoader = plugin.getFactory().createDatabaseBridge((Island) null);

        islandsLoader.loadAllObjects("islands", resultSet ->
                plugin.getGrid().createIsland(new DatabaseResult(resultSet)));

        SuperiorSkyblockPlugin.log("Finished islands!");
    }

    private void loadGrid() {
        SuperiorSkyblockPlugin.log("Starting to load grid...");

        DatabaseBridge gridLoader = plugin.getFactory().createDatabaseBridge((GridManager) null);

        gridLoader.loadAllObjects("grid",
                resultSet -> plugin.getGrid().loadGrid(new DatabaseResult(resultSet)));

        SuperiorSkyblockPlugin.log("Finished grid!");
    }

    private void loadBankTransactions() {
        if (BuiltinModules.BANK.bankLogs) {
            SuperiorSkyblockPlugin.log("Starting to load bank transactions...");

            DatabaseBridge islandsLoader = plugin.getFactory().createDatabaseBridge((Island) null);

            islandsLoader.loadAllObjects("bank_transactions", _resultSet -> {
                DatabaseResult resultSet = new DatabaseResult(_resultSet);
                try {
                    Island island = plugin.getGrid().getIslandByUUID(UUID.fromString(resultSet.getString("island")));
                    if (island != null)
                        island.getIslandBank().loadTransaction(new SBankTransaction(resultSet));
                } catch (Exception error) {
                    SuperiorSkyblockPlugin.log("&cError occurred while loading bank transaction:");
                    error.printStackTrace();
                }
            });

            SuperiorSkyblockPlugin.log("Finished bank transactions!");
        }
    }

}
