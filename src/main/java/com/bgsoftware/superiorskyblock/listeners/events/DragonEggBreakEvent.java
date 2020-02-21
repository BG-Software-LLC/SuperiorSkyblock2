package com.bgsoftware.superiorskyblock.listeners.events;

import org.bukkit.entity.FallingBlock;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class DragonEggBreakEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final FallingBlock fallingBlock;
    private boolean cancelled = false;

    public DragonEggBreakEvent(FallingBlock fallingBlock){
        this.fallingBlock = fallingBlock;
    }

    public FallingBlock getFallingBlock() {
        return fallingBlock;
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
