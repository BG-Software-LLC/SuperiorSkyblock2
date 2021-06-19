package com.bgsoftware.superiorskyblock.data;

import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.objects.Pair;

import java.util.Map;
import java.util.function.Consumer;

public final class EmptyDatabaseBridge implements DatabaseBridge {

    private static final EmptyDatabaseBridge instance = new EmptyDatabaseBridge();

    public static EmptyDatabaseBridge getInstance() {
        return instance;
    }

    private EmptyDatabaseBridge(){

    }

    @Override
    public void loadAllObjects(String table, Consumer<Map<String, Object>> resultConsumer) {

    }

    @Override
    public void startSavingData() {

    }

    @Override
    public void updateObject(String table, Pair<String, Object>[] columns) {

    }

    @Override
    public void insertObject(String table, Pair<String, Object>... columns) {

    }

    @Override
    public void deleteObject(String table) {

    }

}
