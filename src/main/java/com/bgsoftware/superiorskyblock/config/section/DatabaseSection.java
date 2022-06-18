package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.config.SettingsContainer;

public class DatabaseSection implements SettingsManager.Database {

    private final SettingsContainer container;

    public DatabaseSection(SettingsContainer container) {
        this.container = container;
    }

    @Override
    public String getType() {
        return this.container.databaseType;
    }

    @Override
    public String getAddress() {
        return this.container.databaseMySQLAddress;
    }

    @Override
    public int getPort() {
        return this.container.databaseMySQLPort;
    }

    @Override
    public String getDBName() {
        return this.container.databaseMySQLDBName;
    }

    @Override
    public String getUsername() {
        return this.container.databaseMySQLUsername;
    }

    @Override
    public String getPassword() {
        return this.container.databaseMySQLPassword;
    }

    @Override
    public String getPrefix() {
        return this.container.databaseMySQLPrefix;
    }

    @Override
    public boolean hasSSL() {
        return this.container.databaseMySQLSSL;
    }

    @Override
    public boolean hasPublicKeyRetrieval() {
        return this.container.databaseMySQLPublicKeyRetrieval;
    }

    @Override
    public long getWaitTimeout() {
        return this.container.databaseMySQLWaitTimeout;
    }

    @Override
    public long getMaxLifetime() {
        return this.container.databaseMySQLMaxLifetime;
    }
}
