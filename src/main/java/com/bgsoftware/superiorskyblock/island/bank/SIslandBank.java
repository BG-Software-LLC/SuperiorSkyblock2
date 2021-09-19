package com.bgsoftware.superiorskyblock.island.bank;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BankAction;
import com.bgsoftware.superiorskyblock.api.hooks.EconomyProvider;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.island.bank.IslandBank;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.database.bridge.IslandsDatabaseBridge;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.islands.SortingComparators;
import com.bgsoftware.superiorskyblock.utils.threads.SyncedObject;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public final class SIslandBank implements IslandBank {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final BigDecimal MONEY_FAILURE = BigDecimal.valueOf(-1);
    private static final UUID CONSOLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final SyncedObject<SortedSet<BankTransaction>> transactions = SyncedObject.of(new TreeSet<>(SortingComparators.BANK_TRANSACTIONS_COMPARATOR));
    private final Map<UUID, SyncedObject<List<BankTransaction>>> transactionsByPlayers = new ConcurrentHashMap<>();
    private final AtomicReference<BigDecimal> balance = new AtomicReference<>(BigDecimal.ZERO);
    private final Island island;

    public SIslandBank(Island island){
        this.island = island;
    }

    @Override
    public BigDecimal getBalance() {
        return balance.get();
    }

    @Override
    public void setBalance(BigDecimal balance) {
        this.balance.set(balance.setScale(2, RoundingMode.HALF_DOWN));
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
                EconomyProvider.EconomyResult result = plugin.getProviders().withdrawMoneyForBanks(superiorPlayer, amount);
                failureReason = result.getErrorMessage();
                amount = BigDecimal.valueOf(result.getTransactionMoney());
            }
        }

        int position = transactions.readAndGet(SortedSet::size) + 1;

        if(failureReason == null || failureReason.isEmpty()){
            bankTransaction = new SBankTransaction(superiorPlayer.getUniqueId(), BankAction.DEPOSIT_COMPLETED, position, System.currentTimeMillis(), "", amount);
            increaseBalance(amount);

            addTransaction(bankTransaction, true);

            IslandUtils.sendMessage(island, Locale.DEPOSIT_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName(), StringUtils.format(amount));

            plugin.getMenus().refreshBankLogs(island);
            plugin.getMenus().refreshBankLogs(island);
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

        int position = transactions.readAndGet(SortedSet::size) + 1;

        BankTransaction bankTransaction = new SBankTransaction(senderUUID, BankAction.DEPOSIT_COMPLETED, position, System.currentTimeMillis(), "", amount);
        increaseBalance(amount);

        addTransaction(bankTransaction, true);

        plugin.getMenus().refreshBankLogs(island);
        plugin.getMenus().refreshBankLogs(island);

        return bankTransaction;
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
                EconomyProvider.EconomyResult result = plugin.getProviders().depositMoneyForBanks(superiorPlayer, withdrawAmount);
                failureReason = result.getErrorMessage();
                withdrawAmount = BigDecimal.valueOf(result.getTransactionMoney());
            } else {
                String currentBalance = balance.get().toString();
                failureReason = "";
                commandsToExecute.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                        .replace("{0}", superiorPlayer.getName())
                        .replace("{1}", currentBalance)
                ));
            }
        }

        int position = transactions.readAndGet(SortedSet::size) + 1;

        if(failureReason == null || failureReason.isEmpty()){
            bankTransaction = new SBankTransaction(superiorPlayer.getUniqueId(), BankAction.WITHDRAW_COMPLETED, position, System.currentTimeMillis(), "", withdrawAmount);
            decreaseBalance(withdrawAmount);

            addTransaction(bankTransaction, true);

            IslandUtils.sendMessage(island, Locale.WITHDRAW_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName(), StringUtils.format(withdrawAmount));

            plugin.getMenus().refreshBankLogs(island);
            plugin.getMenus().refreshBankLogs(island);
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

        int position = transactions.readAndGet(SortedSet::size) + 1;

        BankTransaction bankTransaction = new SBankTransaction(senderUUID, BankAction.WITHDRAW_COMPLETED, position, System.currentTimeMillis(), "", amount);
        decreaseBalance(amount);

        addTransaction(bankTransaction, true);

        plugin.getMenus().refreshBankLogs(island);
        plugin.getMenus().refreshBankLogs(island);

        return bankTransaction;
    }

    @Override
    public List<BankTransaction> getAllTransactions() {
        return transactions.readAndGet(bankTransactions -> Collections.unmodifiableList(new ArrayList<>(bankTransactions)));
    }

    @Override
    public List<BankTransaction> getTransactions(SuperiorPlayer superiorPlayer) {
        return getTransactions(superiorPlayer.getUniqueId());
    }

    @Override
    public List<BankTransaction> getConsoleTransactions() {
        return getTransactions(CONSOLE_UUID);
    }

    @Override
    public void loadTransaction(BankTransaction bankTransaction){
        addTransaction(bankTransaction, false);
    }

    private List<BankTransaction> getTransactions(UUID uuid){
        SyncedObject<List<BankTransaction>> transactions = this.transactionsByPlayers.get(uuid);
        return transactions == null ? Collections.unmodifiableList(new ArrayList<>()) :
                transactions.readAndGet(Collections::unmodifiableList);
    }

    private void addTransaction(BankTransaction bankTransaction, boolean save){
        if(!BuiltinModules.BANK.bankLogs)
            return;

        UUID senderUUID = bankTransaction.getPlayer();

        transactions.write(transactions -> transactions.add(bankTransaction));
        transactionsByPlayers.computeIfAbsent(senderUUID != null ? senderUUID : CONSOLE_UUID, p -> SyncedObject.of(new ArrayList<>()))
                .write(transactions -> transactions.add(bankTransaction));

        if(save){
            IslandsDatabaseBridge.saveBankTransaction(island, bankTransaction);
        }
    }

    private void decreaseBalance(BigDecimal amount){
        increaseBalance(amount.negate());
    }

    private void increaseBalance(BigDecimal amount){
        this.balance.updateAndGet(bigDecimal -> bigDecimal.add(amount).setScale(2, RoundingMode.HALF_DOWN));
        IslandsDatabaseBridge.saveBankBalance(island);
    }

}
