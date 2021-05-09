package com.bgsoftware.superiorskyblock.api.modules;

import org.bukkit.Bukkit;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class ModuleLogger extends Logger {

    private final String loggerPrefix;

    public ModuleLogger(PluginModule pluginModule) {
        super(pluginModule.getClass().getCanonicalName(), null);
        this.loggerPrefix = "[" + pluginModule.getName() + "] ";
        this.setParent(Bukkit.getServer().getLogger());
        this.setLevel(Level.ALL);
    }

    public void log(LogRecord logRecord) {
        logRecord.setMessage(this.loggerPrefix + logRecord.getMessage());
        super.log(logRecord);
    }

}
