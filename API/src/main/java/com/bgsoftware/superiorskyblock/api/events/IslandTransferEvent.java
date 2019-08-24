package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class IslandTransferEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer oldOwner, newOwner;
    private final Island island;
    private boolean cancelled = false;

    public IslandTransferEvent(Island island, SuperiorPlayer oldOwner, SuperiorPlayer newOwner){
        this.island = island;
        this.oldOwner = oldOwner;
        this.newOwner = newOwner;
    }

    public SuperiorPlayer getOldOwner() {
        return oldOwner;
    }

    public SuperiorPlayer getNewOwner() {
        return newOwner;
    }

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
