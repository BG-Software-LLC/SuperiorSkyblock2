package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.hooks.AFKProvider;
import com.bgsoftware.superiorskyblock.api.hooks.EconomyProvider;
import com.bgsoftware.superiorskyblock.api.hooks.MenusProvider;
import com.bgsoftware.superiorskyblock.api.hooks.SpawnersProvider;
import com.bgsoftware.superiorskyblock.api.hooks.StackedBlocksProvider;
import com.bgsoftware.superiorskyblock.api.hooks.WorldsProvider;

import java.util.Collection;
import java.util.List;

public interface ProvidersManager {

    /**
     * Set custom spawners provider for the plugin.
     * @param spawnersProvider The spawner provider to set.
     */
    void setSpawnersProvider(SpawnersProvider spawnersProvider);

    /**
     * Get the currently used spawners-provider.
     */
    SpawnersProvider getSpawnersProvider();

    /**
     * Set a custom stacked-blocks provider for the plugin.
     * @param stackedBlocksProvider The stacked-blocks provider to set.
     */
    void setStackedBlocksProvider(StackedBlocksProvider stackedBlocksProvider);

    /**
     * Get the currently used stacked-blocks provider.
     */
    StackedBlocksProvider getStackedBlocksProvider();

    /**
     * Set custom economy provider for the plugin.
     * @param economyProvider The economy provider to set.
     */
    void setEconomyProvider(EconomyProvider economyProvider);

    /**
     * Get the currently used economy-provider.
     */
    EconomyProvider getEconomyProvider();

    /**
     * Set a custom worlds provider for the plugin.
     * @param worldsProvider The worlds provider to set.
     */
    void setWorldsProvider(WorldsProvider worldsProvider);

    /**
     * Get the currently used worlds-provider.
     */
    WorldsProvider getWorldsProvider();

    /**
     * Set custom economy provider for the island banks.
     * @param economyProvider The economy provider to set.
     */
    void setBankEconomyProvider(EconomyProvider economyProvider);

    /**
     * Get the currently used bank-economy provider.
     */
    EconomyProvider getBankEconomyProvider();

    /**
     * Add AFK Provider to the plugin.
     * @param afkProvider The afk-provider to add.
     */
    void addAFKProvider(AFKProvider afkProvider);

    /**
     * Get the currently used afk providers.
     */
    List<AFKProvider> getAFKProviders();

    /**
     * Set a new menus-provider to the plugin.
     * @param menuProvider The new menus-provider to use.
     */
    void setMenusProvider(MenusProvider menuProvider);

    /**
     * Get the currently used menus-provider.
     */
    MenusProvider getMenusProvider();

}
