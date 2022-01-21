package com.bgsoftware.superiorskyblock.database.sql;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.database.sql.session.MariaDBSession;
import com.bgsoftware.superiorskyblock.database.sql.session.MySQLSession;
import com.bgsoftware.superiorskyblock.database.sql.session.QueryResult;
import com.bgsoftware.superiorskyblock.database.sql.session.SQLSession;
import com.bgsoftware.superiorskyblock.database.sql.session.SQLiteSession;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public final class SQLHelper {

    private static SQLSession globalSession = null;

    private SQLHelper() {

    }

    public static boolean isReady() {
        return globalSession != null;
    }

    public static void waitForConnection() {
        if (isReady())
            globalSession.waitForConnection();
    }

    public static Optional<Object> getMutex() {
        return Optional.ofNullable(isReady() ? globalSession.getMutex() : null);
    }

    public static boolean createConnection(SuperiorSkyblockPlugin plugin) {
        SQLSession session;

        switch (plugin.getSettings().getDatabase().getType().toUpperCase()) {
            case "MYSQL":
                session = new MySQLSession(plugin, true);
                break;
            case "MARIADB":
                session = new MariaDBSession(plugin, true);
                break;
            default:
                session = new SQLiteSession(plugin, true);
                break;
        }

        if (session.createConnection()) {
            globalSession = session;
            return true;
        }

        return false;
    }

    public static void createTable(String tableName, Pair<String, String>... columns) {
        if (isReady())
            globalSession.createTable(tableName, columns);
    }

    public static void createIndex(String indexName, String tableName, String... columns) {
        if (isReady())
            globalSession.createIndex(indexName, tableName, columns);
    }

    public static void modifyColumnType(String tableName, String columnName, String newType) {
        if (isReady())
            globalSession.modifyColumnType(tableName, columnName, newType);
    }

    public static QueryResult<ResultSet> select(String tableName, String filters) {
        return isReady() ? globalSession.select(tableName, filters) : QueryResult.RESULT_SET_ERROR;
    }

    public static void setJournalMode(String jounralMode) {
        if (isReady())
            globalSession.setJournalMode(jounralMode);
    }

    public static QueryResult<PreparedStatement> customQuery(String query) {
        return isReady() ? globalSession.customQuery(query) : QueryResult.PREPARED_STATEMENT_ERROR;
    }

    public static void close() {
        if (isReady())
            globalSession.closeConnection();
    }

    public static void setAutoCommit(boolean autoCommit) {
        if (isReady())
            globalSession.setAutoCommit(autoCommit);
    }

    public static void commit() {
        if (isReady())
            globalSession.commit();
    }

}

