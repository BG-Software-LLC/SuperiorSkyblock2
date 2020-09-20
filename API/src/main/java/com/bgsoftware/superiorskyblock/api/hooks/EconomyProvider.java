package com.bgsoftware.superiorskyblock.api.hooks;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.math.BigDecimal;

public interface EconomyProvider {

    /**
     * Returns if this economy provider is enabled.
     * It should only return false if no economy providers were found (default economy object)
     *
     * @deprecated not used anymore.
     */
    @Deprecated
    default boolean isEnabled(){
        return true;
    }

    /**
     * Get the amount of money a specific user has in his bank.
     * @param superiorPlayer The player to check.
     *
     * @deprecated See getBalance
     */
    @Deprecated
    double getMoneyInBank(SuperiorPlayer superiorPlayer);

    /**
     * Get the amount of money a specific user has in his bank.
     * @param superiorPlayer The player to check.
     */
    BigDecimal getBalance(SuperiorPlayer superiorPlayer);

    /**
     * Deposit money into a player's bank.
     * @param superiorPlayer The player to deposit money to.
     * @param amount The amount to deposit.
     * @return The error message if needed. Otherwise, empty string.
     */
    String depositMoney(SuperiorPlayer superiorPlayer, double amount);

    /**
     * Withdraw money from a player's bank.
     * @param superiorPlayer The player to withdraw money from.
     * @param amount The amount to withdraw.
     * @return The error message if needed. Otherwise, empty string.
     */
    String withdrawMoney(SuperiorPlayer superiorPlayer, double amount);
    
}
