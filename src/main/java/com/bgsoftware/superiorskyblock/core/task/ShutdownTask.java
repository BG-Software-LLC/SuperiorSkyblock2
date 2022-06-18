package com.bgsoftware.superiorskyblock.core.task;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

public class ShutdownTask extends Thread {

    private final SuperiorSkyblockPlugin plugin;

    public ShutdownTask(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getGrid().wasPluginDisabled()) {
            SuperiorSkyblockPlugin.log("&cDetected crash. SuperiorSkyblock will attempt to save data...");
            plugin.onDisable();
        }
    }

}
