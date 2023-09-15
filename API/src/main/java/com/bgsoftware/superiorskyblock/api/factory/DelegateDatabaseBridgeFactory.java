package com.bgsoftware.superiorskyblock.api.factory;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.handlers.StackedBlocksManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

public class DelegateDatabaseBridgeFactory implements DatabaseBridgeFactory {

    protected final DatabaseBridgeFactory handle;

    protected DelegateDatabaseBridgeFactory(DatabaseBridgeFactory handle) {
        this.handle = handle;
    }

    @Override
    public DatabaseBridge createIslandsDatabaseBridge(@Nullable Island island, DatabaseBridge original) {
        return this.handle.createIslandsDatabaseBridge(island, original);
    }

    @Override
    public DatabaseBridge createPlayersDatabaseBridge(@Nullable SuperiorPlayer superiorPlayer, DatabaseBridge original) {
        return this.handle.createPlayersDatabaseBridge(superiorPlayer, original);
    }

    @Override
    public DatabaseBridge createGridDatabaseBridge(@Nullable GridManager gridManager, DatabaseBridge original) {
        return this.handle.createGridDatabaseBridge(gridManager, original);
    }

    @Override
    public DatabaseBridge createStackedBlocksDatabaseBridge(@Nullable StackedBlocksManager stackedBlocksManager, DatabaseBridge original) {
        return this.handle.createStackedBlocksDatabaseBridge(stackedBlocksManager, original);
    }

}
