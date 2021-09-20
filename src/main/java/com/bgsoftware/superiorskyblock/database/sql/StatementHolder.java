package com.bgsoftware.superiorskyblock.database.sql;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StatementHolder {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final String prefix = plugin.getSettings().getDatabase().getType().equalsIgnoreCase("MySQL") ?
            plugin.getSettings().getDatabase().getPrefix() : "";

    private final List<Map<Integer, Object>> batches = new ArrayList<>();

    private final Map<Integer, Object> values = new HashMap<>();
    private String query;
    private int currentIndex = 1;

    public StatementHolder(String statement){
        setQuery(statement);
    }

    public void setQuery(String query) {
        this.query = query.replace("{prefix}", prefix);
    }

    public void addBatch(){
        batches.add(new HashMap<>(values));
        values.clear();
        currentIndex = 1;
    }

    public StatementHolder setObject(Object value){
        values.put(currentIndex++, value);
        return this;
    }

    public void executeBatch(boolean async){
        if(query == null || query.isEmpty() || batches.isEmpty())
            return;

        if(async && !Executor.isDataThread()){
            Executor.data(() -> executeBatch(false));
            return;
        }

        SQLHelper.waitForConnection();

        try {
            StringHolder errorQuery = new StringHolder(query);

            synchronized (SQLHelper.getMutex()) {
                SuperiorSkyblockPlugin.debug("Action: Database Execute, Query: " + query);
                SQLHelper.buildStatement(query, preparedStatement -> {
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
                }, ex -> {
                    SuperiorSkyblockPlugin.log("&cFailed to execute query " + errorQuery);
                    ex.printStackTrace();
                });
            }
        } finally {
            values.clear();
        }
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
                SQLHelper.buildStatement(query, preparedStatement -> {
                    for (Map.Entry<Integer, Object> entry : values.entrySet()) {
                        preparedStatement.setObject(entry.getKey(), entry.getValue());
                        errorQuery.value = errorQuery.value.replaceFirst("\\?", entry.getValue() + "");
                    }
                    preparedStatement.executeUpdate();
                }, ex -> {
                    SuperiorSkyblockPlugin.log("&cFailed to execute query " + errorQuery);
                    ex.printStackTrace();
                });
            }
        } finally {
            values.clear();
        }
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

}
