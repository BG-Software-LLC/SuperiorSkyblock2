package com.bgsoftware.superiorskyblock.api.factory;

import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

public interface DatabaseBridgeFactory {

    /**
     * Create a new database bridge for an island.
     * @param island The island to create the database-bridge for.
     * @param original The original database-bridge that was created.
     */
    DatabaseBridge createIslandsDatabaseBridge(Island island, DatabaseBridge original);

    /**
     * Create a new database bridge for a player.
     * @param superiorPlayer The player to create the database-bridge for.
     * @param original The original database-bridge that was created.
     */
    DatabaseBridge createPlayersDatabaseBridge(SuperiorPlayer superiorPlayer, DatabaseBridge original);

    /**
     * Create a new database bridge for the grid.
     * @param gridManager The grid to create the database-bridge for.
     * @param original The original database-bridge that was created.
     */
    DatabaseBridge createGridDatabaseBridge(GridManager gridManager, DatabaseBridge original);

}
