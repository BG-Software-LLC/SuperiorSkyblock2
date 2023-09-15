package com.bgsoftware.superiorskyblock.island.bank.logs;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.superiorskyblock.api.data.DatabaseFilter;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.island.bank.SBankTransaction;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
public class DatabaseBankLogs implements IBankLogs {

    private final LoadingCache<Integer, List<BankTransaction>> cachedBankTransactions = CacheBuilder.newBuilder()
            .maximumSize(1)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<Integer, List<BankTransaction>>() {
                @Override
                public List<BankTransaction> load(@NotNull Integer ignored) {
                    return loadTransactionsFromDatabase();
                }
            });

    private final Island island;
    private int lastTransactionPosition = -1;

    public DatabaseBankLogs(Island island) {
        this.island = island;
    }

    @Override
    public int getLastTransactionPosition() {
        if (lastTransactionPosition == -1) {
            lastTransactionPosition = getTransactions().size();
        }

        return lastTransactionPosition++;
    }

    @Override
    public List<BankTransaction> getTransactions() {
        return new SequentialListBuilder<BankTransaction>().build(cachedBankTransactions.getUnchecked(0));
    }

    @Override
    public List<BankTransaction> getTransactions(UUID playerUUID) {
        return new SequentialListBuilder<BankTransaction>()
                .filter(bankTransaction -> playerUUID.equals(bankTransaction.getPlayer()))
                .build(cachedBankTransactions.getUnchecked(0));
    }

    @Override
    public void addTransaction(BankTransaction bankTransaction, UUID senderUUID, boolean loadFromDatabase) {
        if (cachedBankTransactions.size() != 0)
            cachedBankTransactions.getUnchecked(0).add(bankTransaction);
    }

    private List<BankTransaction> loadTransactionsFromDatabase() {
        List<BankTransaction> bankTransactionsList = new LinkedList<>();
        island.getDatabaseBridge().loadObject("bank_transactions",
                DatabaseFilter.fromFilter("island", island.getUniqueId().toString()),
                bankTransactionRow -> SBankTransaction.fromDatabase(new DatabaseResult(bankTransactionRow))
                        .ifPresent(bankTransactionsList::add));
        return bankTransactionsList;
    }

}
