package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * IslandRestrictMoveEvent is called when a player is cancelled from moving by the plugin.
 */
public class IslandRestrictMoveEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer superiorPlayer;
    private final RestrictReason restrictReason;

    /**
     * The constructor for the event.
     *
     * @param superiorPlayer The player which was restricted.
     * @param restrictReason The reason for the restriction.
     */
    public IslandRestrictMoveEvent(SuperiorPlayer superiorPlayer, RestrictReason restrictReason) {
        this.superiorPlayer = superiorPlayer;
        this.restrictReason = restrictReason;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Get the player which was restricted.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the reason for the restriction.
     */
    public RestrictReason getRestrictReason() {
        return restrictReason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public enum RestrictReason {

        LEAVE_ISLAND_TO_OUTSIDE,
        LEAVE_PROTECTED_EVENT_CANCELLED,
        LEAVE_EVENT_CANCELLED,

        BANNED_FROM_ISLAND,
        LOCKED_ISLAND,
        ENTER_PROTECTED_EVENT_CANCELLED,
        ENTER_EVENT_CANCELLED,

    }

}
