package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

public class GameListeners {

    private final SuperiorSkyblockPlugin plugin;

    public GameListeners(SuperiorSkyblockPlugin plugin) {
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

        plugin.getPlatform().getEventsManager().registerGameEventsListener();
    }

    public void unregisterListeners() {
        plugin.getGameEventsDispatcher().clearCallbacks();
        plugin.getPlatform().getEventsManager().unregisterGameEventsListener();
    }

}
