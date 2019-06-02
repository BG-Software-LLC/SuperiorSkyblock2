package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class IslandKickEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer superiorPlayer, targetPlayer;
    private final Island island;

    public IslandKickEvent(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer, Island island){
        this.superiorPlayer = superiorPlayer;
        this.targetPlayer = targetPlayer;
        this.island = island;
    }

    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    public SuperiorPlayer getTarget() {
        return targetPlayer;
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
