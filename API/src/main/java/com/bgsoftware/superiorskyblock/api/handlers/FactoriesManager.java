package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.factory.BanksFactory;
import com.bgsoftware.superiorskyblock.api.factory.DatabaseBridgeFactory;
import com.bgsoftware.superiorskyblock.api.factory.IslandsFactory;
import com.bgsoftware.superiorskyblock.api.factory.PlayersFactory;

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

}
