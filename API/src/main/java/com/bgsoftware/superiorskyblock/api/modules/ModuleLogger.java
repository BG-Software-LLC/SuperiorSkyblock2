package com.bgsoftware.superiorskyblock.api.modules;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;

import java.io.PrintWriter;
import java.io.StringWriter;
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

    public void i(String message) {
        super.info(message);
    }

    public void w(String message) {
        super.warning(message);
    }

    public void e(String message) {
        super.severe(message);
    }

    public void e(String message, Throwable error) {
        super.severe(message);
        logError(error);
    }

    public void d(String message) {
        if (this.debugMode) {
            this.info(message);
        } else {
            LogRecord logRecord = new LogRecord(Level.INFO, message);
            for (Handler handler : getHandlers()) {
                if (handler instanceof FileHandler) {
                    handler.publish(logRecord);
                }
            }
        }
    }

    private void logError(Throwable error) {
        StringWriter buffer = new StringWriter();
        PrintWriter pw = new PrintWriter(buffer);
        error.printStackTrace(pw);
        super.severe(buffer.toString());
    }

}
