package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class IslandEnterEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer superiorPlayer;
    private final Island island;
    private final EnterCause enterCause;
    private boolean cancelled = false;
    private Location cancelTeleport = null;

    @Deprecated
    public IslandEnterEvent(SuperiorPlayer superiorPlayer, Island island){
        this(superiorPlayer, island, EnterCause.INVALID);
    }

    public IslandEnterEvent(SuperiorPlayer superiorPlayer, Island island, EnterCause enterCause){
        this.superiorPlayer = superiorPlayer;
        this.island = island;
        this.enterCause = enterCause;
    }

    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    public Island getIsland() {
        return island;
    }

    public EnterCause getCause() {
        return enterCause;
    }

    public void setCancelTeleport(Location cancelTeleport) {
        this.cancelTeleport = cancelTeleport.clone();
    }

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

    public enum EnterCause{

        PLAYER_MOVE,
        PLAYER_TELEPORT,
        PLAYER_JOIN,
        PORTAL,
        INVALID

    }

}
