package com.bgsoftware.superiorskyblock.database.sql.session;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class SQLiteSession implements SQLSession {


    private final CompletableFuture<Void> ready = new CompletableFuture<>();
    private final Object mutex = new Object();
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
        log("Trying to connect to SQLite database...");

        File file = new File(plugin.getDataFolder(), "database.db");

        if (!file.exists()) {
            if (!file.getParentFile().mkdirs())
                return false;
            try {
                if (!file.createNewFile())
                    return false;
            } catch (IOException error) {
                return false;
            }
        }

        String jdbcUrl = "jdbc:sqlite:" + file.getAbsolutePath().replace("\\", "/");

        try {
            Class.forName("org.sqlite.JDBC");

            conn = DriverManager.getConnection(jdbcUrl);

            log("Successfully established connection with SQLite database!");

            ready.complete(null);

            return true;
        } catch (Exception error) {
            log("&cFailed to connect to the SQLite database:");
            error.printStackTrace();
            PluginDebugger.debug(error);
        }

        return false;
    }

    @Override
    public void closeConnection() {
        Preconditions.checkNotNull(this.conn, "Session was not initialized.");

        try {
            conn.close();
        } catch (SQLException error) {
            error.printStackTrace();
            PluginDebugger.debug(error);
        }
    }

    @Override
    public void waitForConnection() {
        try {
            ready.get();
        } catch (Exception ex) {
            ex.printStackTrace();
            PluginDebugger.debug(ex);
        }
    }

    @Override
    public Object getMutex() {
        return mutex;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) {
        Preconditions.checkNotNull(this.conn, "Session was not initialized.");

        try {
            this.conn.setAutoCommit(autoCommit);
        } catch (SQLException error) {
            error.printStackTrace();
            PluginDebugger.debug(error);
        }
    }

    @Override
    public void commit() {
        Preconditions.checkNotNull(this.conn, "Session was not initialized.");

        try {
            this.conn.commit();
        } catch (SQLException error) {
            error.printStackTrace();
            PluginDebugger.debug(error);
        }
    }

    @Override
    public QueryResult<Void> createTable(String tableName, Pair<String, String>... columns) {
        StringBuilder columnsSection = new StringBuilder();
        for (Pair<String, String> column : columns) {
            columnsSection.append(",")
                    .append(column.getKey())
                    .append(" ")
                    .append(column.getValue());
        }

        String prefix = plugin.getSettings().getDatabase().getPrefix();

        return executeUpdate(String.format("CREATE TABLE IF NOT EXISTS %s%s (%s);",
                        prefix, tableName, columnsSection.substring(1)),
                QueryResult.VOID, QueryResult::ofFail);
    }

    @Override
    public QueryResult<Void> renameTable(String tableName, String newName) {
        String prefix = plugin.getSettings().getDatabase().getPrefix();
        return executeUpdate(String.format("RENAME TABLE %s%s TO %s%s;", prefix, tableName, prefix, newName),
                QueryResult.VOID, QueryResult::ofFail);
    }

    @Override
    public QueryResult<Void> createIndex(String indexName, String tableName, String... columns) {
        StringBuilder columnsSection = new StringBuilder();
        for (String column : columns) {
            columnsSection.append(",").append(column);
        }

        String prefix = plugin.getSettings().getDatabase().getPrefix();

        return executeUpdate(String.format("CREATE UNIQUE INDEX %s ON %s%s (%s);", indexName, prefix, tableName,
                columnsSection.substring(1)), QueryResult.VOID, QueryResult::ofFail);
    }

    @Override
    public QueryResult<Void> modifyColumnType(String tableName, String columnName, String newType) {
        String prefix = plugin.getSettings().getDatabase().getPrefix();
        return executeUpdate(String.format("ALTER TABLE %s%s MODIFY COLUMN %s %s;", prefix, tableName,
                columnName, newType), QueryResult.VOID, QueryResult::ofFail);
    }

    @Override
    public QueryResult<ResultSet> select(String tableName, String filters) {
        String prefix = plugin.getSettings().getDatabase().getPrefix();
        return executeQuery(String.format("SELECT * FROM %s%s%s;", prefix, tableName, filters),
                QueryResult::ofSuccess, QueryResult::ofFail);
    }

    @Override
    public QueryResult<ResultSet> setJournalMode(String jounralMode) {
        return executeQuery(String.format("PRAGMA journal_mode=%s;", jounralMode),
                QueryResult::ofSuccess, QueryResult::ofFail);
    }

    @Override
    public QueryResult<PreparedStatement> customQuery(String query) {
        Preconditions.checkNotNull(this.conn, "Session was not initialized.");

        String prefix = plugin.getSettings().getDatabase().getPrefix();

        try {
            return QueryResult.ofSuccess(this.conn.prepareStatement(query.replace("{prefix}", prefix)))
                    .onFinish(preparedStatement -> {
                        try {
                            preparedStatement.close();
                        } catch (SQLException ignored) {
                        }
                    });
        } catch (SQLException error) {
            return QueryResult.ofFail(error);
        }
    }

    private void log(String message) {
        if (logging)
            SuperiorSkyblockPlugin.log(message);
    }

    private <T> T executeUpdate(String statement, T success, Function<SQLException, T> onFailure) {
        Preconditions.checkNotNull(this.conn, "Session was not initialized.");

        String query = statement
                .replace("BIG_DECIMAL", "TEXT")
                .replace("UUID", "VARCHAR(36)")
                .replace("LONG_UNIQUE_TEXT", "VARCHAR(255)")
                .replace("UNIQUE_TEXT", "VARCHAR(30)");

        try (PreparedStatement preparedStatement = this.conn.prepareStatement(query)) {
            preparedStatement.executeUpdate();
        } catch (SQLException error) {
            return onFailure.apply(error);
        }

        return success;
    }

    private <T> T executeQuery(String statement, Function<ResultSet, T> callback, Function<SQLException, T> onFailure) {
        Preconditions.checkNotNull(this.conn, "Session was not initialized.");

        String prefix = plugin.getSettings().getDatabase().getPrefix();
        String query = statement.replace("{prefix}", prefix);

        try (PreparedStatement preparedStatement = this.conn.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            return callback.apply(resultSet);
        } catch (SQLException error) {
            return onFailure.apply(error);
        }
    }

}
