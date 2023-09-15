package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.math.BigDecimal;

/**
 * IslandWorthCalculatedEvent is called when the worth of an island is calculated.
 */
public class IslandWorthCalculatedEvent extends IslandEvent {

    private final BigDecimal level;
    private final BigDecimal worth;
    @Nullable
    private final SuperiorPlayer player;

    /**
     * The constructor of the event.
     *
     * @param island The island that it's worth was calculated.
     * @param player The player who requested the operation (may be null).
     * @param level  The new level of the island.
     * @param worth  The new worth value of the island.
     */
    public IslandWorthCalculatedEvent(Island island, @Nullable SuperiorPlayer player, BigDecimal level, BigDecimal worth) {
        super(island);
        this.player = player;
        this.level = level;
        this.worth = worth;
    }

    /**
     * Get the player who requested the operation.
     * Can be null if the console called the operation.
     */
    @Nullable
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
}
