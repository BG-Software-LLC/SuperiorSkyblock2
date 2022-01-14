package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;

import java.math.BigDecimal;

/**
 * IslandWorthUpdateEvent is called when the worth of the island is updated.
 */
public class IslandWorthUpdateEvent extends IslandEvent {

    private final BigDecimal oldWorth;
    private final BigDecimal oldLevel;
    private final BigDecimal newWorth;
    private final BigDecimal newLevel;

    /**
     * The constructor of the event.
     *
     * @param island   The island that the leadership of it is transferred.
     * @param oldWorth The old worth of the island.
     * @param oldLevel The old level of the island.
     * @param newWorth The new worth of the island.
     * @param newLevel The new level of the island.
     */
    public IslandWorthUpdateEvent(Island island, BigDecimal oldWorth, BigDecimal oldLevel, BigDecimal newWorth, BigDecimal newLevel) {
        super(island);
        this.oldWorth = oldWorth;
        this.oldLevel = oldLevel;
        this.newWorth = newWorth;
        this.newLevel = newLevel;
    }

    /**
     * Get the old worth of the island.
     */
    public BigDecimal getOldWorth() {
        return oldWorth;
    }

    /**
     * Get the old level of the island.
     */
    public BigDecimal getOldLevel() {
        return oldLevel;
    }

    /**
     * Get the new worth of the island.
     */
    public BigDecimal getNewWorth() {
        return newWorth;
    }

    /**
     * Get the new level of the island.
     */
    public BigDecimal getNewLevel() {
        return newLevel;
    }
}
