package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * IslandEnterEvent is called when a player is walking into an island's area.
 */
public class IslandEnterEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer superiorPlayer;
    private final Island island;
    private final EnterCause enterCause;
    private boolean cancelled = false;
    private Location cancelTeleport = null;

    /**
     * The constructor of the event.
     * @param superiorPlayer The player who entered to the island's area.
     * @param island The island that the player entered into.
     *
     * @deprecated See IslandEnterEvent(SuperiorPlayer, Island, EnterCause)
     */
    @Deprecated
    public IslandEnterEvent(SuperiorPlayer superiorPlayer, Island island){
        this(superiorPlayer, island, EnterCause.INVALID);
    }

    /**
     * The constructor of the event.
     * @param superiorPlayer The player who entered to the island's area.
     * @param island The island that the player entered into.
     * @param enterCause The cause of entering into the island.
     */
    public IslandEnterEvent(SuperiorPlayer superiorPlayer, Island island, EnterCause enterCause){
        this.superiorPlayer = superiorPlayer;
        this.island = island;
        this.enterCause = enterCause;
    }

    /**
     * Get the player who entered to the island's area.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the island that the player entered into.
     */
    public Island getIsland() {
        return island;
    }

    /**
     * Get the cause of entering into the island.
     */
    public EnterCause getCause() {
        return enterCause;
    }

    /**
     * Set the location the player would be teleported if the event is cancelled.
     */
    public void setCancelTeleport(Location cancelTeleport) {
        this.cancelTeleport = cancelTeleport.clone();
    }

    /**
     * Get the location the player would be teleported if the event is cancelled.
     */
    public Location getCancelTeleport() {
        return cancelTeleport == null ? null : cancelTeleport.clone();
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

    /**
     * Used to determine the cause of entering.
     */
    public enum EnterCause{

        PLAYER_MOVE,
        PLAYER_TELEPORT,
        PLAYER_JOIN,
        PORTAL,
        INVALID

    }

}
