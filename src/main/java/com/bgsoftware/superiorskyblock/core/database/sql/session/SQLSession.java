package com.bgsoftware.superiorskyblock.core.database.sql.session;

import com.bgsoftware.superiorskyblock.api.objects.Pair;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public interface SQLSession {

    void setLogging(boolean logging);

    boolean createConnection();

    void closeConnection();

    void waitForConnection();

    void createTable(String tableName, Pair<String, String>[] columns, QueryResult<Void> queryResult);

    void renameTable(String tableName, String newName, QueryResult<Void> queryResult);

    void createIndex(String indexName, String tableName, String[] columns, QueryResult<Void> queryResult);

    void modifyColumnType(String tableName, String columnName, String newType, QueryResult<Void> queryResult);

    void removePrimaryKey(String tableName, String columnName, QueryResult<Void> queryResult);

    void select(String tableName, String filters, QueryResult<ResultSet> queryResult);

    void setJournalMode(String jounralMode, QueryResult<ResultSet> queryResult);

    void customQuery(String query, QueryResult<PreparedStatement> queryResult);

}
