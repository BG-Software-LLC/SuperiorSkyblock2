package com.bgsoftware.superiorskyblock.core.database.sql.transaction;

import com.bgsoftware.common.annotations.Nullable;

import java.util.List;

public class DeleteSQLDatabaseTransaction extends SQLDatabaseTransaction<DeleteSQLDatabaseTransaction> {

    protected final String tableName;
    @Nullable
    private final List<String> filteredColumns;

    public DeleteSQLDatabaseTransaction(String tableName, @Nullable List<String> filteredColumns) {
        this.tableName = tableName;
        this.filteredColumns = filteredColumns;
    }

    @Override
    public String buildQuery() {
        String columnFilter = getColumnsFilter(filteredColumns);
        return String.format("DELETE FROM {prefix}%s%s;", tableName, columnFilter);
    }

}
