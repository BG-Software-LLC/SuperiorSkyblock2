package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.config.SettingsContainerHolder;

public class DatabaseSection extends SettingsContainerHolder implements SettingsManager.Database {

    @Override
    public String getType() {
        return getContainer().databaseType;
    }

    @Override
    public boolean isBackup() {
        return getContainer().databaseBackup;
    }

    @Override
    public String getAddress() {
        return getContainer().databaseMySQLAddress;
    }

    @Override
    public int getPort() {
        return getContainer().databaseMySQLPort;
    }

    @Override
    public String getDBName() {
        return getContainer().databaseMySQLDBName;
    }

    @Override
    public String getUsername() {
        return getContainer().databaseMySQLUsername;
    }

    @Override
    public String getPassword() {
        return getContainer().databaseMySQLPassword;
    }

    @Override
    public String getPrefix() {
        return getContainer().databaseMySQLPrefix;
    }

    @Override
    public boolean hasSSL() {
        return getContainer().databaseMySQLSSL;
    }

    @Override
    public boolean hasPublicKeyRetrieval() {
        return getContainer().databaseMySQLPublicKeyRetrieval;
    }

    @Override
    public long getWaitTimeout() {
        return getContainer().databaseMySQLWaitTimeout;
    }

    @Override
    public long getMaxLifetime() {
        return getContainer().databaseMySQLMaxLifetime;
    }
}
