package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * IslandInviteEvent is called when a player is invited to an island.
 */
public class IslandInviteEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer superiorPlayer, targetPlayer;
    private final Island island;
    private boolean cancelled = false;

    /**
     * The constructor for the event.
     * @param superiorPlayer The player who sent the invite request.
     * @param targetPlayer The player who received the invite request.
     * @param island The island that the player was invited into.
     */
    public IslandInviteEvent(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer, Island island){
        this.superiorPlayer = superiorPlayer;
        this.targetPlayer = targetPlayer;
        this.island = island;
    }

    /**
     * Get the player who sent the invite request.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the player who received the invite request.
     */
    public SuperiorPlayer getTarget() {
        return targetPlayer;
    }

    /**
     * Get the island that the player was invited into.
     */
    public Island getIsland() {
        return island;
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
