package com.bgsoftware.superiorskyblock.api.upgrades;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.math.BigDecimal;

public interface UpgradeCostProvider {
    /**
     * Get the name of the provider used in configs to determine which provider to use
     */
    String getName();

    /**
     * Get the amount of money a specific user has in his bank.
     * @param superiorPlayer The player to check.
     */
    BigDecimal getBalance(SuperiorPlayer superiorPlayer);

    /**
     * take specified amount of the player
     * @param superiorPlayer the player to take from
     * @param amount amount to take
     */
    void take(SuperiorPlayer superiorPlayer, BigDecimal amount);
}
