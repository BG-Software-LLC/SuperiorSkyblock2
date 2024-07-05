package com.bgsoftware.superiorskyblock.core.database.transaction;

import com.bgsoftware.superiorskyblock.core.database.sql.SQLTransactionProcessor;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class DatabaseTransactionsExecutor {

    private static final SQLTransactionProcessor PROCESSOR = new SQLTransactionProcessor();

    private static final BlockingQueue<PendingTransaction> pendingTransactions = new LinkedBlockingQueue<>();
    private static final AtomicBoolean IS_RUNNING = new AtomicBoolean(true);
    private static final CompletableFuture<Void> STOP_CONDITION = new CompletableFuture<>();


    private DatabaseTransactionsExecutor() {

    }

    public static CompletableFuture<Void> addTransaction(IDatabaseTransaction transaction) {
        Preconditions.checkState(IS_RUNNING.get(), "Database Executor is not running");
        return addPendingTransaction(new PendingTransaction(transaction));
    }

    public static CompletableFuture<Void> addTransactions(Collection<IDatabaseTransaction> transactions) {
        Preconditions.checkState(IS_RUNNING.get(), "Database Executor is not running");

        if (transactions.isEmpty())
            return CompletableFuture.completedFuture(null);

        return addPendingTransaction(new PendingTransaction(new MultipleDatabaseTransactions(transactions)));
    }

    public static CompletableFuture<Void> addTransactions(IDatabaseTransaction... transactions) {
        Preconditions.checkState(IS_RUNNING.get(), "Database Executor is not running");

        if (transactions.length == 0)
            return CompletableFuture.completedFuture(null);

        if (transactions.length == 1)
            return addTransaction(transactions[0]);

        List<IDatabaseTransaction> transactionList = new LinkedList<>();
        Collections.addAll(transactionList, transactions);
        return addTransactions(transactionList);
    }

    private static CompletableFuture<Void> addPendingTransaction(PendingTransaction pendingTransaction) {
        pendingTransactions.add(pendingTransaction);
        return pendingTransaction.waitable;
    }

    public static void init() {
        Thread thread = new ThreadFactoryBuilder()
                .setNameFormat("SuperiorSkyblock Database Thread")
                .build()
                .newThread(DatabaseTransactionsExecutor::transactionsHandler);
        thread.start();
    }

    public static void stop() {
        IS_RUNNING.set(false);
        try {
            STOP_CONDITION.get();
        } catch (ExecutionException | InterruptedException ignored) {
        }
    }

    private static void transactionsHandler() {
        while (IS_RUNNING.get()) {
            try {
                handleNextTransactionSafe();
            } catch (Throwable error) {
                error.printStackTrace();
            }
        }

        // Handle all pending transactions
        while (!pendingTransactions.isEmpty())
            handleNextTransactionSafe();

        STOP_CONDITION.complete(null);
    }

    private static void handleNextTransactionSafe() {
        PendingTransaction transaction;
        try {
            transaction = pendingTransactions.poll(1, TimeUnit.SECONDS);
        } catch (InterruptedException error) {
            return;
        }

        if (transaction != null) {
            processTransaction(transaction);
        }
    }

    private static void processTransaction(PendingTransaction pendingTransaction) {
        IDatabaseTransaction transaction = pendingTransaction.transaction;
        if (transaction instanceof MultipleDatabaseTransactions) {
            for (IDatabaseTransaction innerTransaction : ((MultipleDatabaseTransactions) transaction).getTransactions())
                PROCESSOR.processTransaction(innerTransaction);
        } else {
            PROCESSOR.processTransaction(transaction);
        }
        pendingTransaction.waitable.complete(null);
    }

    private static class MultipleDatabaseTransactions implements IDatabaseTransaction {

        private final List<IDatabaseTransaction> transactions;

        MultipleDatabaseTransactions(Collection<IDatabaseTransaction> transactions) {
            this.transactions = new LinkedList<>(transactions);
        }

        List<IDatabaseTransaction> getTransactions() {
            return transactions;
        }

    }

    private static class PendingTransaction {

        private final IDatabaseTransaction transaction;
        private final CompletableFuture<Void> waitable;

        PendingTransaction(IDatabaseTransaction transaction) {
            this.transaction = transaction;
            this.waitable = new CompletableFuture<>();
        }

    }


}
