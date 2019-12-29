package com.bgsoftware.superiorskyblock.utils.database;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class SQLHelper {

    private static Connection conn;

    private SQLHelper(){}

    public static boolean createConnection(SuperiorSkyblockPlugin plugin){
        try {
            SuperiorSkyblockPlugin.log("Trying to connect to " + plugin.getSettings().databaseType + " database...");
            if (plugin.getSettings().databaseType.equalsIgnoreCase("MySQL")) {
                Class.forName("com.mysql.jdbc.Driver");

                String address = plugin.getSettings().databaseMySQLAddress,
                        dbName = plugin.getSettings().databaseMySQLDBName,
                        userName = plugin.getSettings().databaseMySQLUsername,
                        password = plugin.getSettings().databaseMySQLPassword;
                int port = plugin.getSettings().databaseMySQLPort;

                String sqlURL = "jdbc:mysql://" + address + ":" + port + "/" + dbName;
                conn = DriverManager.getConnection(sqlURL, userName, password);
                SuperiorSkyblockPlugin.log("Successfully established connection with MySQL database!");
            } else {
                Class.forName("org.sqlite.JDBC");
                File file = new File(plugin.getDataFolder(), "database.db");
                String sqlURL = "jdbc:sqlite:" + file.getAbsolutePath().replace("\\", "/");
                conn = DriverManager.getConnection(sqlURL);
                SuperiorSkyblockPlugin.log("Successfully established connection with SQLite database!");
            }

            return true;
        }catch(Exception ignored){}

        return false;
    }

    public static void executeUpdate(String statement){
        try(PreparedStatement preparedStatement = conn.prepareStatement(statement)){
            preparedStatement.executeUpdate();
        }catch(SQLException ex){
            System.out.println(statement);
            ex.printStackTrace();
        }
    }

    public static boolean doesConditionExist(String statement){
        boolean ret = false;

        try(PreparedStatement preparedStatement = conn.prepareStatement(statement); ResultSet resultSet = preparedStatement.executeQuery()){
            ret = resultSet.next();
        }catch(SQLException ex){
            ex.printStackTrace();
        }

        return ret;
    }

    public static void executeQuery(String statement, QueryCallback callback){
        try(PreparedStatement preparedStatement = conn.prepareStatement(statement); ResultSet resultSet = preparedStatement.executeQuery()){
            callback.run(resultSet);
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    public interface QueryCallback{

        void run(ResultSet resultSet) throws SQLException;

    }

    public static void close(){
        try{
            conn.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    public static PreparedStatement buildStatement(String query) throws SQLException{
        return conn.prepareStatement(query);
    }

}

