package com.bgsoftware.superiorskyblock.database.sql;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Consumer;

public final class SQLHelper {

    private static SQLSession globalSession = null;

    private SQLHelper() {

    }

    public static boolean isReady() {
        return globalSession != null;
    }

    public static void waitForConnection() {
        if(isReady())
            globalSession.waitForConnection();
    }

    public static Optional<Object> getMutex() {
        return Optional.ofNullable(isReady() ? globalSession.getMutex() : null);
    }

    public static boolean createConnection(SuperiorSkyblockPlugin plugin) {
        SQLSession session = new SQLSession(plugin, true);
        if(session.createConnection()) {
            globalSession = session;
            return true;
        }

        return false;
    }

    public static void executeUpdate(String statement) {
        if(isReady())
            globalSession.executeUpdate(statement);
    }

    public static void executeUpdate(String statement, Consumer<SQLException> onFailure) {
        if(isReady())
            globalSession.executeUpdate(statement, onFailure);
    }

    public static boolean doesConditionExist(String statement) {
        return isReady() && globalSession.doesConditionExist(statement);
    }

    public static void executeQuery(String statement, QueryConsumer<ResultSet> callback) {
        if(isReady())
            globalSession.executeQuery(statement, callback::accept);
    }

    public static void close() {
        if(isReady())
            globalSession.close();
    }

    public static void buildStatement(String query, QueryConsumer<PreparedStatement> consumer, Consumer<SQLException> failure) {
        if(isReady())
            globalSession.buildStatement(query, consumer::accept, failure);
    }

    public static void setAutoCommit(boolean autoCommit) {
        if(isReady())
            globalSession.setAutoCommit(autoCommit);
    }

    public static void commit() throws SQLException {
        if(isReady())
            globalSession.commit();
    }

    public interface QueryConsumer<T> {

        void accept(T value) throws SQLException;

    }
}

