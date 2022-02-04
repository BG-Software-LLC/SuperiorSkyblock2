package com.bgsoftware.superiorskyblock.database.sql;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.database.sql.session.QueryResult;
import com.bgsoftware.superiorskyblock.threads.Executor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class StatementHolder {

    private final List<Map<Integer, Object>> batches = new ArrayList<>();

    private final Map<Integer, Object> values = new HashMap<>();
    private String query;
    private int currentIndex = 1;

    public StatementHolder(String statement) {
        setQuery(statement);
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void addBatch() {
        batches.add(new HashMap<>(values));
        values.clear();
        currentIndex = 1;
    }

    public StatementHolder setObject(Object value) {
        values.put(currentIndex++, value);
        return this;
    }

    public void executeBatch(boolean async) {
        if (query == null || !SQLHelper.isReady() || query.isEmpty() || batches.isEmpty())
            return;

        if (async && !Executor.isDataThread()) {
            Executor.data(() -> executeBatch(false));
            return;
        }

        SQLHelper.waitForConnection();

        try {
            StringHolder errorQuery = new StringHolder(query);

            Optional<Object> mutex = SQLHelper.getMutex();

            if (!mutex.isPresent())
                return;

            synchronized (mutex.get()) {
                SQLHelper.customQuery(query, new QueryResult<PreparedStatement>().onSuccess(preparedStatement -> {
                    Connection connection = preparedStatement.getConnection();
                    connection.setAutoCommit(false);

                    for (Map<Integer, Object> values : batches) {
                        for (Map.Entry<Integer, Object> entry : values.entrySet()) {
                            preparedStatement.setObject(entry.getKey(), entry.getValue());
                            errorQuery.value = errorQuery.value.replaceFirst("\\?", entry.getValue() + "");
                        }
                        preparedStatement.addBatch();
                    }

                    preparedStatement.executeBatch();

                    try {
                        connection.commit();
                    } catch (Throwable ignored) {
                    }

                    connection.setAutoCommit(true);
                }).onFail(error -> {
                    SuperiorSkyblockPlugin.log("&cFailed to execute query " + errorQuery);
                    error.printStackTrace();
                }));
            }
        } finally {
            values.clear();
        }
    }

    public void execute(boolean async) {
        if (!SQLHelper.isReady())
            return;

        if (async && !Executor.isDataThread()) {
            Executor.data(() -> execute(false));
            return;
        }

        SQLHelper.waitForConnection();

        try {
            StringHolder errorQuery = new StringHolder(query);

            Optional<Object> mutex = SQLHelper.getMutex();

            if (!mutex.isPresent())
                return;

            synchronized (mutex.get()) {
                SQLHelper.customQuery(query, new QueryResult<PreparedStatement>().onSuccess(preparedStatement -> {
                    for (Map.Entry<Integer, Object> entry : values.entrySet()) {
                        preparedStatement.setObject(entry.getKey(), entry.getValue());
                        errorQuery.value = errorQuery.value.replaceFirst("\\?", entry.getValue() + "");
                    }
                    preparedStatement.executeUpdate();
                }).onFail(error -> {
                    SuperiorSkyblockPlugin.log("&cFailed to execute query " + errorQuery);
                    error.printStackTrace();
                }));
            }
        } finally {
            values.clear();
        }
    }

    private static class StringHolder {

        private String value;

        StringHolder(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

}
