package com.bgsoftware.superiorskyblock.database;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.threads.SuperiorThread;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatementHolder {

    private PreparedStatement statement;
    private int currentIndex = 1;

    StatementHolder(Query query){
        try {
            statement = query.getStatement(SuperiorSkyblockPlugin.getPlugin().getDataHandler().getConnection());
        }catch(SQLException ex){
            ex.printStackTrace();
            statement = null;
        }
    }

    public StatementHolder setString(String value){
        if(statement != null) {
            try {
                statement.setString(currentIndex++, value);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return this;
    }

    public StatementHolder setInt(int value){
        if(statement != null) {
            try {
                statement.setInt(currentIndex++, value);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return this;
    }

    public StatementHolder setShort(short value){
        if(statement != null) {
            try {
                statement.setInt(currentIndex++, value);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return this;
    }

    public StatementHolder setLong(long value){
        if(statement != null) {
            try {
                statement.setLong(currentIndex++, value);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return this;
    }

    public StatementHolder setFloat(float value){
        if(statement != null) {
            try {
                statement.setFloat(currentIndex++, value);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return this;
    }

    public StatementHolder setDouble(double value){
        if(statement != null) {
            try {
                statement.setDouble(currentIndex++, value);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return this;
    }

    public StatementHolder setBoolean(boolean value){
        if(statement != null) {
            try {
                statement.setBoolean(currentIndex++, value);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return this;
    }

    public void execute() {
        if(statement != null) {
            new SuperiorThread(() -> {
                try {
                    statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

}
