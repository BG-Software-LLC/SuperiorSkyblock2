package com.bgsoftware.superiorskyblock.api.handlers;


import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeCostProvider;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;

import java.util.Collection;

public interface UpgradesManager {

    /**
     * Get an upgrade by it's name.
     * @param upgradeName The name of the upgrade.
     */
    Upgrade getUpgrade(String upgradeName);

    /**
     * Get an upgrade by it's menu slot.
     * @param slot The slot of the upgrade.
     */
    Upgrade getUpgrade(int slot);

    /**
     * Get an upgrade object that contains all the default values from config.
     */
    Upgrade getDefaultUpgrade();

    /**
     * Check whether or not an upgrade with the provided name exists or not.
     * @param upgradeName The name to check.
     */
    boolean isUpgrade(String upgradeName);

    /**
     * Get all the upgrades of the plugin.
     */
    Collection<Upgrade> getUpgrades();

    /**
     * Register custom upgrade cost provider
     * @param provider the provider you're registering
     */
    void registerUpgradeCostProvider(UpgradeCostProvider provider);

    /**
     * Get all registered cost providers
     */
    Collection<UpgradeCostProvider> getUpgradesCostProviders();

    /**
     * Check if a upgrade cost provider exists by a name lowercase
     */
    boolean isValidUpgradesCostProvider(String name);

    /**
     * Get upgrade cost provider by name lowercase
     */
    UpgradeCostProvider getUpgradeCostProvider(String name);

}
