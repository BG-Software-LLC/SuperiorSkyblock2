package com.bgsoftware.superiorskyblock.factory;

import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.factory.BanksFactory;
import com.bgsoftware.superiorskyblock.api.factory.DatabaseBridgeFactory;
import com.bgsoftware.superiorskyblock.api.factory.IslandsFactory;
import com.bgsoftware.superiorskyblock.api.factory.PlayersFactory;
import com.bgsoftware.superiorskyblock.api.handlers.FactoriesManager;
import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.handlers.StackedBlocksManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandBlocksTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandEntitiesTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.bank.IslandBank;
import com.bgsoftware.superiorskyblock.api.player.algorithm.PlayerTeleportAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.database.cache.CachedIslandInfo;
import com.bgsoftware.superiorskyblock.database.cache.CachedPlayerInfo;
import com.bgsoftware.superiorskyblock.database.cache.DatabaseCache;
import com.bgsoftware.superiorskyblock.database.sql.SQLDatabaseBridge;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.island.algorithms.DefaultIslandBlocksTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.island.algorithms.DefaultIslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.island.algorithms.DefaultIslandEntitiesTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.island.bank.SIslandBank;
import com.bgsoftware.superiorskyblock.player.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.player.algorithm.DefaultPlayerTeleportAlgorithm;
import com.bgsoftware.superiorskyblock.wrappers.SBlockOffset;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.google.common.base.Preconditions;
import org.bukkit.Location;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public final class FactoriesHandler implements FactoriesManager {

    private IslandsFactory islandsFactory;
    private PlayersFactory playersFactory;
    private BanksFactory banksFactory;
    private DatabaseBridgeFactory databaseBridgeFactory;

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

    @Override
    public void registerBanksFactory(BanksFactory banksFactory) {
        Preconditions.checkNotNull(banksFactory, "banksFactory parameter cannot be null.");
        this.banksFactory = banksFactory;
    }

    @Override
    public void registerDatabaseBridgeFactory(DatabaseBridgeFactory databaseBridgeFactory) {
        Preconditions.checkNotNull(databaseBridgeFactory, "databaseBridgeFactory parameter cannot be null.");
        this.databaseBridgeFactory = databaseBridgeFactory;
    }

    @Override
    public BlockOffset createBlockOffset(int offsetX, int offsetY, int offsetZ) {
        return SBlockOffset.fromOffsets(offsetX, offsetY, offsetZ);
    }

    @Override
    public BlockPosition createBlockPosition(String world, int blockX, int blockY, int blockZ) {
        Preconditions.checkNotNull(world, "world cannot be null.");
        return new SBlockPosition(world, blockX, blockY, blockZ);
    }

    @Override
    public BlockPosition createBlockPosition(Location location) {
        Preconditions.checkNotNull(location.getWorld(), "location's world cannot be null.");
        return new SBlockPosition(location);
    }

    public Optional<Island> createIsland(DatabaseCache<CachedIslandInfo> cache, DatabaseResult resultSet) {
        Optional<Island> island = SIsland.fromDatabase(cache, resultSet);

        if (!island.isPresent())
            return island;

        return islandsFactory == null ? island : island.map(islandsFactory::createIsland);
    }

    public Island createIsland(SuperiorPlayer superiorPlayer, UUID uuid, Location location, String islandName, String schemName) {
        SIsland island = new SIsland(superiorPlayer, uuid, location, islandName, schemName);
        return islandsFactory == null ? island : islandsFactory.createIsland(island);
    }

    public Optional<SuperiorPlayer> createPlayer(DatabaseCache<CachedPlayerInfo> databaseCache, DatabaseResult resultSet) {
        Optional<SuperiorPlayer> superiorPlayer = SSuperiorPlayer.fromDatabase(databaseCache, resultSet);

        if (!superiorPlayer.isPresent())
            return superiorPlayer;

        return playersFactory == null ? superiorPlayer : superiorPlayer.map(playersFactory::createPlayer);
    }

    public SuperiorPlayer createPlayer(UUID player) {
        SSuperiorPlayer superiorPlayer = new SSuperiorPlayer(player);
        return playersFactory == null ? superiorPlayer : playersFactory.createPlayer(superiorPlayer);
    }

    public IslandBank createIslandBank(Island island, Supplier<Boolean> isGiveInterestFailed) {
        SIslandBank islandBank = new SIslandBank(island, isGiveInterestFailed);
        return banksFactory == null ? islandBank : banksFactory.createIslandBank(island, islandBank);
    }

    public IslandCalculationAlgorithm createIslandCalculationAlgorithm(Island island) {
        return islandsFactory == null ? DefaultIslandCalculationAlgorithm.getInstance() :
                islandsFactory.createIslandCalculationAlgorithm(island);
    }

    public IslandBlocksTrackerAlgorithm createIslandBlocksTrackerAlgorithm(Island island) {
        return islandsFactory == null ? new DefaultIslandBlocksTrackerAlgorithm(island) :
                islandsFactory.createIslandBlocksTrackerAlgorithm(island);
    }

    public IslandEntitiesTrackerAlgorithm createIslandEntitiesTrackerAlgorithm(Island island) {
        return islandsFactory == null ? new DefaultIslandEntitiesTrackerAlgorithm(island) :
                islandsFactory.createIslandEntitiesTrackerAlgorithm(island);
    }

    public PlayerTeleportAlgorithm createPlayerTeleportAlgorithm(SuperiorPlayer superiorPlayer) {
        return playersFactory == null ? DefaultPlayerTeleportAlgorithm.getInstance() :
                playersFactory.createPlayerTeleportAlgorithm(superiorPlayer);
    }

    public boolean hasCustomDatabaseBridge() {
        return databaseBridgeFactory != null;
    }

    public DatabaseBridge createDatabaseBridge(Island island) {
        SQLDatabaseBridge databaseBridge = SQLDatabaseBridge.getInstance();
        return databaseBridgeFactory == null ? databaseBridge :
                databaseBridgeFactory.createIslandsDatabaseBridge(island, databaseBridge);
    }

    public DatabaseBridge createDatabaseBridge(SuperiorPlayer superiorPlayer) {
        SQLDatabaseBridge databaseBridge = SQLDatabaseBridge.getInstance();
        return databaseBridgeFactory == null ? databaseBridge :
                databaseBridgeFactory.createPlayersDatabaseBridge(superiorPlayer, databaseBridge);
    }

    public DatabaseBridge createDatabaseBridge(GridManager gridManager) {
        SQLDatabaseBridge databaseBridge = SQLDatabaseBridge.getInstance();
        return databaseBridgeFactory == null ? databaseBridge :
                databaseBridgeFactory.createGridDatabaseBridge(gridManager, databaseBridge);
    }

    public DatabaseBridge createDatabaseBridge(StackedBlocksManager stackedBlocksManager) {
        SQLDatabaseBridge databaseBridge = SQLDatabaseBridge.getInstance();
        return databaseBridgeFactory == null ? databaseBridge :
                databaseBridgeFactory.createStackedBlocksDatabaseBridge(stackedBlocksManager, databaseBridge);
    }

}
