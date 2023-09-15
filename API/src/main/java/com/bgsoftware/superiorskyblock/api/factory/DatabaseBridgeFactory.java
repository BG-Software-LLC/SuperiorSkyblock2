package com.bgsoftware.superiorskyblock.api.factory;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.handlers.StackedBlocksManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

public interface DatabaseBridgeFactory {

    /**
     * Create a new database bridge for an island.
     *
     * @param island   The island to create the database-bridge for.
     *                 If island is null, then the database-bridge is used as a loader from the database.
     * @param original The original database-bridge that was created.
     */
    DatabaseBridge createIslandsDatabaseBridge(@Nullable Island island, DatabaseBridge original);

    /**
     * Create a new database bridge for a player.
     *
     * @param superiorPlayer The player to create the database-bridge for.
     *                       If player is null, then the database-bridge is used as a loader from the database.
     * @param original       The original database-bridge that was created.
     */
    DatabaseBridge createPlayersDatabaseBridge(@Nullable SuperiorPlayer superiorPlayer, DatabaseBridge original);

    /**
     * Create a new database bridge for the grid.
     *
     * @param gridManager The grid to create the database-bridge for.
     *                    If grid is null, then the database-bridge is used as a loader from the database.
     * @param original    The original database-bridge that was created.
     */
    DatabaseBridge createGridDatabaseBridge(@Nullable GridManager gridManager, DatabaseBridge original);

    /**
     * Create a new database bridge for the stacked-blocks manager.
     *
     * @param stackedBlocksManager The stacked-blocks manager to create the database-bridge for.
     *                             If manager is null, then the database-bridge is used as a loader from the database.
     * @param original             The original database-bridge that was created.
     */
    DatabaseBridge createStackedBlocksDatabaseBridge(@Nullable StackedBlocksManager stackedBlocksManager, DatabaseBridge original);

}
