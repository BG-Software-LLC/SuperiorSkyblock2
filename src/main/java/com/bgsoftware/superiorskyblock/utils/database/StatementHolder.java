package com.bgsoftware.superiorskyblock.utils.database;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class StatementHolder {

    private final String query;
    private final Map<Integer, Object> values = new HashMap<>();
    private int currentIndex = 1;

    StatementHolder(Query query){
        this.query = query.getStatement();
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

    public void execute(boolean async) {
        if(async && !Executor.isDataThread()){
            Executor.data(() -> execute(false));
            return;
        }

        String errorQuery = query;
        try(PreparedStatement preparedStatement = SQLHelper.buildStatement(query)){
            for(Map.Entry<Integer, Object> entry : values.entrySet()) {
                preparedStatement.setObject(entry.getKey(), entry.getValue());
                errorQuery = errorQuery.replaceFirst("\\?", entry.getValue() + "");
            }

            preparedStatement.executeUpdate();
        }catch(SQLException ex){
            SuperiorSkyblockPlugin.log("&cFailed to execute query " + errorQuery);
            ex.printStackTrace();
        }
    }

}
