package com.bgsoftware.superiorskyblock.core.factory;

import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.enums.BankAction;
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
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.island.bank.IslandBank;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;
import com.bgsoftware.superiorskyblock.api.player.algorithm.PlayerTeleportAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import com.bgsoftware.superiorskyblock.core.SBlockOffset;
import com.bgsoftware.superiorskyblock.core.SBlockPosition;
import com.bgsoftware.superiorskyblock.core.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.core.database.bridge.IslandsDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.database.bridge.PlayersDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.database.cache.CachedIslandInfo;
import com.bgsoftware.superiorskyblock.core.database.cache.CachedPlayerInfo;
import com.bgsoftware.superiorskyblock.core.database.cache.DatabaseCache;
import com.bgsoftware.superiorskyblock.core.database.sql.SQLDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.persistence.PersistentDataContainerImpl;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.island.algorithm.DefaultIslandBlocksTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.island.algorithm.DefaultIslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.island.algorithm.DefaultIslandEntitiesTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.island.bank.SBankTransaction;
import com.bgsoftware.superiorskyblock.island.bank.SIslandBank;
import com.bgsoftware.superiorskyblock.player.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.player.algorithm.DefaultPlayerTeleportAlgorithm;
import com.google.common.base.Preconditions;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class FactoriesManagerImpl implements FactoriesManager {

    private IslandsFactory islandsFactory;
    private PlayersFactory playersFactory;
    private BanksFactory banksFactory;
    private DatabaseBridgeFactory databaseBridgeFactory;

    @Override
    public void registerIslandsFactory(IslandsFactory islandsFactory) {
        Preconditions.checkNotNull(islandsFactory, "islandsFactory parameter cannot be null.");
        this.islandsFactory = islandsFactory;
    }

    @Nullable
    @Override
    public IslandsFactory getIslandsFactory() {
        return islandsFactory;
    }

    @Override
    public void registerPlayersFactory(PlayersFactory playersFactory) {
        Preconditions.checkNotNull(playersFactory, "playersFactory parameter cannot be null.");
        this.playersFactory = playersFactory;
    }

    @Nullable
    @Override
    public PlayersFactory getPlayersFactory() {
        return playersFactory;
    }

    @Override
    public void registerBanksFactory(BanksFactory banksFactory) {
        Preconditions.checkNotNull(banksFactory, "banksFactory parameter cannot be null.");
        this.banksFactory = banksFactory;
    }

    @Nullable
    @Override
    public BanksFactory getBanksFactory() {
        return banksFactory;
    }

    @Override
    public void registerDatabaseBridgeFactory(DatabaseBridgeFactory databaseBridgeFactory) {
        Preconditions.checkNotNull(databaseBridgeFactory, "databaseBridgeFactory parameter cannot be null.");
        this.databaseBridgeFactory = databaseBridgeFactory;
    }

    @Nullable
    @Override
    public DatabaseBridgeFactory getDatabaseBridgeFactory() {
        return databaseBridgeFactory;
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
        if (!(location instanceof LazyWorldLocation))
            Preconditions.checkNotNull(location.getWorld(), "location's world cannot be null.");
        return new SBlockPosition(location);
    }

    @Override
    public BankTransaction createTransaction(@Nullable UUID player, BankAction action, int position, long time,
                                             String failureReason, BigDecimal amount) {
        Preconditions.checkNotNull(action, "action parameter cannot be null");
        Preconditions.checkNotNull(amount, "amount parameter cannot be null");
        return new SBankTransaction(player, action, position, time, failureReason, amount);
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
        IslandCalculationAlgorithm original = DefaultIslandCalculationAlgorithm.getInstance();
        if (islandsFactory == null)
            return original;

        try {
            // noinspection deprecation
            return islandsFactory.createIslandCalculationAlgorithm(island);
        } catch (UnsupportedOperationException error) {
            return islandsFactory.createIslandCalculationAlgorithm(island, original);
        }
    }

    public IslandBlocksTrackerAlgorithm createIslandBlocksTrackerAlgorithm(Island island) {
        IslandBlocksTrackerAlgorithm original = new DefaultIslandBlocksTrackerAlgorithm(island);
        if (islandsFactory == null)
            return original;

        try {
            // noinspection deprecation
            return islandsFactory.createIslandBlocksTrackerAlgorithm(island);
        } catch (UnsupportedOperationException error) {
            return islandsFactory.createIslandBlocksTrackerAlgorithm(island, original);
        }
    }

    public IslandEntitiesTrackerAlgorithm createIslandEntitiesTrackerAlgorithm(Island island) {
        IslandEntitiesTrackerAlgorithm original = new DefaultIslandEntitiesTrackerAlgorithm(island);
        if (islandsFactory == null)
            return original;

        try {
            // noinspection deprecation
            return islandsFactory.createIslandEntitiesTrackerAlgorithm(island);
        } catch (UnsupportedOperationException error) {
            return islandsFactory.createIslandEntitiesTrackerAlgorithm(island, original);
        }
    }

    public PlayerTeleportAlgorithm createPlayerTeleportAlgorithm(SuperiorPlayer superiorPlayer) {
        PlayerTeleportAlgorithm original = DefaultPlayerTeleportAlgorithm.getInstance();
        if (playersFactory == null)
            return original;

        try {
            // noinspection deprecation
            return playersFactory.createPlayerTeleportAlgorithm(superiorPlayer);
        } catch (UnsupportedOperationException error) {
            return playersFactory.createPlayerTeleportAlgorithm(superiorPlayer, original);
        }
    }

    public boolean hasCustomDatabaseBridge() {
        return databaseBridgeFactory != null;
    }

    public DatabaseBridge createDatabaseBridge(Island island) {
        SQLDatabaseBridge databaseBridge = new SQLDatabaseBridge();
        return databaseBridgeFactory == null ? databaseBridge :
                databaseBridgeFactory.createIslandsDatabaseBridge(island, databaseBridge);
    }

    public DatabaseBridge createDatabaseBridge(SuperiorPlayer superiorPlayer) {
        SQLDatabaseBridge databaseBridge = new SQLDatabaseBridge();
        return databaseBridgeFactory == null ? databaseBridge :
                databaseBridgeFactory.createPlayersDatabaseBridge(superiorPlayer, databaseBridge);
    }

    public DatabaseBridge createDatabaseBridge(GridManager gridManager) {
        SQLDatabaseBridge databaseBridge = new SQLDatabaseBridge();
        return databaseBridgeFactory == null ? databaseBridge :
                databaseBridgeFactory.createGridDatabaseBridge(gridManager, databaseBridge);
    }

    public DatabaseBridge createDatabaseBridge(StackedBlocksManager stackedBlocksManager) {
        SQLDatabaseBridge databaseBridge = new SQLDatabaseBridge();
        return databaseBridgeFactory == null ? databaseBridge :
                databaseBridgeFactory.createStackedBlocksDatabaseBridge(stackedBlocksManager, databaseBridge);
    }

    public PersistentDataContainer createPersistentDataContainer(Island island) {
        PersistentDataContainerImpl<Island> persistentDataContainer = new PersistentDataContainerImpl<>(
                island, IslandsDatabaseBridge::markPersistentDataContainerToBeSaved);
        return islandsFactory == null ? persistentDataContainer :
                islandsFactory.createPersistentDataContainer(island, persistentDataContainer);
    }

    public PersistentDataContainer createPersistentDataContainer(SuperiorPlayer superiorPlayer) {
        PersistentDataContainerImpl<SuperiorPlayer> persistentDataContainer = new PersistentDataContainerImpl<>(
                superiorPlayer, PlayersDatabaseBridge::markPersistentDataContainerToBeSaved);
        return playersFactory == null ? persistentDataContainer :
                playersFactory.createPersistentDataContainer(superiorPlayer, persistentDataContainer);
    }

}
