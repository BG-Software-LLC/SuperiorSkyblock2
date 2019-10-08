package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.math.BigDecimal;

/**
 * IslandWorthCalculatedEvent is called when the worth of an island is calculated.
 */
public class IslandWorthCalculatedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Island island;
    private final BigDecimal level, worth;
    private final SuperiorPlayer player;

    /**
     * The constructor of the event.
     * @param island The island that it's worth was calculated.
     * @param level The new level of the island.
     * @param player The player who requested the recalculate (may be null).
     *
     * @deprecated See IslandWorthCalculatedEvent(Island, SuperiorPlayer, BigDecimal, BigDecimal)
     */
    @Deprecated
    public IslandWorthCalculatedEvent(Island island, BigDecimal level, SuperiorPlayer player) {
        this(island, player, level, island.getWorthAsBigDecimal());
    }

    /**
     * The constructor of the event.
     * @param island The island that it's worth was calculated.
     * @param player The player who requested the operation (may be null).
     * @param level The new level of the island.
     * @param worth The new worth value of the island.
     */
    public IslandWorthCalculatedEvent(Island island, SuperiorPlayer player, BigDecimal level, BigDecimal worth) {
        this.island = island;
        this.player = player;
        this.level = level;
        this.worth = worth;
    }

    /**
     * Get the island that it's worth was calculated.
     */
    public Island getIsland() {
        return island;
    }

    /**
     * Get the player who requested the operation.
     * Can be null in some cases.
     */
    public SuperiorPlayer getPlayer() {
        return player;
    }

    /**
     * Get the new level of the island.
     */
    public BigDecimal getLevel() {
        return level;
    }

    /**
     * Get the new worth value of the island.
     */
    public BigDecimal getWorth() {
        return worth;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
