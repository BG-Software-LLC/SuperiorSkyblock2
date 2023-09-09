package com.bgsoftware.superiorskyblock.api.island.bank;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.List;

public class DelegateIslandBank implements IslandBank {

    protected final IslandBank handle;

    public DelegateIslandBank(IslandBank handle) {
        this.handle = handle;
    }

    @Override
    public BigDecimal getBalance() {
        return this.handle.getBalance();
    }

    @Override
    public void setBalance(BigDecimal balance) {
        this.handle.setBalance(balance);
    }

    @Override
    public BankTransaction depositMoney(SuperiorPlayer superiorPlayer, BigDecimal amount) {
        return this.handle.depositMoney(superiorPlayer, amount);
    }

    @Override
    public BankTransaction depositAdminMoney(CommandSender commandSender, BigDecimal amount) {
        return this.handle.depositAdminMoney(commandSender, amount);
    }

    @Override
    public boolean canDepositMoney(BigDecimal amount) {
        return this.handle.canDepositMoney(amount);
    }

    @Override
    public BankTransaction withdrawMoney(SuperiorPlayer superiorPlayer, BigDecimal amount, @Nullable List<String> commandsToExecute) {
        return this.handle.withdrawMoney(superiorPlayer, amount, commandsToExecute);
    }

    @Override
    public BankTransaction withdrawAdminMoney(CommandSender commandSender, BigDecimal amount) {
        return this.handle.withdrawAdminMoney(commandSender, amount);
    }

    @Override
    public List<BankTransaction> getAllTransactions() {
        return this.handle.getAllTransactions();
    }

    @Override
    public List<BankTransaction> getTransactions(SuperiorPlayer superiorPlayer) {
        return this.handle.getTransactions(superiorPlayer);
    }

    @Override
    public List<BankTransaction> getConsoleTransactions() {
        return this.handle.getConsoleTransactions();
    }

    @Override
    public void loadTransaction(BankTransaction bankTransaction) {
        this.handle.loadTransaction(bankTransaction);
    }

}
