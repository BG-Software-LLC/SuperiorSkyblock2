package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public final class PreIslandCreateEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer superiorPlayer;
    private boolean teleport = true;
    private boolean cancelled = false;

    public PreIslandCreateEvent(SuperiorPlayer superiorPlayer){
        this.superiorPlayer = superiorPlayer;
    }

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

    public void setTeleport(boolean teleport) {
        this.teleport = teleport;
    }

    public boolean getTeleport() {
        return teleport;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
