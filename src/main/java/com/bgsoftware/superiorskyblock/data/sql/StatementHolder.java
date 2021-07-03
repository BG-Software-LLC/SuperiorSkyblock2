package com.bgsoftware.superiorskyblock.data.sql;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StatementHolder {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final List<Map<Integer, Object>> batches = new ArrayList<>();

    private final String query;
    private final Map<Integer, Object> values = new HashMap<>();
    private int currentIndex = 1;

    private boolean isBatch = false;

    public StatementHolder(String statement){
        String prefix = plugin.getSettings().databaseType.equalsIgnoreCase("MySQL") ? plugin.getSettings().databaseMySQLPrefix : "";
        this.query = statement.replace("{prefix}", prefix);
    }

    public StatementHolder setObject(Object value){
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
