package com.bgsoftware.superiorskyblock.island.bank.logs;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseFilter;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.island.bank.SBankTransaction;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public final class DatabaseBankLogs implements IBankLogs {

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
        return Collections.unmodifiableList(cachedBankTransactions.getUnchecked(0));
    }

    @Override
    public List<BankTransaction> getTransactions(UUID playerUUID) {
        return Collections.unmodifiableList(cachedBankTransactions.getUnchecked(0).stream()
                .filter(bankTransaction -> playerUUID.equals(bankTransaction.getPlayer()))
                .collect(Collectors.toList()));
    }

    @Override
    public void addTransaction(BankTransaction bankTransaction, UUID senderUUID, boolean loadFromDatabase) {
        if (cachedBankTransactions.size() != 0)
            cachedBankTransactions.getUnchecked(0).add(bankTransaction);
    }

    private List<BankTransaction> loadTransactionsFromDatabase() {
        List<BankTransaction> bankTransactionsList = new ArrayList<>();
        island.getDatabaseBridge().loadObject("bank_transactions",
                new DatabaseFilter(Collections.singletonList(new Pair<>("island", island.getUniqueId().toString()))),
                bankTransactionRow -> {
                    try {
                        bankTransactionsList.add(new SBankTransaction(new DatabaseResult(bankTransactionRow)));
                    } catch (Exception error) {
                        SuperiorSkyblockPlugin.log("&cError occurred while loading bank transaction:");
                        error.printStackTrace();
                        PluginDebugger.debug(error);
                    }
                });
        return bankTransactionsList;
    }

}
