package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.hooks.AFKProvider;
import com.bgsoftware.superiorskyblock.api.hooks.ChunksProvider;
import com.bgsoftware.superiorskyblock.api.hooks.EconomyProvider;
import com.bgsoftware.superiorskyblock.api.hooks.EntitiesProvider;
import com.bgsoftware.superiorskyblock.api.hooks.MenusProvider;
import com.bgsoftware.superiorskyblock.api.hooks.PermissionsProvider;
import com.bgsoftware.superiorskyblock.api.hooks.PricesProvider;
import com.bgsoftware.superiorskyblock.api.hooks.SpawnersProvider;
import com.bgsoftware.superiorskyblock.api.hooks.StackedBlocksProvider;
import com.bgsoftware.superiorskyblock.api.hooks.VanishProvider;
import com.bgsoftware.superiorskyblock.api.hooks.WorldsProvider;
import com.bgsoftware.superiorskyblock.api.hooks.listener.ISkinsListener;
import com.bgsoftware.superiorskyblock.api.hooks.listener.IStackedBlocksListener;
import com.bgsoftware.superiorskyblock.api.hooks.listener.IWorldsListener;

import java.util.List;

public interface ProvidersManager {

    /**
     * Get the currently used spawners-provider.
     */
    SpawnersProvider getSpawnersProvider();

    /**
     * Set custom spawners provider for the plugin.
     *
     * @param spawnersProvider The spawner provider to set.
     */
    void setSpawnersProvider(SpawnersProvider spawnersProvider);

    /**
     * Get the currently used stacked-blocks provider.
     */
    StackedBlocksProvider getStackedBlocksProvider();

    /**
     * Set a custom stacked-blocks provider for the plugin.
     *
     * @param stackedBlocksProvider The stacked-blocks provider to set.
     */
    void setStackedBlocksProvider(StackedBlocksProvider stackedBlocksProvider);

    /**
     * Get the currently used stacked-entities provider.
     */
    List<EntitiesProvider> getEntitiesProviders();

    /**
     * Add a custom entities provider for the plugin.
     *
     * @param entitiesProvider The entities provider to add.
     */
    void addEntitiesProvider(EntitiesProvider entitiesProvider);

    /**
     * Get the currently used economy-provider.
     */
    EconomyProvider getEconomyProvider();

    /**
     * Set custom economy provider for the plugin.
     *
     * @param economyProvider The economy provider to set.
     */
    void setEconomyProvider(EconomyProvider economyProvider);

    /**
     * Get the currently used worlds-provider.
     */
    WorldsProvider getWorldsProvider();

    /**
     * Set a custom worlds provider for the plugin.
     *
     * @param worldsProvider The worlds provider to set.
     */
    void setWorldsProvider(WorldsProvider worldsProvider);

    /**
     * Get the currently used chunks-provider.
     */
    ChunksProvider getChunksProvider();

    /**
     * Set a custom chunks provider for the plugin.
     *
     * @param chunksProvider The chunks provider to set.
     */
    void setChunksProvider(ChunksProvider chunksProvider);

    /**
     * Get the currently used bank-economy provider.
     */
    EconomyProvider getBankEconomyProvider();

    /**
     * Set custom economy provider for the island banks.
     *
     * @param economyProvider The economy provider to set.
     */
    void setBankEconomyProvider(EconomyProvider economyProvider);

    /**
     * Get the currently used afk providers.
     */
    List<AFKProvider> getAFKProviders();

    /**
     * Add AFK Provider to the plugin.
     *
     * @param afkProvider The afk-provider to add.
     */
    void addAFKProvider(AFKProvider afkProvider);

    /**
     * Get the currently used menus-provider.
     */
    MenusProvider getMenusProvider();

    /**
     * Set a new menus-provider to the plugin.
     *
     * @param menuProvider The new menus-provider to use.
     */
    void setMenusProvider(MenusProvider menuProvider);

    /**
     * Get the currently used permissions-provider.
     */
    PermissionsProvider getPermissionsProvider();

    /**
     * Set a new permissions-provider to the plugin.
     *
     * @param permissionsProvider The new permissions-provider to use.
     */
    void setPermissionsProvider(PermissionsProvider permissionsProvider);

    /**
     * Get the currently used prices-provider.
     */
    PricesProvider getPricesProvider();

    /**
     * Set a new prices-provider to the plugin.
     *
     * @param pricesProvider The new prices-provider to use.
     */
    void setPricesProvider(PricesProvider pricesProvider);

    /**
     * Get the currently used vanish-provider.
     */
    VanishProvider getVanishProvider();

    /**
     * Set a new vanish-provider to the plugin.
     *
     * @param vanishProvider The new vanish-provider to use.
     */
    void setVanishProvider(VanishProvider vanishProvider);

    /**
     * Register a new skins listener.
     *
     * @param skinsListener The new skins listener to register.
     */
    void registerSkinsListener(ISkinsListener skinsListener);

    /**
     * Unregister a skins listener.
     *
     * @param skinsListener The new skins listener to unregister.
     */
    void unregisterSkinsListener(ISkinsListener skinsListener);

    /**
     * Register a new stacked-blocks listener.
     *
     * @param stackedBlocksListener The new stacked-blocks listener to register.
     */
    void registerStackedBlocksListener(IStackedBlocksListener stackedBlocksListener);

    /**
     * Unregister a stacked-blocks listener.
     *
     * @param stackedBlocksListener The stacked-blocks listener to unregister.
     */
    void unregisterStackedBlocksListener(IStackedBlocksListener stackedBlocksListener);

    /**
     * Register a new worlds listener.
     *
     * @param worldsListener The new worlds listener to register.
     */
    void registerWorldsListener(IWorldsListener worldsListener);

    /**
     * Unregister a worlds listener.
     *
     * @param worldsListener The worlds listener to unregister.
     */
    void unregisterWorldsListener(IWorldsListener worldsListener);

}
