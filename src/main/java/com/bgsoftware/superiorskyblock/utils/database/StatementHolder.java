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
    private final Registry<Integer, Object> values = Registry.createRegistry();
    private int currentIndex = 1;

    StatementHolder(Query query){
        String prefix = plugin.getSettings().databaseType.equalsIgnoreCase("MySQL") ? plugin.getSettings().databaseMySQLPrefix : "";
        this.query = query.getStatement().replace("{prefix}", prefix);
    }

    public StatementHolder setString(String value){
        values.add(currentIndex++, value);
        return this;
    }

    public StatementHolder setInt(int value){
        values.add(currentIndex++, value);
        return this;
    }

    public StatementHolder setShort(short value){
        values.add(currentIndex++, value);
        return this;
    }

    public StatementHolder setLong(long value){
        values.add(currentIndex++, value);
        return this;
    }

    public StatementHolder setFloat(float value){
        values.add(currentIndex++, value);
        return this;
    }

    public StatementHolder setDouble(double value){
        values.add(currentIndex++, value);
        return this;
    }

    public StatementHolder setBoolean(boolean value){
        values.add(currentIndex++, value);
        return this;
    }

    public void addBatch(){
        if(batches.isEmpty())
            SQLHelper.setAutoCommit(false);
        batches.add(Registry.createRegistry(values));
        values.clear();
        currentIndex = 1;
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
                if (!batches.isEmpty()) {
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
                    for (Map.Entry<Integer, Object> entry : values.entries()) {
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
