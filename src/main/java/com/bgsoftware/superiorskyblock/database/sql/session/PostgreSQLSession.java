package com.bgsoftware.superiorskyblock.database.sql.session;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public final class PostgreSQLSession implements SQLSession {


    private final CompletableFuture<Void> ready = new CompletableFuture<>();
    private final Object mutex = new Object();
    private final SuperiorSkyblockPlugin plugin;

    @Nullable
    private HikariDataSource dataSource;
    private boolean logging;

    public PostgreSQLSession(SuperiorSkyblockPlugin plugin, boolean logging) {
        this.plugin = plugin;

        setLogging(logging);
    }

    @Override
    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    @Override
    public boolean createConnection() {
        log("Trying to connect to remote database (PostgreSQL)...");

        try {
            HikariConfig config = new HikariConfig();
            config.setConnectionTestQuery("SELECT 1");
            config.setPoolName("SuperiorSkyblock Pool");

            String address = plugin.getSettings().getDatabase().getAddress();
            String dbName = plugin.getSettings().getDatabase().getDBName();
            int port = plugin.getSettings().getDatabase().getPort();
            config.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s", address, port, dbName));

            config.setUsername(plugin.getSettings().getDatabase().getUsername());
            config.setPassword(plugin.getSettings().getDatabase().getPassword());
            config.setMinimumIdle(5);
            config.setMaximumPoolSize(50);
            config.setConnectionTimeout(10000);
            config.setIdleTimeout(plugin.getSettings().getDatabase().getWaitTimeout());
            config.setMaxLifetime(plugin.getSettings().getDatabase().getMaxLifetime());
            config.addDataSourceProperty("characterEncoding", "utf8");
            config.addDataSourceProperty("useUnicode", "true");

            dataSource = new HikariDataSource(config);

            log("Successfully established connection with remote database!");

            ready.complete(null);

            return true;
        } catch (Throwable error) {
            log("&cFailed to connect to the remote database:");
            error.printStackTrace();
            PluginDebugger.debug(error);
        }

        return false;
    }

    @Override
    public void closeConnection() {
        Preconditions.checkNotNull(this.dataSource, "Session was not initialized.");

        dataSource.close();
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
        Preconditions.checkNotNull(this.dataSource, "Session was not initialized.");

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(autoCommit);
        } catch (SQLException error) {
            error.printStackTrace();
            PluginDebugger.debug(error);
        }
    }

    @Override
    public void commit() {
        Preconditions.checkNotNull(this.dataSource, "Session was not initialized.");

        try (Connection conn = dataSource.getConnection()) {
            conn.commit();
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
                prefix, tableName, columnsSection.substring(1)));
    }

    @Override
    public QueryResult<Void> renameTable(String tableName, String newName) {
        String prefix = plugin.getSettings().getDatabase().getPrefix();
        return executeUpdate(String.format("RENAME TABLE %s%s TO %s%s;", prefix, tableName, prefix, newName));
    }

    @Override
    public QueryResult<Void> createIndex(String indexName, String tableName, String... columns) {
        StringBuilder columnsSection = new StringBuilder();
        for (String column : columns) {
            columnsSection.append(",").append(column);
        }

        String prefix = plugin.getSettings().getDatabase().getPrefix();

        return executeUpdate(String.format("CREATE UNIQUE INDEX %s ON %s%s (%s);", indexName, prefix, tableName,
                columnsSection.substring(1)));
    }

    @Override
    public QueryResult<Void> modifyColumnType(String tableName, String columnName, String newType) {
        String prefix = plugin.getSettings().getDatabase().getPrefix();
        return executeUpdate(String.format("ALTER TABLE %s%s MODIFY COLUMN %s %s;",
                prefix, tableName, columnName, newType));
    }

    @Override
    public QueryResult<ResultSet> select(String tableName, String filters) {
        String prefix = plugin.getSettings().getDatabase().getPrefix();
        return executeQuery(String.format("SELECT * FROM %s%s%s;", prefix, tableName, filters));
    }

    @Override
    public QueryResult<ResultSet> setJournalMode(String jounralMode) {
        return QueryResult.ofFail(new UnsupportedOperationException("Cannot change journal mode in maria-db"));
    }

    @Override
    public QueryResult<PreparedStatement> customQuery(String query) {
        Preconditions.checkNotNull(this.dataSource, "Session was not initialized.");

        String prefix = plugin.getSettings().getDatabase().getPrefix();

        try {
            Connection conn = this.dataSource.getConnection();
            try {
                return QueryResult.ofSuccess(conn.prepareStatement(query.replace("{prefix}", prefix)))
                        .onFinish(preparedStatement -> {
                            try {
                                preparedStatement.close();
                                conn.close();
                            } catch (SQLException ignored) {
                            }
                        });
            } catch (SQLException error) {
                conn.close();
                return QueryResult.ofFail(error);
            }
        } catch (SQLException error) {
            return QueryResult.ofFail(error);
        }
    }

    private void log(String message) {
        if (logging)
            SuperiorSkyblockPlugin.log(message);
    }

    private QueryResult<Void> executeUpdate(String statement) {
        Preconditions.checkNotNull(this.dataSource, "Session was not initialized.");

        String query = statement
                .replace("BIG_DECIMAL", "TEXT")
                .replace("UUID", "VARCHAR(36)")
                .replace("LONG_UNIQUE_TEXT", "VARCHAR(255)")
                .replace("UNIQUE_TEXT", "VARCHAR(30)");

        try (Connection conn = this.dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.executeUpdate();
        } catch (SQLException error) {
            return QueryResult.ofFail(error);
        }

        return QueryResult.VOID;
    }

    private QueryResult<ResultSet> executeQuery(String statement) {
        Preconditions.checkNotNull(this.dataSource, "Session was not initialized.");

        String query = statement.replace("{prefix}", "");

        try {
            Connection conn = this.dataSource.getConnection();
            try {
                PreparedStatement preparedStatement = conn.prepareStatement(query);
                try {
                    return QueryResult.ofSuccess(preparedStatement.executeQuery()).onFinish(resultSet -> {
                        try {
                            resultSet.close();
                            preparedStatement.close();
                            conn.close();
                        } catch (SQLException ignored) {
                        }
                    });
                } catch (SQLException error) {
                    QueryResult<ResultSet> queryResult = QueryResult.ofFail(error);
                    return queryResult.onFinish(v -> {
                        try {
                            preparedStatement.close();
                            conn.close();
                        } catch (SQLException ignored) {
                        }
                    });
                }
            } catch (SQLException error) {
                QueryResult<ResultSet> queryResult = QueryResult.ofFail(error);
                return queryResult.onFinish(v -> {
                    try {
                        conn.close();
                    } catch (SQLException ignored) {
                    }
                });
            }
        } catch (SQLException error) {
            return QueryResult.ofFail(error);
        }
    }

}
