package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class IslandEnterProtectedEvent extends IslandEnterEvent {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled = false;

    @Deprecated
    public IslandEnterProtectedEvent(SuperiorPlayer superiorPlayer, Island island){
        super(superiorPlayer, island, EnterCause.INVALID);
    }

    public IslandEnterProtectedEvent(SuperiorPlayer superiorPlayer, Island island, EnterCause enterCause){
        super(superiorPlayer, island, enterCause);
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
