package com.bgsoftware.superiorskyblock.core.database.sql;

import com.bgsoftware.superiorskyblock.core.database.transaction.IDatabaseTransaction;
import com.bgsoftware.superiorskyblock.core.database.sql.session.QueryResult;
import com.bgsoftware.superiorskyblock.core.database.sql.transaction.SQLDatabaseTransaction;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.mutable.MutableInt;
import com.bgsoftware.superiorskyblock.core.mutable.MutableObject;
import com.google.common.base.Preconditions;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
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

            for (SQLDatabaseTransaction.DatabaseValues batchValues : allBatchValues) {
                fullQuery.setValue(query);
                MutableInt index = new MutableInt(1);
                for (Object value : batchValues.values) {
                    addObject(preparedStatement, index, value, fullQuery);
                }
                Log.debug(Debug.DATABASE_QUERY, fullQuery.getValue());
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
        }).onFail(error -> {
            Log.error(error, "An unexpected error occurred while executing query `", fullQuery.getValue(), "`:");
        }));
    }

    private void addObject(PreparedStatement preparedStatement, MutableInt index, Object value,
                           MutableObject<String> fullQuery) throws SQLException {
        int curr = index.get();
        preparedStatement.setObject(curr, value);
        index.set(curr + 1);
        fullQuery.setValue(QUERY_VALUE_PATTERN.matcher(fullQuery.getValue()).replaceFirst(value + ""));
    }

    private void executeQuery(String query, QueryResult<PreparedStatement> callback) {
        SQLHelper.waitForConnection();
        SQLHelper.customQuery(query, callback);
    }

}
