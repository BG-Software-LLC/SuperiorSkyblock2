package com.bgsoftware.superiorskyblock.core.database.sql;

import com.bgsoftware.common.databasebridge.DatabaseSessionFactory;
import com.bgsoftware.common.databasebridge.logger.ILogger;
import com.bgsoftware.common.databasebridge.session.IDatabaseSession;
import com.bgsoftware.common.databasebridge.sql.query.Column;
import com.bgsoftware.common.databasebridge.sql.query.QueryResult;
import com.bgsoftware.common.databasebridge.sql.session.MariaDBDatabaseSession;
import com.bgsoftware.common.databasebridge.sql.session.MySQLDatabaseSession;
import com.bgsoftware.common.databasebridge.sql.session.SQLDatabaseSession;
import com.bgsoftware.common.databasebridge.sql.session.SQLiteDatabaseSession;
import com.bgsoftware.common.databasebridge.transaction.IDatabaseTransaction;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.data.DatabaseFilter;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class DBSession {

    private static final ILogger LOGGER = new ILogger() {
        @Override
        public void error(String message, Throwable error) {
            Log.error(error, message);
        }

        @Override
        public boolean hasDebugEnabled() {
            return Log.isDebugged(Debug.DATABASE_QUERY);
        }

        @Override
        public void debug(String message) {
            if (hasDebugEnabled())
                Log.debug(Debug.DATABASE_QUERY, message);
        }

        @Override
        public void info(String message) {
            Log.info(message);
        }
    };

    private static SQLDatabaseSession<?> globalSession = null;

    private DBSession() {

    }

    public static boolean isReady() {
        return globalSession != null;
    }

    public static void waitForConnection() {
        if (isReady())
            globalSession.waitForConnection();
    }

    public static boolean createConnection(SuperiorSkyblockPlugin plugin) {
        SQLDatabaseSession<?> session = createSessionInternal(plugin, true);

        if (session.connect()) {
            globalSession = session;
            return true;
        }

        return false;
    }

    public static CompletableFuture<Void> execute(IDatabaseTransaction transaction) {
        return globalSession.execute(transaction);
    }

    public static CompletableFuture<Void> execute(IDatabaseTransaction... transactions) {
        return globalSession.execute(transactions);
    }

    public static CompletableFuture<Void> execute(Collection<IDatabaseTransaction> transactions) {
        return globalSession.execute(transactions);
    }

    public static void createTable(String tableName, Column... columns) {
        if (isReady())
            globalSession.createTable(tableName, columns, QueryResult.EMPTY_VOID_QUERY_RESULT);
    }

    public static void createIndex(String indexName, String tableName, String... columns) {
        if (isReady())
            globalSession.createIndex(indexName, tableName, columns, QueryResult.EMPTY_VOID_QUERY_RESULT);
    }

    public static void modifyColumnType(String tableName, String columnName, String newType) {
        if (isReady())
            globalSession.modifyColumnType(tableName, columnName, newType, QueryResult.EMPTY_VOID_QUERY_RESULT);
    }

    public static void addColumn(String tableName, String columnName, String type) {
        if (isReady())
            globalSession.addColumn(tableName, columnName, type, QueryResult.EMPTY_VOID_QUERY_RESULT);
    }

    public static void removePrimaryKey(String tableName, String columnName) {
        if (isReady())
            globalSession.removePrimaryKey(tableName, columnName, QueryResult.EMPTY_VOID_QUERY_RESULT);
    }

    public static void select(String tableName, String filters, QueryResult<ResultSet> queryResult) {
        if (isReady())
            globalSession.select(tableName, filters, queryResult);
    }

    public static void setJournalMode(String jounralMode, QueryResult<ResultSet> queryResult) {
        if (isReady())
            globalSession.setJournalMode(jounralMode, queryResult);
    }

    public static void customQuery(String query, QueryResult<PreparedStatement> queryResult) {
        if (isReady())
            globalSession.customQuery(query, queryResult);
    }

    public static void close() {
        if (isReady())
            globalSession.close();
    }

    public static String getColumnFilter(DatabaseFilter filter) {
        StringBuilder columnIdentifier = new StringBuilder();
        if (filter != null) {
            filter.forEach((column, value) -> {
                if (columnIdentifier.length() == 0) {
                    columnIdentifier.append(String.format(" WHERE %s=?", column));
                } else {
                    columnIdentifier.append(String.format(" AND %s=?", column));
                }
            });
        }
        return columnIdentifier.toString();
    }

    private static SQLDatabaseSession<?> createSessionInternal(SuperiorSkyblockPlugin plugin, boolean logging) {
        SettingsManager.Database database = plugin.getSettings().getDatabase();

        IDatabaseSession.Args args;
        switch (database.getType()) {
            case "MYSQL":
                args = new MySQLDatabaseSession.Args(database.getAddress(), database.getPort(), database.getDBName(),
                        database.getUsername(), database.getPassword(), database.getPrefix(), database.hasSSL(),
                        database.hasPublicKeyRetrieval(), database.getWaitTimeout(), database.getMaxLifetime(),
                        "SuperiorSkyblock Database Thread", LOGGER);
                break;
            case "MARIADB":
                args = new MariaDBDatabaseSession.Args(database.getAddress(), database.getPort(), database.getDBName(),
                        database.getUsername(), database.getPassword(), database.getPrefix(), database.hasSSL(),
                        database.hasPublicKeyRetrieval(), database.getWaitTimeout(), database.getMaxLifetime(),
                        "SuperiorSkyblock Database Thread", LOGGER);
                break;
            default:
                File databaseFile = new File(plugin.getDataFolder(), "datastore/database.db");
                args = new SQLiteDatabaseSession.Args(databaseFile,
                        "SuperiorSkyblock Database Thread", LOGGER);
                break;
        }

        SQLDatabaseSession<?> session = (SQLDatabaseSession<?>) DatabaseSessionFactory.createSession(args);
        if (logging)
            session.setLogging(true);
        return session;
    }

}

