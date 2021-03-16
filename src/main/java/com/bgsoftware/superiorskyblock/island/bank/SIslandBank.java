package com.bgsoftware.superiorskyblock.island.bank;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BankAction;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.island.bank.IslandBank;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.data.SIslandDataHandler;
import com.bgsoftware.superiorskyblock.menu.MenuBankLogs;
import com.bgsoftware.superiorskyblock.menu.MenuIslandBank;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.database.Query;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.SyncedObject;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class SIslandBank implements IslandBank {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final BigDecimal MONEY_FAILURE = BigDecimal.valueOf(-1);
    private static final UUID CONSOLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final SyncedObject<List<BankTransaction>> transactions = SyncedObject.of(new ArrayList<>());
    private final Registry<UUID, SyncedObject<List<BankTransaction>>> transactionsByPlayers = Registry.createRegistry();
    private final SyncedObject<BigDecimal> balance = SyncedObject.of(BigDecimal.ZERO);
    private final Island island;

    public SIslandBank(Island island){
        this.island = island;
    }

    @Override
    public BigDecimal getBalance() {
        return balance.get();
    }

    public void loadBalance(BigDecimal balance){
        setBalance(balance, false);
    }

    @Override
    public BankTransaction depositMoney(SuperiorPlayer superiorPlayer, BigDecimal amount) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(amount, "amount parameter cannot be null.");

        BankTransaction bankTransaction;
        String failureReason;

        if(!island.hasPermission(superiorPlayer, IslandPrivileges.DEPOSIT_MONEY)){
            failureReason = "No permission";
        }
        else if(amount.compareTo(BigDecimal.ZERO) <= 0){
            failureReason = "Invalid amount";
        }
        else {
            SuperiorSkyblockPlugin.debug("Action: Deposit Money, Island: " + island.getOwner().getName() + ", Player: " + superiorPlayer.getName() + ", Money: " + amount);

            EventsCaller.callIslandBankDepositEvent(superiorPlayer, island, amount);

            BigDecimal playerBalance = plugin.getProviders().getBalanceForBanks(superiorPlayer);

            if (playerBalance.compareTo(amount) < 0) {
                failureReason = "Not enough money";
            } else if(island.getBankLimit().compareTo(BigDecimal.valueOf(-1)) > 0 &&
                    this.balance.get().add(amount).compareTo(island.getBankLimit()) > 0) {
                failureReason = "Exceed bank limit";
            } else {
                failureReason = plugin.getProviders().withdrawMoneyForBanks(superiorPlayer, amount);
            }
        }

        int position = transactions.readAndGet(List::size) + 1;

        if(failureReason == null || failureReason.isEmpty()){
            bankTransaction = new SBankTransaction(superiorPlayer.getUniqueId(), BankAction.DEPOSIT_COMPLETED, position, System.currentTimeMillis(), "", amount);
            setBalance(this.balance.get().add(amount), true);

            addTransaction(bankTransaction, true);

            IslandUtils.sendMessage(island, Locale.DEPOSIT_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName(), StringUtils.format(amount));

            MenuIslandBank.refreshMenus(island);
            MenuBankLogs.refreshMenus(island);
        }
        else{
            bankTransaction = new SBankTransaction(superiorPlayer.getUniqueId(), BankAction.DEPOSIT_FAILED, position, System.currentTimeMillis(), failureReason, MONEY_FAILURE);
        }

        return bankTransaction;
    }

    @Override
    public BankTransaction depositAdminMoney(CommandSender commandSender, BigDecimal amount) {
        Preconditions.checkNotNull(commandSender, "commandSender parameter cannot be null.");
        Preconditions.checkNotNull(amount, "amount parameter cannot be null.");
        SuperiorSkyblockPlugin.debug("Action: Deposit Money, Island: " + island.getOwner().getName() + ", Player: " + commandSender.getName() + ", Money: " + amount);

        UUID senderUUID = commandSender instanceof Player ? ((Player) commandSender).getUniqueId() : null;

        int position = transactions.readAndGet(List::size) + 1;

        BankTransaction bankTransaction = new SBankTransaction(senderUUID, BankAction.DEPOSIT_COMPLETED, position, System.currentTimeMillis(), "", amount);
        setBalance(this.balance.get().add(amount), true);

        addTransaction(bankTransaction, true);

        MenuIslandBank.refreshMenus(island);
        MenuBankLogs.refreshMenus(island);

        return bankTransaction;
    }

    public void giveMoneyRaw(BigDecimal amount){
        setBalance(this.balance.get().add(amount), true);
        MenuIslandBank.refreshMenus(island);
    }

    @Override
    public BankTransaction withdrawMoney(SuperiorPlayer superiorPlayer, BigDecimal amount, @Nullable List<String> commandsToExecute) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(amount, "amount parameter cannot be null.");

        BigDecimal withdrawAmount = balance.get().min(amount);

        BankTransaction bankTransaction;
        String failureReason;

        if(!island.hasPermission(superiorPlayer, IslandPrivileges.WITHDRAW_MONEY)){
            failureReason = "No permission";
        }
        else if(this.balance.get().compareTo(BigDecimal.ZERO) <= 0){
            failureReason = "Bank is empty";
        }
        else if(amount.compareTo(BigDecimal.ZERO) <= 0){
            failureReason = "Invalid amount";
        }
        else {
            SuperiorSkyblockPlugin.debug("Action: Withdraw Money, Island: " + island.getOwner().getName() + ", Player: " + superiorPlayer.getName() + ", Money: " + withdrawAmount);

            EventsCaller.callIslandBankWithdrawEvent(superiorPlayer, island, withdrawAmount);

            if (commandsToExecute == null || commandsToExecute.isEmpty()) {
                failureReason = plugin.getProviders().depositMoneyForBanks(superiorPlayer, withdrawAmount);
            } else {
                String currentBalance = balance.get().toString();
                failureReason = "";
                commandsToExecute.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                        .replace("{0}", superiorPlayer.getName())
                        .replace("{1}", currentBalance)
                ));
            }
        }

        int position = transactions.readAndGet(List::size) + 1;

        if(failureReason == null || failureReason.isEmpty()){
            bankTransaction = new SBankTransaction(superiorPlayer.getUniqueId(), BankAction.WITHDRAW_COMPLETED, position, System.currentTimeMillis(), "", withdrawAmount);
            setBalance(this.balance.get().subtract(withdrawAmount), true);

            addTransaction(bankTransaction, true);

            IslandUtils.sendMessage(island, Locale.WITHDRAW_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName(), StringUtils.format(withdrawAmount));

            MenuIslandBank.refreshMenus(island);
            MenuBankLogs.refreshMenus(island);
        }
        else{
            bankTransaction = new SBankTransaction(superiorPlayer.getUniqueId(), BankAction.WITHDRAW_FAILED, position, System.currentTimeMillis(), failureReason, MONEY_FAILURE);
        }

        return bankTransaction;
    }

    @Override
    public BankTransaction withdrawAdminMoney(CommandSender commandSender, BigDecimal amount) {
        Preconditions.checkNotNull(commandSender, "commandSender parameter cannot be null.");
        Preconditions.checkNotNull(amount, "amount parameter cannot be null.");
        SuperiorSkyblockPlugin.debug("Action: Withdraw Money, Island: " + island.getOwner().getName() + ", Player: " + commandSender.getName() + ", Money: " + amount);

        UUID senderUUID = commandSender instanceof Player ? ((Player) commandSender).getUniqueId() : null;

        int position = transactions.readAndGet(List::size) + 1;

        BankTransaction bankTransaction = new SBankTransaction(senderUUID, BankAction.WITHDRAW_COMPLETED, position, System.currentTimeMillis(), "", amount);
        setBalance(this.balance.get().subtract(amount), true);

        addTransaction(bankTransaction, true);

        MenuIslandBank.refreshMenus(island);
        MenuBankLogs.refreshMenus(island);

        return bankTransaction;
    }

    @Override
    public List<BankTransaction> getAllTransactions() {
        return transactions.readAndGet(Collections::unmodifiableList);
    }

    @Override
    public List<BankTransaction> getTransactions(SuperiorPlayer superiorPlayer) {
        return getTransactions(superiorPlayer.getUniqueId());
    }

    @Override
    public List<BankTransaction> getConsoleTransactions() {
        return getTransactions(CONSOLE_UUID);
    }

    public void loadTransaction(BankTransaction bankTransaction){
        addTransaction(bankTransaction, false);
    }

    private List<BankTransaction> getTransactions(UUID uuid){
        SyncedObject<List<BankTransaction>> transactions = this.transactionsByPlayers.get(uuid);
        return transactions == null ? Collections.unmodifiableList(new ArrayList<>()) :
                transactions.readAndGet(Collections::unmodifiableList);
    }

    private void addTransaction(BankTransaction bankTransaction, boolean save){
        if(!plugin.getSettings().bankLogs)
            return;

        UUID senderUUID = bankTransaction.getPlayer();

        transactions.write(transactions -> transactions.add(bankTransaction.getPosition() - 1, bankTransaction));
        transactionsByPlayers.computeIfAbsent(senderUUID != null ? senderUUID : CONSOLE_UUID, p -> SyncedObject.of(new ArrayList<>()))
                .write(transactions -> transactions.add(bankTransaction));

        if(save){
            Query.TRANSACTION_INSERT.getStatementHolder((SIslandDataHandler) island.getDataHandler())
                    .setString(island.getUniqueId().toString())
                    .setString(senderUUID == null ? "" : senderUUID + "")
                    .setString(bankTransaction.getAction().name())
                    .setInt(bankTransaction.getPosition())
                    .setString(bankTransaction.getTime() + "")
                    .setString(bankTransaction.getFailureReason())
                    .setString(bankTransaction.getAmount() + "")
                    .execute(true);
        }
    }

    private void setBalance(BigDecimal balance, boolean save){
        this.balance.set(balance);

        if(save){
            Query.ISLAND_SET_BANK.getStatementHolder((SIslandDataHandler) island.getDataHandler())
                    .setString(balance + "")
                    .setString(island.getOwner().getUniqueId() + "")
                    .execute(true);
        }
    }

}
