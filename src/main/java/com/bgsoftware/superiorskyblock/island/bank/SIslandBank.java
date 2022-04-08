package com.bgsoftware.superiorskyblock.island.bank;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BankAction;
import com.bgsoftware.superiorskyblock.api.hooks.EconomyProvider;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.island.bank.IslandBank;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.database.bridge.IslandsDatabaseBridge;
import com.bgsoftware.superiorskyblock.island.bank.logs.CacheBankLogs;
import com.bgsoftware.superiorskyblock.island.bank.logs.DatabaseBankLogs;
import com.bgsoftware.superiorskyblock.island.bank.logs.IBankLogs;
import com.bgsoftware.superiorskyblock.island.permissions.IslandPrivileges;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.utils.events.EventResult;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class SIslandBank implements IslandBank {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final BigDecimal MONEY_FAILURE = BigDecimal.valueOf(-1);
    private static final BigDecimal NO_BANK_LIMIT = BigDecimal.valueOf(-1);
    private static final UUID CONSOLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final AtomicReference<BigDecimal> balance = new AtomicReference<>(BigDecimal.ZERO);
    private final Island island;
    private final Supplier<Boolean> isGiveInterestFailed;
    private final IBankLogs bankLogs;

    public SIslandBank(Island island, Supplier<Boolean> isGiveInterestFailed) {
        this.island = island;
        this.isGiveInterestFailed = isGiveInterestFailed;
        this.bankLogs = BuiltinModules.BANK.cacheAllLogs ? new CacheBankLogs() : new DatabaseBankLogs(island);
    }

    @Override
    public BigDecimal getBalance() {
        return balance.get();
    }

    @Override
    public void setBalance(BigDecimal balance) {
        this.balance.set(balance.setScale(2, RoundingMode.HALF_DOWN));

        // Trying to give interest again if the last one failed.
        if (isGiveInterestFailed.get())
            island.giveInterest(false);
    }

    @Override
    public BankTransaction depositMoney(SuperiorPlayer superiorPlayer, BigDecimal amount) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(amount, "amount parameter cannot be null.");

        BankTransaction bankTransaction;
        String failureReason;

        if (!island.hasPermission(superiorPlayer, IslandPrivileges.DEPOSIT_MONEY)) {
            failureReason = "No permission";
        } else if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            failureReason = "Invalid amount";
        } else {
            PluginDebugger.debug("Action: Deposit Money, Island: " + island.getOwner().getName() + ", Player: " +
                    superiorPlayer.getName() + ", Money: " + amount);

            BigDecimal playerBalance = plugin.getProviders().getBankEconomyProvider().getBalance(superiorPlayer);

            EventResult<String> eventResult = EventsCaller.callIslandBankDepositEvent(superiorPlayer, island, amount);

            if (eventResult.isCancelled()) {
                failureReason = eventResult.getResult();
            } else if (playerBalance.compareTo(amount) < 0) {
                failureReason = "Not enough money";
            } else if (!canDepositMoney(amount)) {
                failureReason = "Exceed bank limit";
            } else {
                EconomyProvider.EconomyResult result = plugin.getProviders()
                        .withdrawMoneyForBanks(superiorPlayer, amount);
                failureReason = result.getErrorMessage();
                amount = BigDecimal.valueOf(result.getTransactionMoney());
            }
        }

        int position = this.bankLogs.getLastTransactionPosition() + 1;

        if (failureReason == null || failureReason.isEmpty()) {
            bankTransaction = new SBankTransaction(superiorPlayer.getUniqueId(), BankAction.DEPOSIT_COMPLETED,
                    position, System.currentTimeMillis(), "", amount);
            increaseBalance(amount);

            addTransaction(bankTransaction, true);

            IslandUtils.sendMessage(island, Message.DEPOSIT_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName(),
                    StringUtils.format(amount));

            plugin.getMenus().refreshBankLogs(island);
            plugin.getMenus().refreshBankLogs(island);
        } else {
            bankTransaction = new SBankTransaction(superiorPlayer.getUniqueId(), BankAction.DEPOSIT_FAILED, position,
                    System.currentTimeMillis(), failureReason, MONEY_FAILURE);
        }

        return bankTransaction;
    }

    @Override
    public BankTransaction depositAdminMoney(CommandSender commandSender, BigDecimal amount) {
        Preconditions.checkNotNull(commandSender, "commandSender parameter cannot be null.");
        Preconditions.checkNotNull(amount, "amount parameter cannot be null.");
        PluginDebugger.debug("Action: Deposit Money, Island: " + island.getOwner().getName() + ", Player: " +
                commandSender.getName() + ", Money: " + amount);

        EventResult<String> eventResult = EventsCaller.callIslandBankDepositEvent(commandSender instanceof Player ?
                plugin.getPlayers().getSuperiorPlayer(commandSender) : null, island, amount);

        UUID senderUUID = commandSender instanceof Player ? ((Player) commandSender).getUniqueId() : null;

        int position = this.bankLogs.getLastTransactionPosition() + 1;

        BankAction bankAction = eventResult.isCancelled() ? BankAction.DEPOSIT_FAILED : BankAction.DEPOSIT_COMPLETED;

        BankTransaction bankTransaction = new SBankTransaction(senderUUID, bankAction, position,
                System.currentTimeMillis(), eventResult.getResult(), amount);

        addTransaction(bankTransaction, true);

        if (!eventResult.isCancelled())
            increaseBalance(amount);

        plugin.getMenus().refreshBankLogs(island);
        plugin.getMenus().refreshBankLogs(island);

        return bankTransaction;
    }

    @Override
    public boolean canDepositMoney(BigDecimal amount) {
        Preconditions.checkNotNull(amount, "amount parameter cannot be null.");
        return this.island.getBankLimit().compareTo(NO_BANK_LIMIT) <= 0 ||
                this.balance.get().add(amount).compareTo(this.island.getBankLimit()) > 0;
    }

    @Override
    public BankTransaction withdrawMoney(SuperiorPlayer superiorPlayer, BigDecimal amount, @Nullable List<String> commandsToExecute) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(amount, "amount parameter cannot be null.");

        BigDecimal withdrawAmount = balance.get().min(amount);

        BankTransaction bankTransaction;
        String failureReason;

        if (!island.hasPermission(superiorPlayer, IslandPrivileges.WITHDRAW_MONEY)) {
            failureReason = "No permission";
        } else if (this.balance.get().compareTo(BigDecimal.ZERO) <= 0) {
            failureReason = "Bank is empty";
        } else if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            failureReason = "Invalid amount";
        } else {
            PluginDebugger.debug("Action: Withdraw Money, Island: " + island.getOwner().getName() + ", Player: " + superiorPlayer.getName() + ", Money: " + withdrawAmount);

            EventResult<String> eventResult = EventsCaller.callIslandBankWithdrawEvent(superiorPlayer,
                    island, withdrawAmount);

            if (eventResult.isCancelled()) {
                failureReason = eventResult.getResult();
            } else if (commandsToExecute == null || commandsToExecute.isEmpty()) {
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

        int position = this.bankLogs.getLastTransactionPosition() + 1;

        if (failureReason == null || failureReason.isEmpty()) {
            bankTransaction = new SBankTransaction(superiorPlayer.getUniqueId(), BankAction.WITHDRAW_COMPLETED, position, System.currentTimeMillis(), "", withdrawAmount);
            decreaseBalance(withdrawAmount);

            addTransaction(bankTransaction, true);

            IslandUtils.sendMessage(island, Message.WITHDRAW_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName(), StringUtils.format(withdrawAmount));

            plugin.getMenus().refreshBankLogs(island);
            plugin.getMenus().refreshBankLogs(island);
        } else {
            bankTransaction = new SBankTransaction(superiorPlayer.getUniqueId(), BankAction.WITHDRAW_FAILED, position, System.currentTimeMillis(), failureReason, MONEY_FAILURE);
        }

        return bankTransaction;
    }

    @Override
    public BankTransaction withdrawAdminMoney(CommandSender commandSender, BigDecimal amount) {
        Preconditions.checkNotNull(commandSender, "commandSender parameter cannot be null.");
        Preconditions.checkNotNull(amount, "amount parameter cannot be null.");
        PluginDebugger.debug("Action: Withdraw Money, Island: " + island.getOwner().getName() + ", Player: " + commandSender.getName() + ", Money: " + amount);

        UUID senderUUID = commandSender instanceof Player ? ((Player) commandSender).getUniqueId() : null;

        int position = this.bankLogs.getLastTransactionPosition() + 1;

        EventResult<String> eventResult = EventsCaller.callIslandBankWithdrawEvent(commandSender instanceof Player ?
                plugin.getPlayers().getSuperiorPlayer(commandSender) : null, island, amount);

        BankAction bankAction = eventResult.isCancelled() ? BankAction.WITHDRAW_FAILED : BankAction.WITHDRAW_COMPLETED;

        BankTransaction bankTransaction = new SBankTransaction(senderUUID, bankAction, position,
                System.currentTimeMillis(), eventResult.getResult(), amount);

        if (!eventResult.isCancelled())
            decreaseBalance(amount);

        addTransaction(bankTransaction, true);

        plugin.getMenus().refreshBankLogs(island);
        plugin.getMenus().refreshBankLogs(island);

        return bankTransaction;
    }

    @Override
    public List<BankTransaction> getAllTransactions() {
        return this.bankLogs.getTransactions();
    }

    @Override
    public List<BankTransaction> getTransactions(SuperiorPlayer superiorPlayer) {
        return this.bankLogs.getTransactions(superiorPlayer.getUniqueId());
    }

    @Override
    public List<BankTransaction> getConsoleTransactions() {
        return this.bankLogs.getTransactions(CONSOLE_UUID);
    }

    @Override
    public void loadTransaction(BankTransaction bankTransaction) {
        addTransaction(bankTransaction, false);
    }

    private void addTransaction(BankTransaction bankTransaction, boolean save) {
        if (!BuiltinModules.BANK.bankLogs)
            return;

        UUID senderUUID = bankTransaction.getPlayer();

        this.bankLogs.addTransaction(bankTransaction, senderUUID == null ? CONSOLE_UUID : senderUUID, !save);

        if (save) {
            IslandsDatabaseBridge.saveBankTransaction(island, bankTransaction);
        }
    }

    private void decreaseBalance(BigDecimal amount) {
        increaseBalance(amount.negate());
    }

    private void increaseBalance(BigDecimal amount) {
        this.balance.updateAndGet(bigDecimal -> bigDecimal.add(amount).setScale(2, RoundingMode.HALF_DOWN));
        IslandsDatabaseBridge.saveBankBalance(island);
    }

}
