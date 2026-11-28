package com.bgsoftware.superiorskyblock.missions.island;

import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.TimedRegisteredListener;

public class DynamicRegisteredListener extends TimedRegisteredListener {

    private static final Listener EMPTY_LISTENER = new Listener() {
    };

    public DynamicRegisteredListener(Plugin plugin, EventExecutor executor) {
        super(EMPTY_LISTENER, executor, EventPriority.MONITOR, plugin, true);
    }

}
