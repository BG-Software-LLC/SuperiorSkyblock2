package com.bgsoftware.superiorskyblock.api.upgrades;

import com.bgsoftware.common.annotations.Nullable;

import java.util.List;

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
    @Deprecated
    int getSlot();

    /**
     * Get the slots the upgrade is in the upgrades menu.
     */
    List<Integer> getSlots();

    /**
     * Check whether the upgrade is in the given slot in the upgrades menu.
     *
     * @param slot The slot to check.
     */
    boolean isSlot(int slot);

    /**
     * Set the slot the upgrade is in the upgrades menu.
     *
     * @param slot The slot to set the upgrade item in.
     */
    @Deprecated
    void setSlot(int slot);

    /**
     * Set the slots the upgrade is in the upgrades menu.
     *
     * @param slots The slots to set the upgrade item in.
     */
    void setSlots(@Nullable List<Integer> slots);

}
