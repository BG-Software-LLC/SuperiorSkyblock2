package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.api.factory.IslandsFactory;
import com.bgsoftware.superiorskyblock.api.factory.PlayersFactory;
import com.bgsoftware.superiorskyblock.api.handlers.FactoriesManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.player.SSuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public final class FactoriesHandler implements FactoriesManager {

    private IslandsFactory islandsFactory;
    private PlayersFactory playersFactory;

    @Override
    public void registerIslandsFactory(IslandsFactory islandsFactory) {
        Preconditions.checkNotNull(islandsFactory, "islandsFactory parameter cannot be null.");
        this.islandsFactory = islandsFactory;
    }

    @Override
    public void registerPlayersFactory(PlayersFactory playersFactory) {
        Preconditions.checkNotNull(playersFactory, "playersFactory parameter cannot be null.");
        this.playersFactory = playersFactory;
    }

    public Island createIsland(GridHandler grid, ResultSet resultSet) throws SQLException {
        SIsland island = new SIsland(grid, resultSet);
        return islandsFactory == null ? island : islandsFactory.createIsland(island);
    }

    public Island createIsland(SuperiorPlayer superiorPlayer, UUID uuid, Location location, String islandName, String schemName){
        SIsland island = new SIsland(superiorPlayer, uuid, location, islandName, schemName);
        return islandsFactory == null ? island : islandsFactory.createIsland(island);
    }

    public SuperiorPlayer createPlayer(ResultSet resultSet) throws SQLException {
        SSuperiorPlayer superiorPlayer = new SSuperiorPlayer(resultSet);
        return playersFactory == null ? superiorPlayer : playersFactory.createPlayer(superiorPlayer);
    }

    public SuperiorPlayer createPlayer(UUID player) {
        SSuperiorPlayer superiorPlayer = new SSuperiorPlayer(player);
        return playersFactory == null ? superiorPlayer : playersFactory.createPlayer(superiorPlayer);
    }

}
