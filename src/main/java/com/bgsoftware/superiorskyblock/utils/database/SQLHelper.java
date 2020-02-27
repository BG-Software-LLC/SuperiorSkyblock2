package com.bgsoftware.superiorskyblock.utils.database;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

public final class SQLHelper {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static HikariDataSource dataSource;

    private SQLHelper(){}

    public static boolean createConnection(SuperiorSkyblockPlugin plugin){
        try {
            SuperiorSkyblockPlugin.log("Trying to connect to " + plugin.getSettings().databaseType + " database...");
            dataSource = new HikariDataSource();
            dataSource.setConnectionTestQuery("SELECT 1");

            if (plugin.getSettings().databaseType.equalsIgnoreCase("MySQL")) {
                dataSource.setDriverClassName("com.mysql.jdbc.Driver");

                String address = plugin.getSettings().databaseMySQLAddress;
                String dbName = plugin.getSettings().databaseMySQLDBName;
                String userName = plugin.getSettings().databaseMySQLUsername;
                String password = plugin.getSettings().databaseMySQLPassword;
                int port = plugin.getSettings().databaseMySQLPort;

                dataSource.setJdbcUrl("jdbc:mysql://" + address + ":" + port + "/" + dbName);
                dataSource.setUsername(userName);
                dataSource.setPassword(password);

                SuperiorSkyblockPlugin.log("Successfully established connection with MySQL database!");
            } else {
                dataSource.setDriverClassName("org.sqlite.JDBC");
                File file = new File(plugin.getDataFolder(), "database.db");
                dataSource.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath().replace("\\", "/"));
                SuperiorSkyblockPlugin.log("Successfully established connection with SQLite database!");
            }

            return true;
        }catch(Exception ignored){}

        return false;
    }

    public static void executeUpdate(String statement){
        String prefix = plugin.getSettings().databaseType.equalsIgnoreCase("MySQL") ? plugin.getSettings().databaseMySQLPrefix : "";
        try(Connection conn = dataSource.getConnection(); PreparedStatement preparedStatement = conn.prepareStatement(statement.replace("{prefix}", prefix))){
            preparedStatement.executeUpdate();
        }catch(SQLException ex){
            System.out.println(statement);
            ex.printStackTrace();
        }
    }

    public static boolean doesConditionExist(String statement){
        boolean ret = false;

        String prefix = plugin.getSettings().databaseType.equalsIgnoreCase("MySQL") ? plugin.getSettings().databaseMySQLPrefix : "";
        try(Connection conn = dataSource.getConnection(); PreparedStatement preparedStatement = conn.prepareStatement(statement.replace("{prefix}", prefix)); ResultSet resultSet = preparedStatement.executeQuery()){
            ret = resultSet.next();
        }catch(SQLException ex){
            ex.printStackTrace();
        }

        return ret;
    }

    public static void executeQuery(String statement, QueryConsumer<ResultSet> callback){
        String prefix = plugin.getSettings().databaseType.equalsIgnoreCase("MySQL") ? plugin.getSettings().databaseMySQLPrefix : "";
        try(Connection conn = dataSource.getConnection(); PreparedStatement preparedStatement = conn.prepareStatement(statement.replace("{prefix}", prefix)); ResultSet resultSet = preparedStatement.executeQuery()){
            callback.accept(resultSet);
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    public static void close(){
        dataSource.close();
    }

    public static void buildStatement(String query, QueryConsumer<PreparedStatement> consumer, Consumer<SQLException> failure){
        String prefix = plugin.getSettings().databaseType.equalsIgnoreCase("MySQL") ? plugin.getSettings().databaseMySQLPrefix : "";
        try(Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(query.replace("{prefix}", prefix))){
            consumer.accept(ps);
        }catch(SQLException ex){
            failure.accept(ex);
        }
    }

    public interface QueryConsumer<T>{

        void accept(T value) throws SQLException;

    }

}

