package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
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
        safeEventsRegister(new AdminPlayersListener(this.plugin));
        safeEventsRegister(new BlockChangesListener(this.plugin));
        safeEventsRegister(new ChunksListener(this.plugin));
        safeEventsRegister(new EntityTrackingListener(this.plugin));
        safeEventsRegister(new FeaturesListener(this.plugin));
        safeEventsRegister(new IslandFlagsListener(this.plugin));
        safeEventsRegister(new IslandOutsideListener(this.plugin));
        safeEventsRegister(new IslandPreviewListener(this.plugin));
        safeEventsRegister(new IslandWorldEventsListener(this.plugin));
        safeEventsRegister(new MenusListener());
        safeEventsRegister(new PlayersListener(this.plugin));
        safeEventsRegister(new PortalsListener(this.plugin));
        safeEventsRegister(new ProtectionListener(this.plugin));
        safeEventsRegister(new SignsListener(this.plugin));
        safeEventsRegister(new StackedBlocksListener(this.plugin));
        safeEventsRegister(new WorldDestructionListener(this.plugin));
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
