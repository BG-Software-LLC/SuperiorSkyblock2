package com.ome_r.superiorskyblock.events;

import com.ome_r.superiorskyblock.island.Island;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class IslandLeaveEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private WrappedPlayer wrappedPlayer;
    private Island island;

    public IslandLeaveEvent(Player player, Island island){
        this(WrappedPlayer.of(player), island);
    }

    public IslandLeaveEvent(WrappedPlayer wrappedPlayer, Island island){
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
