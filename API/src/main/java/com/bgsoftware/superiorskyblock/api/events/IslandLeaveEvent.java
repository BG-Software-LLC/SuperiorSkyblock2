package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class IslandLeaveEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private SuperiorPlayer superiorPlayer;
    private Island island;

    public IslandLeaveEvent(SuperiorPlayer superiorPlayer, Island island){
        this.superiorPlayer = superiorPlayer;
        this.island = island;
    }

    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    public Island getIsland() {
        return island;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
