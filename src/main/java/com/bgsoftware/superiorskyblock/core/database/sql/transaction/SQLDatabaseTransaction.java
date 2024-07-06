package com.bgsoftware.superiorskyblock.core.database.sql.transaction;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.core.database.transaction.DatabaseTransactionsExecutor;
import com.bgsoftware.superiorskyblock.core.database.transaction.IDatabaseTransaction;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class SQLDatabaseTransaction<T extends SQLDatabaseTransaction<T>> implements IDatabaseTransaction {

    protected final List<DatabaseValues> values = new LinkedList<>();
    protected DatabaseValues currentValues;

    protected SQLDatabaseTransaction() {
    }

    public abstract String buildQuery();

    public T bindObject(Object value) {
        ensureDatabaseValues();
        currentValues.values.add(value);
        return (T) this;
    }

    public T bindObjects(List<Object> values) {
        ensureDatabaseValues();
        currentValues.values.addAll(values);
        return (T) this;
    }

    public T newBatch() {
        currentValues = null;
        return (T) this;
    }

    public List<DatabaseValues> getValues() {
        return values;
    }

    private void ensureDatabaseValues() {
        if (currentValues == null) {
            currentValues = new DatabaseValues();
            values.add(currentValues);
        }
    }

    public CompletableFuture<Void> execute() {
        return DatabaseTransactionsExecutor.addTransaction(this);
    }

    protected static String getColumnsFilter(@Nullable List<String> columnNames) {
        if (columnNames == null || columnNames.isEmpty())
            return "";

        StringBuilder columnIdentifier = new StringBuilder();
        for (String columnName : columnNames) {
            if (columnIdentifier.length() == 0) {
                columnIdentifier.append(String.format(" WHERE %s=?", columnName));
            } else {
                columnIdentifier.append(String.format(" AND %s=?", columnName));
            }
        }
        return columnIdentifier.toString();
    }

    public static class DatabaseValues {

        public final List<Object> values = new LinkedList<>();

    }

}
