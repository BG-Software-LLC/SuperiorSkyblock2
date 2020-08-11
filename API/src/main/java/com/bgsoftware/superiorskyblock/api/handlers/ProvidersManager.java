package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.hooks.EconomyProvider;
import com.bgsoftware.superiorskyblock.api.hooks.SpawnersProvider;

public interface ProvidersManager {

    /**
     * Set custom spawners provider for the plugin.
     * Cannot be null.
     * @param spawnersProvider The spawner provider to set.
     */
    void setSpawnersProvider(SpawnersProvider spawnersProvider);

    /**
     * Set custom ecconomy provider for the plugin.
     * Cannot be null.
     * @param economyProvider The economy provider to set.
     */
    void setEconomyProvider(EconomyProvider economyProvider);

}
