package com.bgsoftware.superiorskyblock.core.database.sql;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.database.sql.session.QueryResult;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StatementHolder {

    private final List<Map<Integer, Object>> batches = new LinkedList<>();

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
        if (batches.isEmpty())
            return;

        StringHolder errorQuery = new StringHolder(query);

        executeQuery(async, new QueryResult<PreparedStatement>().onSuccess(preparedStatement -> {
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

    public void execute(boolean async) {
        StringHolder errorQuery = new StringHolder(query);

        executeQuery(async, new QueryResult<PreparedStatement>().onSuccess(preparedStatement -> {
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

    private void executeQuery(boolean async, QueryResult<PreparedStatement> queryResult) {
        if (Text.isBlank(query) || !SQLHelper.isReady())
            return;

        if (async && !BukkitExecutor.isDataThread()) {
            BukkitExecutor.data(() -> executeQuery(false, queryResult));
            return;
        }

        SQLHelper.waitForConnection();

        try {
            Optional<Object> mutex = SQLHelper.getMutex();

            if (!mutex.isPresent())
                return;

            synchronized (mutex.get()) {
                SQLHelper.customQuery(query, queryResult);
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
