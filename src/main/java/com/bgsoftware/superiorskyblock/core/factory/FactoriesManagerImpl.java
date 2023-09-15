package com.bgsoftware.superiorskyblock.core.factory;

import com.bgsoftware.common.annotations.Nullable;
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
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import com.bgsoftware.superiorskyblock.core.SBlockOffset;
import com.bgsoftware.superiorskyblock.core.SBlockPosition;
import com.bgsoftware.superiorskyblock.core.WorldInfoImpl;
import com.bgsoftware.superiorskyblock.core.database.bridge.IslandsDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.database.bridge.PlayersDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.database.sql.SQLDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.persistence.PersistentDataContainerImpl;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.island.algorithm.DefaultIslandBlocksTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.island.algorithm.DefaultIslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.island.algorithm.DefaultIslandEntitiesTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.island.bank.SBankTransaction;
import com.bgsoftware.superiorskyblock.island.bank.SIslandBank;
import com.bgsoftware.superiorskyblock.island.builder.IslandBuilderImpl;
import com.bgsoftware.superiorskyblock.player.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.player.algorithm.DefaultPlayerTeleportAlgorithm;
import com.bgsoftware.superiorskyblock.player.builder.SuperiorPlayerBuilderImpl;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Supplier;

public class FactoriesManagerImpl implements FactoriesManager {

    private IslandsFactory islandsFactory = DefaultIslandsFactory.getInstance();
    private PlayersFactory playersFactory = DefaultPlayersFactory.getInstance();
    private BanksFactory banksFactory = DefaultBanksFactory.getInstance();
    private DatabaseBridgeFactory databaseBridgeFactory = DefaultDatabaseBridgeFactory.getInstance();

    @Override
    public void registerIslandsFactory(@Nullable IslandsFactory islandsFactory) {
        this.islandsFactory = islandsFactory == null ? DefaultIslandsFactory.getInstance() : islandsFactory;
    }

    @Override
    public IslandsFactory getIslandsFactory() {
        return islandsFactory;
    }

    @Override
    public void registerPlayersFactory(@Nullable PlayersFactory playersFactory) {
        this.playersFactory = playersFactory == null ? DefaultPlayersFactory.getInstance() : playersFactory;
    }

    @Override
    public PlayersFactory getPlayersFactory() {
        return playersFactory;
    }

    @Override
    public void registerBanksFactory(@Nullable BanksFactory banksFactory) {
        this.banksFactory = banksFactory == null ? DefaultBanksFactory.getInstance() : banksFactory;
    }

    @Override
    public BanksFactory getBanksFactory() {
        return banksFactory;
    }

    @Override
    public void registerDatabaseBridgeFactory(@Nullable DatabaseBridgeFactory databaseBridgeFactory) {
        this.databaseBridgeFactory = databaseBridgeFactory == null ? DefaultDatabaseBridgeFactory.getInstance() : databaseBridgeFactory;
    }

    @Override
    public DatabaseBridgeFactory getDatabaseBridgeFactory() {
        return databaseBridgeFactory;
    }

    @Override
    public Island createIsland(@Nullable SuperiorPlayer owner, UUID uuid, Location center, String islandName, String schemName) {
        Preconditions.checkNotNull(uuid, "uuid parameter cannot be null.");
        Preconditions.checkNotNull(center, "center parameter cannot be null.");
        Preconditions.checkNotNull(islandName, "islandName parameter cannot be null.");
        Preconditions.checkNotNull(schemName, "schemName parameter cannot be null.");
        return createIslandBuilder()
                .setOwner(owner)
                .setUniqueId(uuid)
                .setCenter(center)
                .setName(islandName)
                .setSchematicName(schemName)
                .build();
    }

    @Override
    public Island.Builder createIslandBuilder() {
        return new IslandBuilderImpl();
    }

    public Island createIsland(IslandBuilderImpl builder) {
        return islandsFactory.createIsland(new SIsland(builder));
    }

    @Override
    public SuperiorPlayer createPlayer(UUID playerUUID) {
        Preconditions.checkNotNull(playerUUID, "playerUUID parameter cannot be null.");
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
        SuperiorPlayer.Builder builder = createPlayerBuilder()
                .setUniqueId(playerUUID);

        if (offlinePlayer != null && offlinePlayer.getName() != null)
            builder.setName(offlinePlayer.getName());

        return builder.build();
    }

