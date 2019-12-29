package com.bgsoftware.superiorskyblock.utils.database;

public abstract class DatabaseObject {

    public abstract void executeUpdateStatement(boolean async);

    public abstract void executeInsertStatement(boolean async);

    public abstract void executeDeleteStatement(boolean async);

}
