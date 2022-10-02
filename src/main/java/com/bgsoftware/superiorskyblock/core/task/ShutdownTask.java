package com.bgsoftware.superiorskyblock.core.task;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.logging.Log;

public class ShutdownTask extends Thread {

    private final SuperiorSkyblockPlugin plugin;

    public ShutdownTask(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getGrid().wasPluginDisabled()) {
            Log.error("Detected crash. SuperiorSkyblock will attempt to save data...");
            plugin.onDisable();
        }
    }

}
