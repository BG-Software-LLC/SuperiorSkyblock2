package com.bgsoftware.superiorskyblock.core.events.plugin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.CommandsMap;
import com.bgsoftware.superiorskyblock.commands.player.CmdAdmin;
import com.bgsoftware.superiorskyblock.commands.player.CmdHelp;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.events.EventCallback;
import com.bgsoftware.superiorskyblock.core.events.EventsDispatcher;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.island.SpawnIsland;
import com.bgsoftware.superiorskyblock.island.top.SortingTypes;
import com.bgsoftware.superiorskyblock.player.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.service.region.RegionManagerServiceImpl;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import java.util.EnumMap;
import java.util.List;

public class PluginEventsDispatcher extends EventsDispatcher<
        Void,
        PluginEventType<?>,
        PluginEventPriority,
        PluginEvent<?>> {

    public PluginEventsDispatcher(SuperiorSkyblockPlugin plugin) {
        super(plugin, PluginEventPriority.class, PluginEventType.values());
    }

    public void registerDefaultListeners() {
        SIsland.registerListeners(this);
        CommandsMap.registerListeners(this);
        SpawnIsland.registerListeners(this);
        RegionManagerServiceImpl.registerCallbacks(this);
        CmdHelp.registerListeners(this);
        CmdAdmin.registerCallbacks(this);
        Message.registerListeners(this);
        SSuperiorPlayer.registerListeners(this);
        SortingTypes.registerListeners(this);
    }

    public void registerCallback(PluginEventType<?> type, Runnable callback) {
        registerCallback(type, unused -> callback.run());
    }

    public <Args extends PluginEventArgs> void registerCallback(PluginEventType<Args> type, EventCallback<PluginEvent<Args>> callback) {
        registerCallback(null, type, PluginEventPriority.NORMAL, true, callback);
    }

    public <Args extends PluginEventArgs> PluginEvent<Args> fireEvent(PluginEventType<Args> type, Args args) {
        PluginEvent<Args> event = type.createEvent(args);

        EnumMap<PluginEventPriority, List<RegisteredListener>> gameEventCallbacks = callbacks.get(type);
        if (gameEventCallbacks != null) {
            gameEventCallbacks.forEach((priority, priorityCallbacks) -> {
                for (RegisteredListener listener : priorityCallbacks) {
                    if (listener.ignoreCancelled && event.isCancelled()) {
                        continue;
                    }

                    try {
                        listener.callback.execute(event);
                    } catch (Throwable error) {
                        Log.error(error, "Could not pass listener: " + listener.listener);
                    }
                }
            });
        }

        String bukkitEventName = type.getBukkitEventName();
        boolean fireEventDebug = !Text.isBlank(bukkitEventName);
        if (!fireEventDebug || !plugin.getSettings().getDisabledEvents().contains(bukkitEventName)) {
            if (fireEventDebug)
                Log.debug(Debug.FIRE_EVENT, bukkitEventName);

            Event bukkitEvent = type.createBukkitEvent(event.getArgs());
            if (bukkitEvent != null) {
                Bukkit.getPluginManager().callEvent(bukkitEvent);

                if (fireEventDebug && bukkitEvent instanceof Cancellable) {
                    Log.debugResult(Debug.FIRE_EVENT, "Cancelled:", ((Cancellable) bukkitEvent).isCancelled());
                }

                type.applyBukkitToPluginEvent(bukkitEvent, event);
            }
        }


        return event;
    }

}
