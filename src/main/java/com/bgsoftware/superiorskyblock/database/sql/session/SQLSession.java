package com.bgsoftware.superiorskyblock.database.sql.session;

import com.bgsoftware.superiorskyblock.api.objects.Pair;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public interface SQLSession {

    void setLogging(boolean logging);

    boolean createConnection();

    void closeConnection();

    void waitForConnection();

    Object getMutex();

    void setAutoCommit(boolean autoCommit);

    void commit();

    QueryResult<Void> createTable(String tableName, Pair<String, String>... columns);

    QueryResult<Void> renameTable(String tableName, String newName);

    QueryResult<Void> createIndex(String indexName, String tableName, String... columns);

    QueryResult<Void> modifyColumnType(String tableName, String columnName, String newType);

    QueryResult<ResultSet> select(String tableName, String filters);

    QueryResult<ResultSet> setJournalMode(String jounralMode);

    QueryResult<PreparedStatement> customQuery(String query);

}
