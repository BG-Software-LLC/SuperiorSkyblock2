package com.bgsoftware.superiorskyblock.listeners.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.math.BigDecimal;

public class IslandWorthCalculatedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private Island island;
    private BigDecimal level;
    private SuperiorPlayer player;

    public IslandWorthCalculatedEvent(Island island, BigDecimal level, SuperiorPlayer player) {
        this.island = island;
        this.level = level;
        this.player = player;
    }

    public Island getIsland() {
        return island;
    }

    public SuperiorPlayer getPlayer() {
        return player;
    }

    public BigDecimal getLevel() {
        return level;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
