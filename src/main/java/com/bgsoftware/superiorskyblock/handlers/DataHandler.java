package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.data.GridDatabaseBridge;
import com.bgsoftware.superiorskyblock.data.IslandsDatabaseBridge;
import com.bgsoftware.superiorskyblock.data.PlayersDatabaseBridge;
import com.bgsoftware.superiorskyblock.data.sql.SQLDatabaseInitializer;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.exceptions.HandlerLoadException;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.Bukkit;

@SuppressWarnings("WeakerAccess")
public final class DataHandler extends AbstractHandler {

    public DataHandler(SuperiorSkyblockPlugin plugin){
        super(plugin);
    }

    @Override
    public void loadData() {
        throw new UnsupportedOperationException("Not supported for DataHandler.");
    }

    @Override
    public void loadDataWithException() throws HandlerLoadException {
        if(!plugin.getFactory().hasCustomDatabaseBridge()){
            SQLDatabaseInitializer.getInstance().init(plugin);
        }

        /*
         *  Because of a bug caused leaders to be guests, I am looping through all the players and trying to fix it here.
         */

        for(SuperiorPlayer superiorPlayer : plugin.getPlayers().getAllPlayers()){
            if(superiorPlayer.getIslandLeader().getUniqueId().equals(superiorPlayer.getUniqueId()) && superiorPlayer.getIsland() != null && !superiorPlayer.getPlayerRole().isLastRole()){
                SuperiorSkyblockPlugin.log("[WARN] Seems like " + superiorPlayer.getName() + " is an island leader, but have a guest role - fixing it...");
                superiorPlayer.setPlayerRole(SPlayerRole.lastRole());
            }
        }
    }

    public void saveDatabase(boolean async) {
        if (async && Bukkit.isPrimaryThread()) {
            Executor.async(() -> saveDatabase(false));
            return;
        }

        try{
            //Saving grid
            GridDatabaseBridge.deleteGrid(plugin.getGrid());
            GridDatabaseBridge.insertGrid(plugin.getGrid());
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void closeConnection(){
        if(!plugin.getFactory().hasCustomDatabaseBridge()){
            SQLDatabaseInitializer.getInstance().close();
        }
    }

    public void insertIsland(Island island){
        IslandsDatabaseBridge.insertIsland(island);
    }

    public void deleteIsland(Island island, boolean async){
        if (async && Bukkit.isPrimaryThread()) {
            Executor.async(() -> deleteIsland(island, false));
            return;
        }

        IslandsDatabaseBridge.deleteIsland(island);
    }

    public void insertPlayer(SuperiorPlayer player){
        PlayersDatabaseBridge.insertPlayer(player);
    }

}
