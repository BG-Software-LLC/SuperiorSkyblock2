package com.bgsoftware.superiorskyblock.core.database.sql.transaction;

import java.util.List;

public class InsertSQLDatabaseTransaction extends SQLDatabaseTransaction<InsertSQLDatabaseTransaction> {

    protected final String tableName;
    private final List<String> columnNames;

    public InsertSQLDatabaseTransaction(String tableName, List<String> columnNames) {
        this.tableName = tableName;
        this.columnNames = columnNames;
    }

    @Override
    public String buildQuery() {
        StringBuilder columnsBuilder = new StringBuilder();
        StringBuilder valuesBuilder = new StringBuilder();

        for (String columnName : this.columnNames) {
            if (columnsBuilder.length() != 0)
                columnsBuilder.append(",");
            if (valuesBuilder.length() != 0)
                valuesBuilder.append(",");
            columnsBuilder.append("`").append(columnName).append("`");
            valuesBuilder.append("?");
        }

        return String.format("REPLACE INTO {prefix}%s (%s) VALUES(%s);", this.tableName, columnsBuilder, valuesBuilder);
    }

}
