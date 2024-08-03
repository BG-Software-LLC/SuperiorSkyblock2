package com.bgsoftware.superiorskyblock.core.database.sql.transaction;

public class CustomSQLDatabaseTransaction extends SQLDatabaseTransaction<CustomSQLDatabaseTransaction> {

    private final String query;

    public CustomSQLDatabaseTransaction(String query) {
        this.query = query;
    }

    @Override
    public String buildQuery() {
        return this.query;
    }

}
