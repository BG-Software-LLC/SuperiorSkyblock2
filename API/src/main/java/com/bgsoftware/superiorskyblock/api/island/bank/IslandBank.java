package com.bgsoftware.superiorskyblock.api.island.bank;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.List;

public interface IslandBank {

    /**
     * Get balance in bank.
     */
    BigDecimal getBalance();

    /**
     * Deposit money into the bank.
     * @param superiorPlayer The player that deposited the money.
     * @param amount The amount to deposit.
     * @return The transaction details.
     */
    BankTransaction depositMoney(SuperiorPlayer superiorPlayer, BigDecimal amount);

    /**
     * Deposit money into the bank, without taking money from any player.
     * @param commandSender The player that deposited the money.
     * @param amount The amount to deposit.
     * @return The transaction details.
     */
    BankTransaction depositAdminMoney(CommandSender commandSender, BigDecimal amount);

    /**
     * Withdraw money from the bank.
     * @param superiorPlayer The player that withdrawn the money.
     * @param amount The amount to withdraw.
     * @param commandsToExecute Commands to execute instead of using the default economy provider.
     *                          The commands can use {0} as player's name placeholder, and {1} for the amount.
     * @return The transaction details.
     */
    BankTransaction withdrawMoney(SuperiorPlayer superiorPlayer, BigDecimal amount, List<String> commandsToExecute);

    /**
     * Withdraw money from the bank, without giving it to any player.
     * @param commandSender The player that withdrawn the money.
     * @param amount The amount to withdraw.
     * @return The transaction details.
     */
    BankTransaction withdrawAdminMoney(CommandSender commandSender, BigDecimal amount);

    /**
     * Get all the transactions of the bank, sorted by the time they were created.
     */
    List<BankTransaction> getAllTransactions();

    /**
     * Get all the transactions made by a player.
     */
    List<BankTransaction> getTransactions(SuperiorPlayer superiorPlayer);

    /**
     * Get all the transactions made by CONSOLE.
     */
    List<BankTransaction> getConsoleTransactions();

}
