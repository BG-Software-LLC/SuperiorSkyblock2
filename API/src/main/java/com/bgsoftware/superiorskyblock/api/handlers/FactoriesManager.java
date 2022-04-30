package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.factory.BanksFactory;
import com.bgsoftware.superiorskyblock.api.factory.DatabaseBridgeFactory;
import com.bgsoftware.superiorskyblock.api.factory.IslandsFactory;
import com.bgsoftware.superiorskyblock.api.factory.PlayersFactory;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import org.bukkit.Location;

public interface FactoriesManager {

    /**
     * Register a custom islands factory.
     */
    void registerIslandsFactory(IslandsFactory islandsFactory);

    /**
     * Register a custom islands factory.
     */
    void registerPlayersFactory(PlayersFactory playersFactory);

    /**
     * Register a custom banks factory.
     */
    void registerBanksFactory(BanksFactory banksFactory);

    /**
     * Register a custom database-bridge factory.
     */
    void registerDatabaseBridgeFactory(DatabaseBridgeFactory databaseBridgeFactory);

    /**
     * Create a {@link BlockOffset} object from given offsets.
     *
     * @param offsetX The x-coords offset.
     * @param offsetY The y-coords offset.
     * @param offsetZ The z-coords offset.
     */
    BlockOffset createBlockOffset(int offsetX, int offsetY, int offsetZ);

    /**
     * Create a {@link BlockPosition} object from given block coords.
     *
     * @param world  The name of the world of the block.
     * @param blockX The x-coords of the block.
     * @param blockY The y-coords of the block.
     * @param blockZ The z-coords of the block.
     */
    BlockPosition createBlockPosition(String world, int blockX, int blockY, int blockZ);

    /**
     * Create a {@link BlockPosition} object from a location.
     *
     * @param location The location.
     */
    BlockPosition createBlockPosition(Location location);

}
