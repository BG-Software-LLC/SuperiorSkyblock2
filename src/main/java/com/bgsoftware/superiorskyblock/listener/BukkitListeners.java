package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BukkitListeners {

    private static final Pattern LISTENER_REGISTER_FAILURE =
            Pattern.compile("Plugin SuperiorSkyblock2 v(.*) has failed to register events for (.*) because (.*) does not exist\\.");

    private final SuperiorSkyblockPlugin plugin;

    private String listenerRegisterFailure = "";
    private boolean foliaBridgeRegistered;
    private List<AbstractGameEventListener> foliaGameListeners = Collections.emptyList();

    public BukkitListeners(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerListeners() {
        if (plugin.getTaskScheduler().isFolia()) {
            List<AbstractGameEventListener> replacements = new ArrayList<>();
            plugin.getGameEventsDispatcher().replaceCallbacks(this.foliaGameListeners,
                    () -> replacements.addAll(registerGameListeners()));
            this.foliaGameListeners = replacements;
            if (!this.foliaBridgeRegistered) {
                safeEventsRegister(new BukkitEventsListener(this.plugin));
                this.foliaBridgeRegistered = true;
            }
        } else {
            registerGameListeners();
            safeEventsRegister(new BukkitEventsListener(this.plugin));
        }
    }

    private List<AbstractGameEventListener> registerGameListeners() {
        List<AbstractGameEventListener> listeners = new ArrayList<>();
        listeners.add(new AdminPlayersListener(this.plugin));
        listeners.add(new ChunksListener(this.plugin));
        listeners.add(new EntityTrackingListener(this.plugin));
        listeners.add(new FeaturesListener(this.plugin));
        listeners.add(new IslandFlagsListener(this.plugin));
        listeners.add(new IslandWorldEventsListener(this.plugin));
        listeners.add(new MenusListener(this.plugin));
        listeners.add(new PlayersListener(this.plugin));
        listeners.add(new PortalsListener(this.plugin));
        listeners.add(new ProtectionListener(this.plugin));
        listeners.add(new SignsListener(this.plugin));
        listeners.add(new StackedBlocksListener(this.plugin));
        listeners.add(new WorldDestructionListener(this.plugin));

        if (plugin.getSettings().isStopLeaving())
            listeners.add(new IslandOutsideListener(this.plugin));

        if (plugin.getSettings().isAutoBlocksTracking())
            listeners.add(new BlockChangesListener(this.plugin));

        if (!plugin.getSettings().getIslandPreviews().getLocations().isEmpty())
            listeners.add(new IslandPreviewListener(this.plugin));

        return listeners;
    }

    public void unregisterListeners() {
        plugin.getGameEventsDispatcher().clearCallbacks();
        HandlerList.unregisterAll(this.plugin);
    }

    public void registerListenerFailureFilter() {
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
