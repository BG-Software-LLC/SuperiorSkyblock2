package com.bgsoftware.superiorskyblock.data.loaders;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.List;

public final class DatabaseLoadedData {

    private final List<SuperiorPlayer> loadedPlayers;
    private final List<Island> loadedIslands;

    public DatabaseLoadedData(List<SuperiorPlayer> loadedPlayers, List<Island> loadedIslands){
        this.loadedPlayers = loadedPlayers;
        this.loadedIslands = loadedIslands;
    }

    public List<SuperiorPlayer> getLoadedPlayers() {
        return loadedPlayers;
    }

    public List<Island> getLoadedIslands() {
        return loadedIslands;
    }

}
