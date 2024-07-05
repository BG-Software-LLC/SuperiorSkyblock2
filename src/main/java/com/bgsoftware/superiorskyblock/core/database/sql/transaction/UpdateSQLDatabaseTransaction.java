package com.bgsoftware.superiorskyblock.core.database.sql.transaction;

import com.bgsoftware.common.annotations.Nullable;

import java.util.List;

public class UpdateSQLDatabaseTransaction extends SQLDatabaseTransaction<UpdateSQLDatabaseTransaction> {

    private final String tableName;
    private final List<String> columnNames;
    @Nullable
    private final List<String> filteredColumns;

    public UpdateSQLDatabaseTransaction(String tableName, List<String> columnNames, @Nullable List<String> filteredColumns) {
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.filteredColumns = filteredColumns;
    }

    @Override
    public String buildQuery() {
        StringBuilder columnsBuilder = new StringBuilder();

        for (String columnName : columnNames) {
            if (columnsBuilder.length() != 0)
                columnsBuilder.append(",");
            columnsBuilder.append(columnName).append("=?");
        }

        String columnFilter = getColumnsFilter(filteredColumns);
        return String.format("UPDATE {prefix}%s SET %s%s;", tableName, columnsBuilder, columnFilter);
    }

}