    @Override
    public SuperiorPlayer.Builder createPlayerBuilder() {
        return new SuperiorPlayerBuilderImpl();
    }

    public SuperiorPlayer createPlayer(SuperiorPlayerBuilderImpl builder) {
        return playersFactory.createPlayer(new SSuperiorPlayer(builder));
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

    @Override
    public WorldInfo createWorldInfo(String worldName, World.Environment environment) {
        Preconditions.checkNotNull(worldName, "worldName parameter cannot be null");
        Preconditions.checkNotNull(environment, "environment parameter cannot be null");
        return new WorldInfoImpl(worldName, environment);
    }

    @Override
    public GameSound createGameSound(Sound sound, float volume, float pitch) {
        Preconditions.checkNotNull(sound, "sound parameter cannot be null");
        return new GameSoundImpl(sound, volume, pitch);
    }

    public IslandBank createIslandBank(Island island, Supplier<Boolean> isGiveInterestFailed) {
        return banksFactory.createIslandBank(island, new SIslandBank(island, isGiveInterestFailed));
    }

    public IslandCalculationAlgorithm createIslandCalculationAlgorithm(Island island) {
        try {
            // noinspection deprecation
            return islandsFactory.createIslandCalculationAlgorithm(island);
        } catch (UnsupportedOperationException error) {
            return islandsFactory.createIslandCalculationAlgorithm(island, DefaultIslandCalculationAlgorithm.getInstance());
        }
    }

    public IslandBlocksTrackerAlgorithm createIslandBlocksTrackerAlgorithm(Island island) {
        try {
            // noinspection deprecation
            return islandsFactory.createIslandBlocksTrackerAlgorithm(island);
        } catch (UnsupportedOperationException error) {
            return islandsFactory.createIslandBlocksTrackerAlgorithm(island, new DefaultIslandBlocksTrackerAlgorithm(island));
        }
    }

    public IslandEntitiesTrackerAlgorithm createIslandEntitiesTrackerAlgorithm(Island island) {
        try {
            // noinspection deprecation
            return islandsFactory.createIslandEntitiesTrackerAlgorithm(island);
        } catch (UnsupportedOperationException error) {
            return islandsFactory.createIslandEntitiesTrackerAlgorithm(island, new DefaultIslandEntitiesTrackerAlgorithm(island));
        }
    }

    public PlayerTeleportAlgorithm createPlayerTeleportAlgorithm(SuperiorPlayer superiorPlayer) {
        try {
            // noinspection deprecation
            return playersFactory.createPlayerTeleportAlgorithm(superiorPlayer);
        } catch (UnsupportedOperationException error) {
            return playersFactory.createPlayerTeleportAlgorithm(superiorPlayer, DefaultPlayerTeleportAlgorithm.getInstance());
        }
    }

    public boolean hasCustomDatabaseBridge() {
        return databaseBridgeFactory != DefaultDatabaseBridgeFactory.getInstance();
    }

    public DatabaseBridge createDatabaseBridge(Island island) {
        return databaseBridgeFactory.createIslandsDatabaseBridge(island, new SQLDatabaseBridge());
    }

    public DatabaseBridge createDatabaseBridge(SuperiorPlayer superiorPlayer) {
        return databaseBridgeFactory.createPlayersDatabaseBridge(superiorPlayer, new SQLDatabaseBridge());
    }

    public DatabaseBridge createDatabaseBridge(GridManager gridManager) {
        return databaseBridgeFactory.createGridDatabaseBridge(gridManager, new SQLDatabaseBridge());
    }

    public DatabaseBridge createDatabaseBridge(StackedBlocksManager stackedBlocksManager) {
        return databaseBridgeFactory.createStackedBlocksDatabaseBridge(stackedBlocksManager, new SQLDatabaseBridge());
    }

    public PersistentDataContainer createPersistentDataContainer(Island island) {
        return islandsFactory.createPersistentDataContainer(island, new PersistentDataContainerImpl<>(
                island, IslandsDatabaseBridge::markPersistentDataContainerToBeSaved));
    }

    public PersistentDataContainer createPersistentDataContainer(SuperiorPlayer superiorPlayer) {
        return playersFactory.createPersistentDataContainer(superiorPlayer, new PersistentDataContainerImpl<>(
                superiorPlayer, PlayersDatabaseBridge::markPersistentDataContainerToBeSaved));
    }

}
