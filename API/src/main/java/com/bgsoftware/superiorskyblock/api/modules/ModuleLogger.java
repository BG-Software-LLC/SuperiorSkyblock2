package com.bgsoftware.superiorskyblock.api.modules;

import org.bukkit.Bukkit;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Simple implementation of a custom logger for modules.
 */
public class ModuleLogger extends Logger {

    private final String loggerPrefix;

    /**
     * Constructor for the logger.
     *
     * @param pluginModule The module that uses the logger.
     */
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
