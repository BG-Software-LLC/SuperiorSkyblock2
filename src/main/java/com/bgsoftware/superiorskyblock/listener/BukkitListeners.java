package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BukkitListeners {

    private static final Pattern LISTENER_REGISTER_FAILURE =
            Pattern.compile("Plugin SuperiorSkyblock2 v(.*) has failed to register events for (.*) because (.*) does not exist\\.");

    private final SuperiorSkyblockPlugin plugin;

    private String listenerRegisterFailure = "";

    public BukkitListeners(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerListeners() {
        new AdminPlayersListener(this.plugin);
        new ChunksListener(this.plugin);
        new EntityTrackingListener(this.plugin);
        new FeaturesListener(this.plugin);
        new IslandFlagsListener(this.plugin);
        new IslandWorldEventsListener(this.plugin);
        new MenusListener(this.plugin);
        new PlayersListener(this.plugin);
        new PortalsListener(this.plugin);
        new ProtectionListener(this.plugin);
        new SignsListener(this.plugin);
        new StackedBlocksListener(this.plugin);
        new WorldDestructionListener(this.plugin);

        if (plugin.getSettings().isStopLeaving())
            new IslandOutsideListener(this.plugin);

        if (plugin.getSettings().isAutoBlocksTracking())
            new BlockChangesListener(this.plugin);

        if (!plugin.getSettings().getIslandPreviews().getLocations().isEmpty())
            new IslandPreviewListener(this.plugin);

        safeEventsRegister(new BukkitEventsListener(this.plugin));
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
