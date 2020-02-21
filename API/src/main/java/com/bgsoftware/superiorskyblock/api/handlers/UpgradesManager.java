package com.bgsoftware.superiorskyblock.api.handlers;


import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;

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
     * Check whether or not an upgrade with the provided name exists or not.
     * @param upgradeName The name to check.
     */
    boolean isUpgrade(String upgradeName);

    /**
     * Get all the upgrades of the plugin.
     */
    Collection<Upgrade> getUpgrades();

}
