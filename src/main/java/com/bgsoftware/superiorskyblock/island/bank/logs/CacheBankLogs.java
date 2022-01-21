package com.bgsoftware.superiorskyblock.island.bank.logs;

import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.threads.SyncedObject;
import com.bgsoftware.superiorskyblock.utils.islands.SortingComparators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CacheBankLogs implements IBankLogs {

    private final SyncedObject<SortedSet<BankTransaction>> transactions = SyncedObject.of(new TreeSet<>(SortingComparators.BANK_TRANSACTIONS_COMPARATOR));
    private final Map<UUID, SyncedObject<List<BankTransaction>>> transactionsByPlayers = new ConcurrentHashMap<>();

    @Override
    public int getLastTransactionPosition() {
        return this.transactions.readAndGet(SortedSet::size);
    }

    @Override
    public List<BankTransaction> getTransactions() {
        return transactions.readAndGet(bankTransactions -> Collections.unmodifiableList(new ArrayList<>(bankTransactions)));
    }

    @Override
    public List<BankTransaction> getTransactions(UUID playerUUID) {
        SyncedObject<List<BankTransaction>> transactions = this.transactionsByPlayers.get(playerUUID);
        return transactions == null ? Collections.emptyList() : transactions.readAndGet(Collections::unmodifiableList);
    }

    @Override
    public void addTransaction(BankTransaction bankTransaction, UUID senderUUID, boolean loadFromDatabase) {
        transactions.write(transactions -> transactions.add(bankTransaction));
        transactionsByPlayers.computeIfAbsent(senderUUID, p -> SyncedObject.of(new ArrayList<>()))
                .write(transactions -> transactions.add(bankTransaction));
    }

}
