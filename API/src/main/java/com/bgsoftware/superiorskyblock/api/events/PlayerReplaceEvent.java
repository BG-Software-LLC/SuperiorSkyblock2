package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * PlayerReplaceEvent is called when a player changes his uuid and replaced with another one.
 */
public class PlayerReplaceEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer oldPlayer;
    private final SuperiorPlayer newPlayer;

    /**
     * The constructor of the event.
     *
     * @param oldPlayer The old player that had his UUID changed.
     * @param newPlayer The new player that has the new UUID.
     */
    public PlayerReplaceEvent(SuperiorPlayer oldPlayer, SuperiorPlayer newPlayer) {
        super(!Bukkit.isPrimaryThread());
        this.oldPlayer = oldPlayer;
        this.newPlayer = newPlayer;
    }

    /**
     * Get the old player that had his UUID changed.
     */
    public SuperiorPlayer getOldPlayer() {
        return oldPlayer;
    }

    /**
     * Get the new player that has the new UUID.
     */
    public SuperiorPlayer getNewPlayer() {
        return newPlayer;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
