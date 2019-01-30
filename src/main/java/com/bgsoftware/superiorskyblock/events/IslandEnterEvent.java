package com.bgsoftware.superiorskyblock.events;

import com.bgsoftware.superiorskyblock.island.Island;
import com.bgsoftware.superiorskyblock.wrappers.WrappedPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class IslandEnterEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private WrappedPlayer wrappedPlayer;
    private Island island;

    public IslandEnterEvent(Player player, Island island){
        this(WrappedPlayer.of(player), island);
    }

    public IslandEnterEvent(WrappedPlayer wrappedPlayer, Island island){
        this.wrappedPlayer = wrappedPlayer;
        this.island = island;
    }

    public WrappedPlayer getPlayer() {
        return wrappedPlayer;
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
