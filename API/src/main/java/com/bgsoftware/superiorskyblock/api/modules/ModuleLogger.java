package com.bgsoftware.superiorskyblock.api.modules;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;

import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Simple implementation of a custom logger for modules.
 */
public class ModuleLogger extends Logger {

    private boolean debugMode = false;

    /**
     * Constructor for the logger.
     *
     * @param pluginModule The module that uses the logger.
     */
    public ModuleLogger(PluginModule pluginModule) {
        super("SuperiorSkyblock2-" + pluginModule.getName(), null);
        this.setParent(SuperiorSkyblockAPI.getSuperiorSkyblock().getLogger());
        this.setLevel(Level.ALL);
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public void debug(String message) {
        if (this.debugMode) {
            this.info(message);
        } else {
            LogRecord logRecord = new LogRecord(Level.INFO, message);
            for (Handler handler : getHandlers()) {
                if(handler instanceof FileHandler) {
                    handler.publish(logRecord);
                }
            }
        }
    }

}
