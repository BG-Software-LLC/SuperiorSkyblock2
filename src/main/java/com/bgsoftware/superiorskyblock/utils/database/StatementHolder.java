package com.bgsoftware.superiorskyblock.utils.database;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StatementHolder {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final EnumMap<Query, IncreasableInteger> queryCalls = new EnumMap<>(Query.class);

    private final List<Map<Integer, Object>> batches = new ArrayList<>();

    private final String query;
    private final DatabaseObject databaseObject;
    private final Query queryEnum;
    private final Map<Integer, Object> values = new HashMap<>();
    private int currentIndex = 1;

    private boolean isBatch = false;

    StatementHolder(DatabaseObject databaseObject, Query query){
        String prefix = plugin.getSettings().databaseType.equalsIgnoreCase("MySQL") ? plugin.getSettings().databaseMySQLPrefix : "";
        this.queryEnum = query;
        this.query = query.getStatement().replace("{prefix}", prefix);
        this.databaseObject = databaseObject == null ? DatabaseObject.NULL_DATA : databaseObject;
        this.databaseObject.setModified(query);
    }

    public StatementHolder setString(String value){
        values.put(currentIndex++, value);
        return this;
    }

    public StatementHolder setInt(int value){
        values.put(currentIndex++, value);
        return this;
    }

    public StatementHolder setShort(short value){
        values.put(currentIndex++, value);
        return this;
    }

    public StatementHolder setLong(long value){
        values.put(currentIndex++, value);
        return this;
    }

    public StatementHolder setFloat(float value){
        values.put(currentIndex++, value);
        return this;
    }

    public StatementHolder setDouble(double value){
        values.put(currentIndex++, value);
        return this;
    }

    public StatementHolder setBoolean(boolean value){
        values.put(currentIndex++, value);
        return this;
    }

    public void addBatch(){
        batches.add(new HashMap<>(values));
        values.clear();
        currentIndex = 1;
    }

    public void prepareBatch(){
        isBatch = true;
    }

    public void execute(boolean async) {
        if(async && !Executor.isDataThread()){
            Executor.data(() -> execute(false));
            return;
        }

        SQLHelper.waitForConnection();

        try {
            StringHolder errorQuery = new StringHolder(query);

            synchronized (SQLHelper.getMutex()) {
                SuperiorSkyblockPlugin.debug("Action: Database Execute, Query: " + query);
                queryCalls.computeIfAbsent(queryEnum, q -> new IncreasableInteger()).increase();
                SQLHelper.buildStatement(query, preparedStatement -> {
                    if (isBatch) {
                        if (batches.isEmpty()) {
                            isBatch = false;
                            return;
                        }

                        SQLHelper.setAutoCommit(false);

                        for (Map<Integer, Object> values : batches) {
                            for (Map.Entry<Integer, Object> entry : values.entrySet()) {
                                preparedStatement.setObject(entry.getKey(), entry.getValue());
                                errorQuery.value = errorQuery.value.replaceFirst("\\?", entry.getValue() + "");
                            }
                            preparedStatement.addBatch();
                        }

                        preparedStatement.executeBatch();
                        try {
                            SQLHelper.commit();
                        }catch(Throwable ignored){}

                        SQLHelper.setAutoCommit(true);
                    } else {
                        for (Map.Entry<Integer, Object> entry : values.entrySet()) {
                            preparedStatement.setObject(entry.getKey(), entry.getValue());
                            errorQuery.value = errorQuery.value.replaceFirst("\\?", entry.getValue() + "");
                        }
                        preparedStatement.executeUpdate();
                    }

                    databaseObject.setUpdated(queryEnum);
                }, ex -> {
                    SuperiorSkyblockPlugin.log("&cFailed to execute query " + errorQuery);
                    ex.printStackTrace();

                    databaseObject.setUpdated(queryEnum);
                });
            }
        } finally {
            values.clear();
            databaseObject.setUpdated(queryEnum);
        }
    }

    public static EnumMap<Query, IncreasableInteger> getQueryCalls() {
        return queryCalls;
    }

    private static class StringHolder{

        private String value;

        StringHolder(String value){
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static final class IncreasableInteger{

        private int value = 0;

        IncreasableInteger(){

        }

        public int get() {
            return value;
        }

        public void increase(){
            value++;
        }

    }

}
