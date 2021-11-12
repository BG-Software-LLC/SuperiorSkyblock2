package com.bgsoftware.superiorskyblock.api.upgrades.cost;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.math.BigDecimal;

public interface UpgradeCost {

    /**
     * Get the id of this upgrade cost.
     */
    String getId();

    /**
     * Get the cost value.
     */
    BigDecimal getCost();

    /**
     * Check whether or not the player has enough money in his bank.
     *
     * @param superiorPlayer The player to check.
     */
    boolean hasEnoughBalance(SuperiorPlayer superiorPlayer);

    /**
     * Withdraw the cost value from the player.
     *
     * @param superiorPlayer The player to withdraw from.
     */
    void withdrawCost(SuperiorPlayer superiorPlayer);

    /**
     * Clone this cost with a new cost value.
     *
     * @param cost The new cost value
     */
    UpgradeCost clone(BigDecimal cost);

}
