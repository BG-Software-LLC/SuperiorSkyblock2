package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * PlayerToggleBypassEvent is called when a player toggles his bypass mode.
 */
public class PlayerToggleBypassEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer superiorPlayer;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that toggled the bypass mode.
     */
    public PlayerToggleBypassEvent(SuperiorPlayer superiorPlayer) {
        super(!Bukkit.isPrimaryThread());
        this.superiorPlayer = superiorPlayer;
    }

    /**
     * Get the player that toggled the bypass mode.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
