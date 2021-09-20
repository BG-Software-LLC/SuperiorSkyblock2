package com.bgsoftware.superiorskyblock.database.sql;

import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseFilter;
import com.bgsoftware.superiorskyblock.api.objects.Pair;

import java.util.Map;
import java.util.function.Consumer;

public final class SQLDatabaseBridge implements DatabaseBridge {

    private boolean shouldSaveData = false;
    private StatementHolder batchStatementHolder;

    @Override
    public void loadAllObjects(String table, Consumer<Map<String, Object>> resultConsumer) {
        SQLHelper.executeQuery("SELECT * FROM {prefix}" + table + ";", resultSet -> {
            while (resultSet.next()){
                try {
                    resultConsumer.accept(new ResultSetMapBridge(resultSet));
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public void startSavingData() {
        shouldSaveData = true;
    }

    @Override
    public void batchOperations(boolean batchOperations) {
        if(batchOperations){
            batchStatementHolder = new StatementHolder("");
        }
        else if(batchStatementHolder != null) {
            batchStatementHolder.executeBatch(true);
            batchStatementHolder = null;
        }
    }

    @Override
    public void updateObject(String table, DatabaseFilter filter, Pair<String, Object>[] columns) {
        if(!shouldSaveData)
            return;

        StringBuilder columnsBuilder = new StringBuilder();

        for(Pair<String, Object> column : columns) {
            if(columnsBuilder.length() != 0)
                columnsBuilder.append(",");
            columnsBuilder.append(column.getKey()).append("=?");
        }

        String columnFilter = getColumnFilter(filter);

        String query = String.format("UPDATE {prefix}%s SET %s%s;", table, columnsBuilder, columnFilter);
        StatementHolder statementHolder = buildStatementHolder(query);

        for(Pair<String, Object> column : columns) {
            statementHolder.setObject(column.getValue());
        }

        if(filter != null){
            for(Pair<String, Object> _columnFilter : filter.getFilters())
                statementHolder.setObject(_columnFilter.getValue() + "");
        }

        executeStatementHolder(statementHolder);
    }

    @Override
    public void insertObject(String table, Pair<String, Object>... columns) {
        if(!shouldSaveData)
            return;

        StringBuilder columnsBuilder = new StringBuilder();
        StringBuilder valuesBuilder = new StringBuilder();

        for(Pair<String, Object> column : columns) {
            if(columnsBuilder.length() != 0)
                columnsBuilder.append(",");
            if(valuesBuilder.length() != 0)
                valuesBuilder.append(",");
            columnsBuilder.append("`").append(column.getKey()).append("`");
            valuesBuilder.append("?");
        }

        String query = String.format("REPLACE INTO {prefix}%s (%s) VALUES(%s);", table, columnsBuilder, valuesBuilder);
        StatementHolder statementHolder = buildStatementHolder(query);

        for(Pair<String, Object> column : columns) {
            statementHolder.setObject(column.getValue());
        }

        executeStatementHolder(statementHolder);
    }

    @Override
    public void deleteObject(String table, DatabaseFilter filter) {
        if(!shouldSaveData)
            return;

        String columnFilter = getColumnFilter(filter);
        String query = String.format("DELETE FROM {prefix}%s%s;", table, columnFilter);
        StatementHolder statementHolder = buildStatementHolder(query);

        if(filter != null){
            for(Pair<String, Object> _columnFilter : filter.getFilters())
                statementHolder.setObject(_columnFilter.getValue() + "");
        }

        executeStatementHolder(statementHolder);
    }

    @Override
    public void loadObject(String table, DatabaseFilter filter, Consumer<Map<String, Object>> resultConsumer){
        String columnFilter = getColumnFilter(filter);

        for(Pair<String, Object> filterPair : filter.getFilters()) {
            columnFilter = columnFilter.replaceFirst("\\?", filterPair.getValue() instanceof String ?
                    String.format("'%s'", filterPair.getValue()) : filterPair.getValue().toString());
        }

        SQLHelper.executeQuery(String.format("SELECT * FROM {prefix}%s%s;", table, columnFilter), resultSet -> {
            while (resultSet.next()){
                try {
                    resultConsumer.accept(new ResultSetMapBridge(resultSet));
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
    }

    private StatementHolder buildStatementHolder(String query){
        if(batchStatementHolder == null){
            return new StatementHolder(query);
        }
        else {
            batchStatementHolder.setQuery(query);
            return batchStatementHolder;
        }
    }

    private void executeStatementHolder(StatementHolder statementHolder){
        if(batchStatementHolder == statementHolder){
            statementHolder.addBatch();
        }
        else {
            statementHolder.execute(true);
        }
    }

    private static String getColumnFilter(DatabaseFilter filter){
        StringBuilder columnIdentifier = new StringBuilder();
        if(filter != null) {
            for(Pair<String, Object> columnFilter : filter.getFilters()) {
                if(columnIdentifier.length() == 0){
                    columnIdentifier.append(String.format(" WHERE %s=?", columnFilter.getKey()));
                }
                else {
                    columnIdentifier.append(String.format(" AND %s=?", columnFilter.getKey()));
                }
            }
        }
        return columnIdentifier.toString();
    }

}
