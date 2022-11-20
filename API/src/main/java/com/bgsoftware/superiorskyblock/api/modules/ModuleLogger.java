package com.bgsoftware.superiorskyblock.api.modules;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple implementation of a custom logger for modules.
 */
public class ModuleLogger extends Logger {

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

}
