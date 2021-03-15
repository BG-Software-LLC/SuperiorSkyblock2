package com.bgsoftware.superiorskyblock.api.upgrades;

import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.math.BigDecimal;

public interface UpgradeCostProvider {
    /**
     * Get the name of the provider used in configs to determine which provider to use
     */
    String getName();

    /**
     * Get the amount of money a specific user has in his bank.
     * @param superiorPlayer The player to check.
     * @param cost from what to get the balance
     */
    BigDecimal getBalance(SuperiorPlayer superiorPlayer, UpgradeCost cost);

    /**
     * take specified amount of the player
     * @param superiorPlayer the player to take from
     * @param cost amount to take
     */
    void take(SuperiorPlayer superiorPlayer, UpgradeCost cost);

    /**
     * Create upgrade cost from level section
     * @param from level section
     * @return Pair of UpgradeCost or if missing any values message with null as key
     */
    Pair<UpgradeCost, String> createCost(ConfigurationSection from);
}
