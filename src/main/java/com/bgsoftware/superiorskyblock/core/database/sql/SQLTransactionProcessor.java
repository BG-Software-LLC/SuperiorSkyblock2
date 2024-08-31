package com.bgsoftware.superiorskyblock.core.database.sql;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.core.database.sql.session.QueryResult;
import com.bgsoftware.superiorskyblock.core.database.sql.transaction.SQLDatabaseTransaction;
import com.bgsoftware.superiorskyblock.core.database.transaction.IDatabaseTransaction;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.mutable.MutableInt;
import com.bgsoftware.superiorskyblock.core.mutable.MutableObject;
import com.google.common.base.Preconditions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLTransactionProcessor {

    private static final Pattern QUERY_VALUE_PATTERN = Pattern.compile("\\?");

    public SQLTransactionProcessor() {

    }

    public void processTransaction(IDatabaseTransaction transaction) {
        Preconditions.checkArgument(transaction instanceof SQLDatabaseTransaction,
                "Transaction is not SQL transaction: " + transaction);

        SQLDatabaseTransaction<?> sqlTransaction = (SQLDatabaseTransaction<?>) transaction;

        String query = sqlTransaction.buildQuery();

        MutableObject<String> fullQuery = new MutableObject<>(query);

        executeQuery(query, new QueryResult<PreparedStatement>().onSuccess(preparedStatement -> {
            List<SQLDatabaseTransaction.DatabaseValues> allBatchValues = sqlTransaction.getValues();
            if (allBatchValues.size() > 1) {
                executeBatchTransaction(preparedStatement, allBatchValues, query);
            } else if (allBatchValues.size() == 1) {
                executeTransaction(preparedStatement, allBatchValues.iterator().next(), query);
            }
        }).onFail(error -> {
            Log.error(error, "An unexpected error occurred while executing query `", fullQuery.getValue(), "`:");
        }));
    }

    private static void executeBatchTransaction(PreparedStatement preparedStatement,
                                                List<SQLDatabaseTransaction.DatabaseValues> allBatchValues,
                                                String query) throws SQLException {
        MutableObject<String> fullQuery = Log.isDebugged(Debug.DATABASE_QUERY) ? new MutableObject<>(query) : null;

        Connection connection = preparedStatement.getConnection();

        try {
            connection.setAutoCommit(false);

            for (SQLDatabaseTransaction.DatabaseValues batchValues : allBatchValues) {
                if (fullQuery != null)
                    fullQuery.setValue(query);

                populateStatement(preparedStatement, batchValues, fullQuery);

                if (fullQuery != null)
                    Log.debug(Debug.DATABASE_QUERY, fullQuery.getValue());

                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();

            try {
                connection.commit();
            } catch (Throwable ignored) {
            }
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private static void executeTransaction(PreparedStatement preparedStatement,
                                           SQLDatabaseTransaction.DatabaseValues values,
                                           String query) throws SQLException {
        MutableObject<String> fullQuery = Log.isDebugged(Debug.DATABASE_QUERY) ? new MutableObject<>(query) : null;

        populateStatement(preparedStatement, values, fullQuery);

        if (fullQuery != null)
            Log.debug(Debug.DATABASE_QUERY, fullQuery.getValue());

        preparedStatement.executeUpdate();
    }

    private static void populateStatement(PreparedStatement preparedStatement,
                                          SQLDatabaseTransaction.DatabaseValues values,
                                          @Nullable MutableObject<String> fullQuery) throws SQLException {
        MutableInt index = new MutableInt(1);
        for (Object value : values.values) {
            addObject(preparedStatement, index, value, fullQuery);
        }
    }

    private static void addObject(PreparedStatement preparedStatement, MutableInt index, Object value,
                                  @Nullable MutableObject<String> fullQuery) throws SQLException {
        int curr = index.get();
        preparedStatement.setObject(curr, value);
        index.set(curr + 1);
        if (fullQuery != null) {
            fullQuery.setValue(QUERY_VALUE_PATTERN.matcher(fullQuery.getValue())
                    .replaceFirst(Matcher.quoteReplacement(value + "")));
        }
    }

    private static void executeQuery(String query, QueryResult<PreparedStatement> callback) {
        SQLHelper.waitForConnection();
        SQLHelper.customQuery(query, callback);
    }

}
