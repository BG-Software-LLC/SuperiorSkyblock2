package com.bgsoftware.superiorskyblock.api.hooks;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public interface EconomyProvider {

    /**
     * Returns if this economy provider is enabled.
     * It should only return false if no economy providers were found (default economy object)
     */
    default boolean isEnabled(){
        return true;
    }

    /**
     * Get the amount of money a specific user has in his bank.
     * @param superiorPlayer The player to check.
     */
    double getMoneyInBank(SuperiorPlayer superiorPlayer);

    /**
     * Deposit money into a player's bank.
     * @param superiorPlayer The player to deposit money to.
     * @param amount The amount to deposit.
     */
    void depositMoney(SuperiorPlayer superiorPlayer, double amount);

    /**
     * Withdraw money from a player's bank.
     * @param superiorPlayer The player to withdraw money from.
     * @param amount The amount to withdraw.
     */
    void withdrawMoney(SuperiorPlayer superiorPlayer, double amount);
    
}
