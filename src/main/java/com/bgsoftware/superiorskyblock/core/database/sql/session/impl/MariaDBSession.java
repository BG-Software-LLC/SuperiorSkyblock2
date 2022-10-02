package com.bgsoftware.superiorskyblock.core.database.sql.session.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.database.sql.session.QueryResult;
import com.bgsoftware.superiorskyblock.core.database.sql.session.RemoteSQLSession;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.ResultSet;

public class MariaDBSession extends RemoteSQLSession {

    public MariaDBSession(SuperiorSkyblockPlugin plugin, boolean logging) {
        super(plugin, logging);
    }

    @Override
    public boolean createConnection() {
        if (logging) Log.info("Trying to connect to remote database (MariaDB)...");

        try {
            HikariConfig config = new HikariConfig();
            config.setConnectionTestQuery("SELECT 1");
            config.setPoolName("SuperiorSkyblock Pool");

            config.setDriverClassName("com.mysql.jdbc.Driver");

            String address = plugin.getSettings().getDatabase().getAddress();
            String dbName = plugin.getSettings().getDatabase().getDBName();
            String userName = plugin.getSettings().getDatabase().getUsername();
            String password = plugin.getSettings().getDatabase().getPassword();
            int port = plugin.getSettings().getDatabase().getPort();

            boolean useSSL = plugin.getSettings().getDatabase().hasSSL();
            boolean publicKeyRetrieval = plugin.getSettings().getDatabase().hasPublicKeyRetrieval();

            config.setJdbcUrl("jdbc:mysql://" + address + ":" + port + "/" + dbName + "?useSSL=" + useSSL);
            config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s?useSSL=%b&allowPublicKeyRetrieval=%b",
                    address, port, dbName, useSSL, publicKeyRetrieval));
            config.setUsername(userName);
            config.setPassword(password);
            config.setMinimumIdle(5);
            config.setMaximumPoolSize(50);
            config.setConnectionTimeout(10000);
            config.setIdleTimeout(plugin.getSettings().getDatabase().getWaitTimeout());
            config.setMaxLifetime(plugin.getSettings().getDatabase().getMaxLifetime());
            config.addDataSourceProperty("characterEncoding", "utf8");
            config.addDataSourceProperty("useUnicode", "true");

            dataSource = new HikariDataSource(config);

            if (logging) Log.info("Successfully established connection with remote database!");

            ready.complete(null);

            return true;
        } catch (Throwable error) {
            Log.error(error, "An unexpected error occurred while connecting to the MariaDB database:");
        }

        return false;
    }

    @Override
    public void setJournalMode(String jounralMode, QueryResult<ResultSet> queryResult) {
        queryResult.fail(new UnsupportedOperationException("Cannot change journal mode in maria-db"));
    }

}
