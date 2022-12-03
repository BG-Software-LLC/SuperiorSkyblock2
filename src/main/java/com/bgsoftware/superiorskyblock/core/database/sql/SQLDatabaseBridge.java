package com.bgsoftware.superiorskyblock.core.database.sql;

import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.data.DatabaseFilter;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.Mutable;
import com.bgsoftware.superiorskyblock.core.database.sql.session.QueryResult;
import com.bgsoftware.superiorskyblock.core.logging.Log;

import java.sql.ResultSet;
import java.util.Map;
import java.util.function.Consumer;

public class SQLDatabaseBridge implements DatabaseBridge {

    private DatabaseBridgeMode databaseBridgeMode = DatabaseBridgeMode.IDLE;
    private StatementHolder batchStatementHolder;

    public SQLDatabaseBridge() {

    }

    private static String getColumnFilter(DatabaseFilter filter) {
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

    @Override
    public void loadAllObjects(String table, Consumer<Map<String, Object>> resultConsumer) {
        SQLHelper.select(table, "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
            while (resultSet.next()) {
                try {
                    resultConsumer.accept(new ResultSetMapBridge(resultSet));
                } catch (Exception error) {
                    Log.entering("ENTER", table);
                    Log.error(error, "An unexpected error occurred while loading data from database:");
                }
            }
        }).onFail(QueryResult.PRINT_ERROR));
    }

    @Override
    public void batchOperations(boolean batchOperations) {
        if (batchOperations) {
            batchStatementHolder = new StatementHolder("");
        } else if (batchStatementHolder != null) {
            batchStatementHolder.executeBatch(true);
            batchStatementHolder = null;
        }
    }

    @Override
    public void updateObject(String table, DatabaseFilter filter, Pair<String, Object>[] columns) {
        if (databaseBridgeMode != DatabaseBridgeMode.SAVE_DATA)
            return;

        StringBuilder columnsBuilder = new StringBuilder();

        for (Pair<String, Object> column : columns) {
            if (columnsBuilder.length() != 0)
                columnsBuilder.append(",");
            columnsBuilder.append(column.getKey()).append("=?");
        }

        String columnFilter = getColumnFilter(filter);

        String query = String.format("UPDATE {prefix}%s SET %s%s;", table, columnsBuilder, columnFilter);
        StatementHolder statementHolder = buildStatementHolder(query);

        for (Pair<String, Object> column : columns) {
            statementHolder.setObject(column.getValue());
        }

        if (filter != null) {
            filter.forEach((column, value) -> statementHolder.setObject(value + ""));
        }

        executeStatementHolder(statementHolder);
    }

    @Override
    public void insertObject(String table, Pair<String, Object>... columns) {
        if (databaseBridgeMode != DatabaseBridgeMode.SAVE_DATA)
            return;

        StringBuilder columnsBuilder = new StringBuilder();
        StringBuilder valuesBuilder = new StringBuilder();

        for (Pair<String, Object> column : columns) {
            if (columnsBuilder.length() != 0)
                columnsBuilder.append(",");
            if (valuesBuilder.length() != 0)
                valuesBuilder.append(",");
            columnsBuilder.append("`").append(column.getKey()).append("`");
            valuesBuilder.append("?");
        }

        String query = String.format("REPLACE INTO {prefix}%s (%s) VALUES(%s);", table, columnsBuilder, valuesBuilder);
        StatementHolder statementHolder = buildStatementHolder(query);

        for (Pair<String, Object> column : columns) {
            statementHolder.setObject(column.getValue());
        }

        executeStatementHolder(statementHolder);
    }

    @Override
    public void deleteObject(String table, DatabaseFilter filter) {
        if (databaseBridgeMode != DatabaseBridgeMode.SAVE_DATA)
            return;

        String columnFilter = getColumnFilter(filter);
        String query = String.format("DELETE FROM {prefix}%s%s;", table, columnFilter);
        StatementHolder statementHolder = buildStatementHolder(query);

        if (filter != null) {
            filter.forEach((column, value) -> statementHolder.setObject(value + ""));
        }

        executeStatementHolder(statementHolder);
    }

    @Override
    public void loadObject(String table, DatabaseFilter filter, Consumer<Map<String, Object>> resultConsumer) {
        Mutable<String> columnFilter = new Mutable<>(getColumnFilter(filter));

        filter.forEach((column, value) -> {
            columnFilter.setValue(columnFilter.getValue().replaceFirst("\\?", value instanceof String ?
                    String.format("'%s'", value) : value.toString()));
        });

        SQLHelper.select(table, columnFilter.getValue(), new QueryResult<ResultSet>().onSuccess(resultSet -> {
            while (resultSet.next()) {
                try {
                    resultConsumer.accept(new ResultSetMapBridge(resultSet));
                } catch (Exception error) {
                    Log.entering("ENTER", table, columnFilter);
                    Log.error(error, "An unexpected error occurred while loading data from database:");
                }
            }
        }).onFail(QueryResult.PRINT_ERROR));
    }

    @Override
    public void setDatabaseBridgeMode(DatabaseBridgeMode databaseBridgeMode) {
        this.databaseBridgeMode = databaseBridgeMode;
    }

    @Override
    public DatabaseBridgeMode getDatabaseBridgeMode() {
        return this.databaseBridgeMode;
    }

    private StatementHolder buildStatementHolder(String query) {
        if (batchStatementHolder == null) {
            return new StatementHolder(query);
        } else {
            batchStatementHolder.setQuery(query);
            return batchStatementHolder;
        }
    }

    private void executeStatementHolder(StatementHolder statementHolder) {
        if (batchStatementHolder == statementHolder) {
            statementHolder.addBatch();
        } else {
            statementHolder.execute(true);
        }
    }

}
