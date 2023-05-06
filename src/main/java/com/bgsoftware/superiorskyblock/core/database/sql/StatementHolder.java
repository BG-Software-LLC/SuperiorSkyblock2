package com.bgsoftware.superiorskyblock.core.database.sql;

import com.bgsoftware.superiorskyblock.core.Mutable;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.database.sql.session.QueryResult;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.logging.StackTrace;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class StatementHolder {

    private static final Pattern QUERY_VALUE_PATTERN = Pattern.compile("\\?");

    private final List<List<Object>> batches = new LinkedList<>();

    private final List<Object> values = new LinkedList<>();
    private String query;

    public StatementHolder(String statement) {
        setQuery(statement);
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void addBatch() {
        this.batches.add(new LinkedList<>(this.values));
        this.values.clear();
    }

    public StatementHolder setObject(Object value) {
        this.values.add(value);
        return this;
    }

    public void executeBatch(boolean async) {
        if (this.batches.isEmpty())
            return;

        // Commit changes if exists.
        if (!this.values.isEmpty())
            addBatch();

        Mutable<String> rawQuery = new Mutable<>(this.query);

        executeQuery(async, new QueryResult<PreparedStatement>().onSuccess(preparedStatement -> {
            Connection connection = preparedStatement.getConnection();
            connection.setAutoCommit(false);

            for (List<Object> values : batches) {
                rawQuery.setValue(this.query);

                populateStatement(preparedStatement, values, rawQuery);

                Log.debug(Debug.DATABASE_QUERY, rawQuery.getValue());

                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();

            try {
                connection.commit();
            } catch (Throwable ignored) {
            }

            connection.setAutoCommit(true);
        }).onFail(error -> {
            Log.error(error, "An unexpected error occurred while executing query `", rawQuery.getValue(), "`:");
        }));
    }

    public void execute(boolean async) {
        Mutable<String> rawQuery = new Mutable<>(query);

        executeQuery(async, new QueryResult<PreparedStatement>().onSuccess(preparedStatement -> {
            populateStatement(preparedStatement, this.values, rawQuery);

            Log.debug(Debug.DATABASE_QUERY, rawQuery.getValue());

            preparedStatement.executeUpdate();
        }).onFail(error -> {
            Log.error(error, "An unexpected error occurred while executing query `", rawQuery.getValue(), "`:");
        }));
    }

    private void executeQuery(boolean async, QueryResult<PreparedStatement> queryResult) {
        if (Text.isBlank(query) || !SQLHelper.isReady())
            return;

        if (async && !BukkitExecutor.isDataThread()) {
            StackTrace stackTrace = new StackTrace();
            BukkitExecutor.data(() -> {
                try {
                    Log.attachStackTrace(stackTrace);
                    executeQuery(false, queryResult);
                } finally {
                    Log.detachStackTrace();
                }
            });
            return;
        }

        SQLHelper.waitForConnection();

        try {
            SQLHelper.customQuery(query, queryResult);
        } finally {
            values.clear();
        }
    }

    private static void populateStatement(PreparedStatement preparedStatement, List<Object> values,
                                          Mutable<String> rawQuery) throws SQLException {
        int currentIndex = 1;
        for (Object value : values) {
            preparedStatement.setObject(currentIndex++, value);
            rawQuery.setValue(QUERY_VALUE_PATTERN.matcher(rawQuery.getValue()).replaceFirst(value + ""));
        }
    }

}
