package com.bgsoftware.superiorskyblock.core.database.sql.session.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.database.sql.session.QueryResult;
import com.bgsoftware.superiorskyblock.core.database.sql.session.SQLSession;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class SQLiteSession implements SQLSession {

    private final CompletableFuture<Void> ready = new CompletableFuture<>();
    private final SuperiorSkyblockPlugin plugin;

    @Nullable
    private Connection conn;
    private boolean logging;

    public SQLiteSession(SuperiorSkyblockPlugin plugin, boolean logging) {
        this.plugin = plugin;

        setLogging(logging);
    }

    @Override
    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    @Override
    public boolean createConnection() {
        if (logging) Log.info("Trying to connect to local database (SQLite)...");

        File file = new File(plugin.getDataFolder(), "datastore/database.db");

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                if (!file.createNewFile()) {
                    Log.error("Failed to create SQLite database file.");
                    return false;
                }
            } catch (IOException error) {
                Log.error(error, "An unexpected error occurred while creating the database file:");
                return false;
            }
        }

        String jdbcUrl = "jdbc:sqlite:" + file.getAbsolutePath().replace("\\", "/");

        try {
            Class.forName("org.sqlite.JDBC");

            conn = DriverManager.getConnection(jdbcUrl);

            if (logging) Log.info("Successfully established connection with local database!");

            ready.complete(null);

            return true;
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while connecting to SQLite database:");
        }

        return false;
    }

    @Override
    public void closeConnection() {
        Preconditions.checkNotNull(this.conn, "Session was not initialized.");

        try {
            conn.close();
        } catch (SQLException error) {
            Log.error(error, "An unexpected error occurred while closing connection to SQLite database:");
        }
    }

    @Override
    public void waitForConnection() {
        try {
            ready.get();
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while waiting for connection:");
        }
    }

    @Override
    public void createTable(String tableName, Pair<String, String>[] columns, QueryResult<Void> queryResult) {
        StringBuilder columnsSection = new StringBuilder();
        for (Pair<String, String> column : columns) {
            columnsSection.append(",")
                    .append(column.getKey())
                    .append(" ")
                    .append(column.getValue());
        }

        executeUpdate(String.format("CREATE TABLE IF NOT EXISTS %s (%s);",
                tableName, columnsSection.substring(1)), queryResult);
    }

    @Override
    public void renameTable(String tableName, String newName, QueryResult<Void> queryResult) {
        executeUpdate(String.format("ALTER TABLE %s RENAME TO %s;", tableName, newName), queryResult);
    }

    @Override
    public void createIndex(String indexName, String tableName, String[] columns, QueryResult<Void> queryResult) {
        StringBuilder columnsSection = new StringBuilder();
        for (String column : columns) {
            columnsSection.append(",").append(column);
        }

        executeUpdate(String.format("CREATE UNIQUE INDEX %s ON %s (%s);",
                indexName, tableName, columnsSection.substring(1)), queryResult);
    }

    @Override
    public void modifyColumnType(String tableName, String columnName, String newType, QueryResult<Void> queryResult) {
        executeUpdate(String.format("ALTER TABLE %s MODIFY COLUMN %s %s;",
                tableName, columnName, newType), queryResult);
    }

    @Override
    public void removePrimaryKey(String tableName, String columnName, QueryResult<Void> queryResult) {
        executeQuery(String.format("PRAGMA table_info('%s')", tableName), new QueryResult<ResultSet>()
                .onSuccess(resultSet -> {
                    if (resultSet.next() && resultSet.getInt("pk") == 1) {
                        resultSet.close();
                        executeUpdate(String.format("DROP TABLE %s_copy;", tableName), QueryResult.EMPTY_VOID_QUERY_RESULT);
                        executeUpdate(String.format("CREATE TABLE %s_copy AS SELECT * FROM %s;", tableName, tableName), queryResult);
                        executeUpdate(String.format("DROP TABLE %s;", tableName), queryResult);
                        renameTable(tableName + "_copy", tableName, queryResult);
                    }
                }).onFail(queryResult::fail));
    }

    @Override
    public void select(String tableName, String filters, QueryResult<ResultSet> queryResult) {
        executeQuery(String.format("SELECT * FROM %s%s;", tableName, filters), queryResult);
    }

    @Override
    public void setJournalMode(String jounralMode, QueryResult<ResultSet> queryResult) {
        executeQuery(String.format("PRAGMA journal_mode=%s;", jounralMode), queryResult);
    }

    @Override
    public void customQuery(String query, QueryResult<PreparedStatement> queryResult) {
        Preconditions.checkNotNull(this.conn, "Session was not initialized.");

        try (PreparedStatement preparedStatement =
                     this.conn.prepareStatement(query.replace("{prefix}", ""))) {
            queryResult.complete(preparedStatement);
        } catch (SQLException error) {
            queryResult.fail(error);
        }
    }

    private void executeUpdate(String statement, QueryResult<Void> queryResult) {
        Preconditions.checkNotNull(this.conn, "Session was not initialized.");

        String query = statement
                .replace("BIG_DECIMAL", "TEXT")
                .replace("UUID", "VARCHAR(36)")
                .replace("LONG_UNIQUE_TEXT", "VARCHAR(255)")
                .replace("UNIQUE_TEXT", "VARCHAR(30)");

        Log.debug(Debug.DATABASE_QUERY, query);

        try (PreparedStatement preparedStatement = this.conn.prepareStatement(query)) {
            preparedStatement.executeUpdate();
            queryResult.complete(null);
        } catch (SQLException error) {
            queryResult.fail(error);
        }
    }

    private void executeQuery(String query, QueryResult<ResultSet> queryResult) {
        Preconditions.checkNotNull(this.conn, "Session was not initialized.");

        Log.debug(Debug.DATABASE_QUERY, query);

        try (PreparedStatement preparedStatement = this.conn.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            queryResult.complete(resultSet);
        } catch (SQLException error) {
            queryResult.fail(error);
        }
    }

}
