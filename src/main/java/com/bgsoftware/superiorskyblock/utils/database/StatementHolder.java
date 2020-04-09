package com.bgsoftware.superiorskyblock.utils.database;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StatementHolder {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final List<Registry<Integer, Object>> batches = new ArrayList<>();

    private final String query;
    private final Map<Integer, Object> values = new HashMap<>();
    private int currentIndex = 1;

    private boolean isBatch = false;

    StatementHolder(Query query){
        String prefix = plugin.getSettings().databaseType.equalsIgnoreCase("MySQL") ? plugin.getSettings().databaseMySQLPrefix : "";
        this.query = query.getStatement().replace("{prefix}", prefix);
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
        if(batches.isEmpty())
            SQLHelper.setAutoCommit(false);
        batches.add(Registry.createRegistry(new HashMap<>(values)));
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
            SQLHelper.waitForLock();

            StringHolder errorQuery = new StringHolder(query);

            SQLHelper.buildStatement(query, preparedStatement -> {
                if (isBatch) {
                    if(batches.isEmpty()){
                        isBatch = false;
                        return;
                    }

                    for (Registry<Integer, Object> values : batches) {
                        for (Map.Entry<Integer, Object> entry : values.entries()) {
                            preparedStatement.setObject(entry.getKey(), entry.getValue());
                            errorQuery.value = errorQuery.value.replaceFirst("\\?", entry.getValue() + "");
                        }
                        preparedStatement.addBatch();
                        values.delete();
                    }
                    preparedStatement.executeBatch();
                    SQLHelper.commit();
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
        } finally {
            SQLHelper.releaseLock();
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
