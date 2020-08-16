package com.bgsoftware.superiorskyblock.utils.database;

import java.util.EnumMap;

public abstract class DatabaseObject {

    public static final DatabaseObject NULL_DATA = new DatabaseObject() {
        @Override
        public StatementHolder setUpdateStatement(StatementHolder statementHolder) {
            return null;
        }

        @Override
        public void executeUpdateStatement(boolean async) {

        }

        @Override
        public void executeInsertStatement(boolean async) {

        }

        @Override
        public void executeDeleteStatement(boolean async) {

        }
    };

    private final EnumMap<Query, Integer> modifiedCalls = new EnumMap<>(Query.class);

    public abstract StatementHolder setUpdateStatement(StatementHolder statementHolder);

    public abstract void executeUpdateStatement(boolean async);

    public abstract void executeInsertStatement(boolean async);

    public abstract void executeDeleteStatement(boolean async);

    public void setModified(Query query){
        modifiedCalls.put(query, modifiedCalls.getOrDefault(query, 0) + 1);
    }

    public void setFullUpdated(Query query){
        modifiedCalls.remove(query);
    }

    public void setUpdated(Query query){
        int calls = modifiedCalls.getOrDefault(query, 0) - 1;

        if(calls <= 0)
            modifiedCalls.remove(query);
        else
            modifiedCalls.put(query, calls);
    }

    public boolean isModified(){
        return !modifiedCalls.isEmpty();
    }

}
