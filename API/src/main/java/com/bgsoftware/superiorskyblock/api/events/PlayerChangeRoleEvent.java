package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * PlayerChangeRoleEvent is called when a player has its role changed.
 */
public class PlayerChangeRoleEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer superiorPlayer;
    private final PlayerRole newRole;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player.
     * @param newRole        The new role for the player.
     */
    public PlayerChangeRoleEvent(SuperiorPlayer superiorPlayer, PlayerRole newRole) {
        super(!Bukkit.isPrimaryThread());
        this.superiorPlayer = superiorPlayer;
        this.newRole = newRole;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Get the player who created the island.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the new role of the player.
     */
    public PlayerRole getNewRole() {
        return newRole;
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

}
