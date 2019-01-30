package com.bgsoftware.superiorskyblock.listeners.events;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.block.Sign;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public final class SignBreakEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private SuperiorPlayer superiorPlayer;
    private Sign sign;

    public SignBreakEvent(SuperiorPlayer superiorPlayer, Sign sign){
        this.superiorPlayer = superiorPlayer;
        this.sign = sign;
    }

    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
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
