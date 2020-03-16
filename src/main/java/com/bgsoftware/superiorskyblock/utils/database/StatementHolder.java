package com.bgsoftware.superiorskyblock.utils.database;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;

import java.util.Map;

public final class StatementHolder {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

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

    public void execute(boolean async) {
        if(async && !Executor.isDataThread()){
            Executor.data(() -> execute(false));
            return;
        }

        SQLHelper.waitForConnection();

        StringHolder errorQuery = new StringHolder(query);

        SQLHelper.buildStatement(query, preparedStatement -> {
            for(Map.Entry<Integer, Object> entry : values.entries()) {
                preparedStatement.setObject(entry.getKey(), entry.getValue());
                errorQuery.value = errorQuery.value.replaceFirst("\\?", entry.getValue() + "");
            }

            preparedStatement.executeUpdate();
        }, ex -> {
            SuperiorSkyblockPlugin.log("&cFailed to execute query " + errorQuery);
            ex.printStackTrace();
        });
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
