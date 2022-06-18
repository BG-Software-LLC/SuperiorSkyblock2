package com.bgsoftware.superiorskyblock.core.database.sql.session.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.database.sql.session.RemoteSQLSession;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class PostgreSQLSession extends RemoteSQLSession {

    public PostgreSQLSession(SuperiorSkyblockPlugin plugin, boolean logging) {
        super(plugin, logging);
    }

    @Override
    public boolean createConnection() {
        log("Trying to connect to remote database (PostgreSQL)...");

        try {
            HikariConfig config = new HikariConfig();
            config.setConnectionTestQuery("SELECT 1");
            config.setPoolName("SuperiorSkyblock Pool");

            String address = plugin.getSettings().getDatabase().getAddress();
            String dbName = plugin.getSettings().getDatabase().getDBName();
            int port = plugin.getSettings().getDatabase().getPort();
            config.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s", address, port, dbName));

            config.setUsername(plugin.getSettings().getDatabase().getUsername());
            config.setPassword(plugin.getSettings().getDatabase().getPassword());
            config.setMinimumIdle(5);
            config.setMaximumPoolSize(50);
            config.setConnectionTimeout(10000);
            config.setIdleTimeout(plugin.getSettings().getDatabase().getWaitTimeout());
            config.setMaxLifetime(plugin.getSettings().getDatabase().getMaxLifetime());
            config.addDataSourceProperty("characterEncoding", "utf8");
            config.addDataSourceProperty("useUnicode", "true");

            dataSource = new HikariDataSource(config);

            log("Successfully established connection with remote database!");

            ready.complete(null);

            return true;
        } catch (Throwable error) {
            log("&cFailed to connect to the remote database:");
            error.printStackTrace();
            PluginDebugger.debug(error);
        }

        return false;
    }

}
