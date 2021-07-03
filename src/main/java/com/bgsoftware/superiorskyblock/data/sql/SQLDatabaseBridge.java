package com.bgsoftware.superiorskyblock.data.sql;

import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.objects.Pair;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class SQLDatabaseBridge implements DatabaseBridge {

    private boolean shouldSaveData = false;
    private final Supplier<UUID> objectIdSupplier;
    private final String idFilter;

    public SQLDatabaseBridge(Supplier<UUID> objectIdSupplier, String columnIdName){
        this.objectIdSupplier = objectIdSupplier;
        this.idFilter = columnIdName == null ? "" : String.format(" WHERE %s=?", columnIdName);
    }

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
    public void updateObject(String table, Pair<String, Object>[] columns) {
        if(!shouldSaveData)
            return;

        StringBuilder columnsBuilder = new StringBuilder();

        for(Pair<String, Object> column : columns) {
            if(columnsBuilder.length() != 0)
                columnsBuilder.append(",");
            columnsBuilder.append(column.getKey()).append("=?");
        }

        StatementHolder statementHolder = new StatementHolder(
                String.format("UPDATE {prefix}%s SET %s%s;", table, columnsBuilder.toString(), idFilter)
        );

        for(Pair<String, Object> column : columns) {
            statementHolder.setObject(column.getValue());
        }

        if(objectIdSupplier != null){
            statementHolder.setObject(objectIdSupplier.get() + "");
        }

        statementHolder.execute(true);
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
            columnsBuilder.append(column.getKey());
            valuesBuilder.append("?");
        }

        StatementHolder statementHolder = new StatementHolder(
                String.format("REPLACE INTO {prefix}%s (%s) VALUES(%s);", table,
                        columnsBuilder.toString(), valuesBuilder.toString())
        );

        for(Pair<String, Object> column : columns) {
            statementHolder.setObject(column.getValue());
        }

        statementHolder.execute(true);
    }

    @Override
    public void deleteObject(String table) {
        if(!shouldSaveData)
            return;

        StatementHolder statementHolder = new StatementHolder(
                String.format("DELETE FROM {prefix}%s%s;", table, idFilter)
        );

        if(objectIdSupplier != null){
            statementHolder.setObject(objectIdSupplier.get() + "");
        }

        statementHolder.execute(true);
    }

    @Override
    public void loadObject(String table, Consumer<Map<String, Object>> resultConsumer){
        SQLHelper.executeQuery(String.format("SELECT * FROM {prefix}%s%s;", table, idFilter), resultSet -> {
            while (resultSet.next()){
                try {
                    resultConsumer.accept(new ResultSetMapBridge(resultSet));
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
    }

}
