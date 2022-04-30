package com.bgsoftware.superiorskyblock.api.upgrades;

public interface Upgrade {

    /**
     * Get the name of the upgrade.
     */
    String getName();

    /**
     * Get the upgrade-level object from a level.
     * If it doesn't exist, an update level with level 0 will be returned.
     *
     * @param level The level to get the object from.
     */
    UpgradeLevel getUpgradeLevel(int level);

    /**
     * Get the maximum level that exists for the upgrade.
     */
    int getMaxUpgradeLevel();

    /**
     * Get the slot the upgrade is in the upgrades menu.
     */
    int getSlot();

    /**
     * Set the slot the upgrade is in the upgrades menu.
     *
     * @param slot The slot to set the upgrade item in.
     */
    void setSlot(int slot);

}
