package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * IslandKickEvent is called when a player is kicked from his island.
 */
public class IslandKickEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer superiorPlayer, targetPlayer;
    private final Island island;

    /**
     * The constructor of the event.
     * @param superiorPlayer The player who kicked the other player.
     * @param targetPlayer The player that was kicked.
     * @param island The island that the player was kicked from.
     */
    public IslandKickEvent(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer, Island island){
        this.superiorPlayer = superiorPlayer;
        this.targetPlayer = targetPlayer;
        this.island = island;
    }

    /**
     * Get the player who kicked the other player.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the player that was kicked.
     */
    public SuperiorPlayer getTarget() {
        return targetPlayer;
    }

    /**
     * Get the island that the player was kicked from.
     */
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
