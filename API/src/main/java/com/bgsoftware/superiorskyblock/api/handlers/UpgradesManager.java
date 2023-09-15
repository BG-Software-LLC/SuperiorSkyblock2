package com.bgsoftware.superiorskyblock.api.handlers;


import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoader;

import java.util.Collection;

public interface UpgradesManager {

    /**
     * Get an upgrade by it's name.
     *
     * @param upgradeName The name of the upgrade.
     */
    @Nullable
    Upgrade getUpgrade(String upgradeName);

    /**
     * Get an upgrade by it's menu slot.
     *
     * @param slot The slot of the upgrade.
     */
    @Nullable
    Upgrade getUpgrade(int slot);

    /**
     * Add a new upgrade.
     *
     * @param upgrade The upgrade to add.
     */
    void addUpgrade(Upgrade upgrade);

    /**
     * Get an upgrade object that contains all the default values from config.
     */
    Upgrade getDefaultUpgrade();

    /**
     * Check whether or not an upgrade with the provided name exists or not.
     *
     * @param upgradeName The name to check.
     */
    boolean isUpgrade(String upgradeName);

    /**
     * Get all the upgrades of the plugin.
     */
    Collection<Upgrade> getUpgrades();

    /**
     * Register custom upgrade cost loader
     *
     * @param id         The id of the loader.
     * @param costLoader the loader you're registering
     */
    void registerUpgradeCostLoader(String id, UpgradeCostLoader costLoader);

    /**
     * Get all registered cost loader
     */
    Collection<UpgradeCostLoader> getUpgradesCostLoaders();

    /**
     * Get upgrade cost loader by its id
     */
    @Nullable
    UpgradeCostLoader getUpgradeCostLoader(String id);

}
