package com.bgsoftware.superiorskyblock.api.upgrades;

public interface Upgrade {

    /**
     * Get the name of the upgrade.
     */
    String getName();

    /**
     * Get the upgrade-level object from a level.
     * If doesn't exist, an update level with level 0 will be returned.
     * @param level The level to get the object from.
     */
    UpgradeLevel getUpgradeLevel(int level);

    /**
     * Get the maximum level that exists for the upgrade.
     */
    int getMaxUpgradeLevel();

}
