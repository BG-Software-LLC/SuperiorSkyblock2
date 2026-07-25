package com.bgsoftware.superiorskyblock.bukkit.event;

import com.bgsoftware.superiorskyblock.bukkit.SuperiorSkyblockBukkitPlugin;
import com.bgsoftware.superiorskyblock.platform.event.IEventsManager;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BukkitEventsManager implements IEventsManager {

    private static final Pattern LISTENER_REGISTER_FAILURE =
            Pattern.compile("Plugin SuperiorSkyblock2 v(.*) has failed to register events for (.*) because (.*) does not exist\\.");

    private final SuperiorSkyblockBukkitPlugin plugin;

    private String listenerRegisterFailure = "";

    public BukkitEventsManager(SuperiorSkyblockBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerGameEventsListener() {
        registerListenerFailureFilter();
        safeEventsRegister(new BukkitEventsListener(this.plugin));
    }

    @Override
    public void unregisterGameEventsListener() {
        HandlerList.unregisterAll(this.plugin);
    }

    private void registerListenerFailureFilter() {
        plugin.getLogger().setFilter(record -> {
            Matcher matcher = LISTENER_REGISTER_FAILURE.matcher(record.getMessage());
            if (matcher.find())
                listenerRegisterFailure = matcher.group(3);

            return true;
        });
    }

    private void safeEventsRegister(Listener listener) {
        listenerRegisterFailure = "";
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        if (!listenerRegisterFailure.isEmpty())
            throw new RuntimeException(listenerRegisterFailure);
    }

}
