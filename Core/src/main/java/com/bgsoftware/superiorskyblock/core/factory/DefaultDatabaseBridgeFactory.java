package com.bgsoftware.superiorskyblock.core.factory;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.factory.DatabaseBridgeFactory;
import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.handlers.StackedBlocksManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

public class DefaultDatabaseBridgeFactory implements DatabaseBridgeFactory {

    private static final DefaultDatabaseBridgeFactory INSTANCE = new DefaultDatabaseBridgeFactory();

    public static DefaultDatabaseBridgeFactory getInstance() {
        return INSTANCE;
    }

    private DefaultDatabaseBridgeFactory() {
    }

    @Override
    public DatabaseBridge createIslandsDatabaseBridge(@Nullable Island island, DatabaseBridge original) {
        return original;
    }

    @Override
    public DatabaseBridge createPlayersDatabaseBridge(@Nullable SuperiorPlayer superiorPlayer, DatabaseBridge original) {
        return original;
    }

    @Override
    public DatabaseBridge createGridDatabaseBridge(@Nullable GridManager gridManager, DatabaseBridge original) {
        return original;
    }

    @Override
    public DatabaseBridge createStackedBlocksDatabaseBridge(@Nullable StackedBlocksManager stackedBlocksManager, DatabaseBridge original) {
        return original;
    }

}
