package com.bgsoftware.superiorskyblock.tasks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

public final class ShutdownTask extends Thread {

    private final SuperiorSkyblockPlugin plugin;

    public ShutdownTask(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if(!plugin.getGrid().wasPluginDisabled()) {
            SuperiorSkyblockPlugin.log("&cDetected crash. SuperiorSkyblock will attempt to save data...");
            plugin.onDisable();
        }
    }

}
