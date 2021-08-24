package com.bgsoftware.superiorskyblock.data.sql;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class SQLSession {

    private final CompletableFuture<Void> ready = new CompletableFuture<>();
    private final Object mutex = new Object();

    private SuperiorSkyblockPlugin plugin;
    private HikariDataSource dataSource;
    private boolean logging;
    private boolean usesMySQL;

    private void log(String message){
        if(logging)
            SuperiorSkyblockPlugin.log(message);
    }

    public boolean createConnection(SuperiorSkyblockPlugin plugin, boolean logging){
        this.plugin = plugin;
        this.logging = logging;
        this.usesMySQL = plugin.getSettings().databaseType.equalsIgnoreCase("MySQL");

        try {
            log("Trying to connect to " + plugin.getSettings().databaseType + " database...");

            HikariConfig config = new HikariConfig();
            config.setConnectionTestQuery("SELECT 1");
            config.setPoolName("SuperiorSkyblock Pool");

            if (usesMySQL) {
                config.setDriverClassName("com.mysql.jdbc.Driver");

                String address = plugin.getSettings().databaseMySQLAddress;
                String dbName = plugin.getSettings().databaseMySQLDBName;
                String userName = plugin.getSettings().databaseMySQLUsername;
                String password = plugin.getSettings().databaseMySQLPassword;
                int port = plugin.getSettings().databaseMySQLPort;

                boolean useSSL = plugin.getSettings().databaseMySQLSSL;
                boolean publicKeyRetrieval = plugin.getSettings().databaseMySQLPublicKeyRetrieval;

                config.setJdbcUrl("jdbc:mysql://" + address + ":" + port + "/" + dbName + "?useSSL=" + useSSL);
                config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s?useSSL=%b&allowPublicKeyRetrieval=%b",
                        address, port, dbName, useSSL, publicKeyRetrieval));
                config.setUsername(userName);
                config.setPassword(password);
                config.setMinimumIdle(5);
                config.setMaximumPoolSize(50);
                config.setConnectionTimeout(10000);
                config.setIdleTimeout(600000);
                config.setMaxLifetime(1800000);
                config.addDataSourceProperty("characterEncoding","utf8");
                config.addDataSourceProperty("useUnicode","true");

                dataSource = new HikariDataSource(config);

                log("Successfully established connection with MySQL database!");
            } else {
                config.setDriverClassName("org.sqlite.JDBC");
                File file = new File(plugin.getDataFolder(), "database.db");
                config.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath().replace("\\", "/"));

                dataSource = new HikariDataSourceSQLiteWrapper(config);

                log("Successfully established connection with SQLite database!");
            }

            ready.complete(null);

            return true;
        }catch(Exception ignored){}

        return false;
    }

    public boolean isUsingMySQL(){
        return usesMySQL;
    }

    public void waitForConnection(){
        try {
            ready.get();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public Object getMutex() {
        return mutex;
    }

    public void executeUpdate(String statement){
        executeUpdate(statement, error -> {
            System.out.println(statement);
            error.printStackTrace();
        });
    }

    public void executeUpdate(String statement, Consumer<SQLException> onFailure){
        String prefix = usesMySQL ? plugin.getSettings().databaseMySQLPrefix : "";
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try{
            conn = dataSource.getConnection();
            preparedStatement = conn.prepareStatement(statement.replace("{prefix}", prefix)
                    .replace("BIG_DECIMAL", "TEXT").replace("UUID", "VARCHAR(36)"));
            preparedStatement.executeUpdate();
        }catch(SQLException ex){
            onFailure.accept(ex);
        } finally {
            close(preparedStatement);
            close(conn);
        }
    }

    public boolean doesConditionExist(String statement){
        AtomicBoolean result = new AtomicBoolean(false);
        executeQuery(statement, resultSet -> result.set(resultSet.next()));
        return result.get();
    }

    public boolean doesTableExist(String tableName) {
        AtomicBoolean result = new AtomicBoolean(false);

        executeQuery("SELECT * FROM {prefix}" + tableName, resultSet ->
                result.set(true), error -> result.set(false));

        return result.get();
    }

    public void executeQuery(String statement, QueryConsumer<ResultSet> callback){
        executeQuery(statement, callback, SQLException::printStackTrace);
    }

    public void executeQuery(String statement, QueryConsumer<ResultSet> callback, Consumer<SQLException> onFailure){
        String prefix = usesMySQL ? plugin.getSettings().databaseMySQLPrefix : "";
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try{
            conn = dataSource.getConnection();
            preparedStatement = conn.prepareStatement(statement.replace("{prefix}", prefix));
            resultSet = preparedStatement.executeQuery();
            callback.accept(resultSet);
        }catch(SQLException ex){
            onFailure.accept(ex);
        } finally {
            close(resultSet);
            close(preparedStatement);
            close(conn);
        }
    }

    public void close(){
        dataSource.close();
    }

    public void buildStatement(String query, QueryConsumer<PreparedStatement> consumer, Consumer<SQLException> failure){
        String prefix = usesMySQL ? plugin.getSettings().databaseMySQLPrefix : "";
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try{
            conn = dataSource.getConnection();
            preparedStatement = conn.prepareStatement(query.replace("{prefix}", prefix));
            consumer.accept(preparedStatement);
        }catch(SQLException ex){
            failure.accept(ex);
        } finally {
            close(preparedStatement);
            close(conn);
        }
    }

    private void close(AutoCloseable closeable){
        if(closeable != null){
            try {
                if(!(closeable instanceof Connection) || usesMySQL)
                    closeable.close();
            } catch (Exception ignored) {}
        }
    }

    public void setAutoCommit(boolean autoCommit){
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(autoCommit);
        }catch(SQLException ex){
            ex.printStackTrace();
        } finally {
            close(conn);
        }
    }

    public void commit() throws SQLException{
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.commit();
        } finally {
            close(conn);
        }
    }

    public interface QueryConsumer<T>{

        void accept(T value) throws SQLException;

    }

    private static class HikariDataSourceSQLiteWrapper extends HikariDataSource {

        private final Connection conn;

        HikariDataSourceSQLiteWrapper(HikariConfig config) throws SQLException{
            conn = DriverManager.getConnection(config.getJdbcUrl());
        }

        @Override
        public Connection getConnection(){
            return conn;
        }

        @Override
        public void close() {
            try {
                conn.close();
            }catch(SQLException ignored){}
        }
    }

}
