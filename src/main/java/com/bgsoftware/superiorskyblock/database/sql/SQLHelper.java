package com.bgsoftware.superiorskyblock.database.sql;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

public final class SQLHelper {

    private static SQLSession globalSession;

    private SQLHelper(){

    }

    public static void waitForConnection(){
        globalSession.waitForConnection();
    }

    public static Object getMutex() {
        return globalSession.getMutex();
    }

    public static boolean createConnection(SuperiorSkyblockPlugin plugin){
        globalSession = new SQLSession(plugin, true);
        return globalSession.createConnection();
    }

    public static void executeUpdate(String statement){
        globalSession.executeUpdate(statement);
    }

    public static void executeUpdate(String statement, Consumer<SQLException> onFailure){
        globalSession.executeUpdate(statement, onFailure);
    }

    public static boolean doesConditionExist(String statement){
        return globalSession.doesConditionExist(statement);
    }

    public static void executeQuery(String statement, QueryConsumer<ResultSet> callback){
        globalSession.executeQuery(statement, callback::accept);
    }

    public static void close(){
        globalSession.close();
    }

    public static void buildStatement(String query, QueryConsumer<PreparedStatement> consumer, Consumer<SQLException> failure){
        globalSession.buildStatement(query, consumer::accept, failure);
    }

    public static void setAutoCommit(boolean autoCommit){
        globalSession.setAutoCommit(autoCommit);
    }

    public static void commit() throws SQLException{
        globalSession.commit();
    }

    public interface QueryConsumer<T>{

        void accept(T value) throws SQLException;

    }
}

