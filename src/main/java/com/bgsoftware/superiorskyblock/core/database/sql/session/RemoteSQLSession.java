package com.bgsoftware.superiorskyblock.core.database.sql.session;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public abstract class RemoteSQLSession implements SQLSession {

    protected final CompletableFuture<Void> ready = new CompletableFuture<>();
    protected final SuperiorSkyblockPlugin plugin;

    @Nullable
    protected HikariDataSource dataSource;
    protected boolean logging;

    protected RemoteSQLSession(SuperiorSkyblockPlugin plugin, boolean logging) {
        this.plugin = plugin;
        setLogging(logging);
    }

    @Override
    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    @Override
    public abstract boolean createConnection();

    @Override
    public void closeConnection() {
        Preconditions.checkNotNull(this.dataSource, "Session was not initialized.");
        dataSource.close();
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

        String prefix = plugin.getSettings().getDatabase().getPrefix();

        executeUpdate(String.format("CREATE TABLE IF NOT EXISTS %s%s (%s);",
                prefix, tableName, columnsSection.substring(1)), queryResult);
    }

    @Override
    public void renameTable(String tableName, String newName, QueryResult<Void> queryResult) {
        String prefix = plugin.getSettings().getDatabase().getPrefix();
        executeUpdate(String.format("RENAME TABLE %s%s TO %s%s;", prefix, tableName, prefix, newName), queryResult);
    }

    @Override
    public void createIndex(String indexName, String tableName, String[] columns, QueryResult<Void> queryResult) {
        StringBuilder columnsSection = new StringBuilder();
        for (String column : columns) {
            columnsSection.append(",").append(column);
        }

        String prefix = plugin.getSettings().getDatabase().getPrefix();

        executeUpdate(String.format("CREATE UNIQUE INDEX %s ON %s%s (%s);",
                indexName, prefix, tableName, columnsSection.substring(1)), queryResult);
    }

    @Override
    public void modifyColumnType(String tableName, String columnName, String newType, QueryResult<Void> queryResult) {
        String prefix = plugin.getSettings().getDatabase().getPrefix();
        executeUpdate(String.format("ALTER TABLE %s%s MODIFY COLUMN %s %s;",
                prefix, tableName, columnName, newType), queryResult);
    }

    @Override
    public void removePrimaryKey(String tableName, String columnName, QueryResult<Void> queryResult) {
        String prefix = plugin.getSettings().getDatabase().getPrefix();
        executeUpdate(String.format("ALTER TABLE %s%s DROP PRIMARY KEY;", prefix, tableName), queryResult);
    }

    @Override
    public void select(String tableName, String filters, QueryResult<ResultSet> queryResult) {
        String prefix = plugin.getSettings().getDatabase().getPrefix();
        executeQuery(String.format("SELECT * FROM %s%s%s;", prefix, tableName, filters), queryResult);
    }

    @Override
    public void setJournalMode(String jounralMode, QueryResult<ResultSet> queryResult) {
        executeQuery(String.format("PRAGMA journal_mode=%s;", jounralMode), queryResult);
    }

    @Override
    public void customQuery(String statement, QueryResult<PreparedStatement> queryResult) {
        Preconditions.checkNotNull(this.dataSource, "Session was not initialized.");

        String prefix = plugin.getSettings().getDatabase().getPrefix();
        String query = statement.replace("{prefix}", prefix);

        Log.debug(Debug.DATABASE_QUERY, query);

        try (Connection conn = this.dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            queryResult.complete(preparedStatement);
        } catch (SQLException error) {
            queryResult.fail(error);
        }
    }

    private void executeUpdate(String statement, QueryResult<Void> queryResult) {
        Preconditions.checkNotNull(this.dataSource, "Session was not initialized.");

        String query = statement
                .replace("BIG_DECIMAL", "TEXT")
                .replace("DECIMAL", "DECIMAL(10, 2)")
                .replace("UUID", "VARCHAR(36)")
                .replace("LONG_UNIQUE_TEXT", "VARCHAR(255)")
                .replace("UNIQUE_TEXT", "VARCHAR(30)");

        Log.debug(Debug.DATABASE_QUERY, query);

        try (Connection conn = this.dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.executeUpdate();
            queryResult.complete(null);
        } catch (SQLException error) {
            queryResult.fail(error);
        }
    }

    private void executeQuery(String query, QueryResult<ResultSet> queryResult) {
        Preconditions.checkNotNull(this.dataSource, "Session was not initialized.");

        Log.debug(Debug.DATABASE_QUERY, query);

        try (Connection conn = this.dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            queryResult.complete(resultSet);
        } catch (SQLException error) {
            queryResult.fail(error);
        }
    }

}
