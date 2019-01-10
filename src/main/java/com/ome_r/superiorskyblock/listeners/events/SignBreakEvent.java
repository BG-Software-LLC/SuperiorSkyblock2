package com.ome_r.superiorskyblock.listeners.events;

import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;
import org.bukkit.block.Sign;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SignBreakEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private WrappedPlayer wrappedPlayer;
    private Sign sign;

    public SignBreakEvent(WrappedPlayer wrappedPlayer, Sign sign){
        this.wrappedPlayer = wrappedPlayer;
        this.sign = sign;
    }

    public WrappedPlayer getPlayer() {
        return wrappedPlayer;
    }

    public Sign getSign() {
        return sign;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
