package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.island.container.IslandsContainer;
import com.bgsoftware.superiorskyblock.api.player.container.PlayersContainer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * PluginInitializeEvent is called when other plugins needs to register their custom data.
 */
public class PluginInitializeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorSkyblock plugin;

    @Nullable
    private IslandsContainer islandsContainer;
    @Nullable
    private PlayersContainer playersContainer;

    /**
     * The constructor for the event.
     * You cannot use handlers in this time, as none of them is set up.
     *
     * @param plugin The instance of the plugin.
     */
    public PluginInitializeEvent(SuperiorSkyblock plugin) {
        this.plugin = plugin;
    }

    /**
     * Get the instance of the plugin.
     *
     * @return
     */
    public SuperiorSkyblock getPlugin() {
        return plugin;
    }

    /**
     * Get the islands container that will be used for storing islands.
     * If null, default islands container of the plugin will be used.
     */
    @Nullable
    public IslandsContainer getIslandsContainer() {
        return islandsContainer;
    }

    /**
     * Set a new islands container to be used for storing islands.
     *
     * @param islandsContainer The new container.
     */
    public void setIslandsContainer(IslandsContainer islandsContainer) {
        Preconditions.checkNotNull(islandsContainer, "IslandsContainer cannot be set to null.");
        this.islandsContainer = islandsContainer;
    }

    /**
     * Get the players container that will be used for storing players.
     * If null, default players container of the plugin will be used.
     */
    @Nullable
    public PlayersContainer getPlayersContainer() {
        return playersContainer;
    }

    /**
     * Set a new players container to be used for storing players.
     *
     * @param playersContainer The new container.
     */
    public void setPlayersContainer(PlayersContainer playersContainer) {
        Preconditions.checkNotNull(playersContainer, "PlayersContainer cannot be set to null.");
        this.playersContainer = playersContainer;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
