package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.hooks.AFKProvider;
import com.bgsoftware.superiorskyblock.api.hooks.EconomyProvider;
import com.bgsoftware.superiorskyblock.api.hooks.SpawnersProvider;
import com.bgsoftware.superiorskyblock.api.hooks.WorldsProvider;

public interface ProvidersManager {

    /**
     * Set custom spawners provider for the plugin.
     * @param spawnersProvider The spawner provider to set.
     */
    void setSpawnersProvider(SpawnersProvider spawnersProvider);

    /**
     * Set custom ecconomy provider for the plugin.
     * @param economyProvider The economy provider to set.
     */
    void setEconomyProvider(EconomyProvider economyProvider);

    /**
     * Set a custom worlds provider for the plugin.
     * @param worldsProvider The worlds provider to set.
     */
    void setWorldsProvider(WorldsProvider worldsProvider);

    /**
     * Set custom economy provider for the island banks.
     * @param economyProvider The economy provider to set.
     */
    void setBankEconomyProvider(EconomyProvider economyProvider);

    /**
     * Add AFK Provider to the plugin.
     * @param afkProvider The afk-provider to add.
     */
    void addAFKProvider(AFKProvider afkProvider);

}
