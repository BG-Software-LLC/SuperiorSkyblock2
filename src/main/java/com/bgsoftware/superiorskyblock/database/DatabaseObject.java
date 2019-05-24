package com.bgsoftware.superiorskyblock.database;

public abstract class DatabaseObject {

    public abstract void executeUpdateStatement();

    public abstract void executeInsertStatement();

    public abstract void executeDeleteStatement();

}
