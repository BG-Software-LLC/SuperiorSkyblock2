package com.bgsoftware.superiorskyblock.island.bank.logs;

import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;

import java.util.List;
import java.util.UUID;

public interface IBankLogs {

    int getLastTransactionPosition();

    List<BankTransaction> getTransactions();

    List<BankTransaction> getTransactions(UUID playerUUID);

    void addTransaction(BankTransaction bankTransaction, UUID senderUUID, boolean loadFromDatabase);

}
