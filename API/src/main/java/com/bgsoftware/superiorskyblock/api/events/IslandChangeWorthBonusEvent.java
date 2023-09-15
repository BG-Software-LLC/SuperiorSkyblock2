package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;

import java.math.BigDecimal;

/**
 * IslandChangeWorthBonusEvent is called when the worth-bonus of the island is changed.
 */
public class IslandChangeWorthBonusEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;
    private final Reason reason;

    private BigDecimal worthBonus;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that changed the worth bonus of the island.
     *                       If set to null, it means the bonus was changed by console.
     * @param island         The island that the worth bonus was changed for.
     * @param reason         The reason for changing the worth bonus.
     * @param worthBonus     The new worth bonus of the island
     */
    public IslandChangeWorthBonusEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, Reason reason, BigDecimal worthBonus) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.reason = reason;
        this.worthBonus = worthBonus;
    }

    /**
     * Get the player that changed the worth bonus.
     * If null, it means the bonus was changed by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the reason for changing the worth bonus.
     */
    public Reason getReason() {
        return reason;
    }

    /**
     * Get the new worth bonus of the island.
     */
    public BigDecimal getWorthBonus() {
        return worthBonus;
    }

    /**
     * Set the new worth bonus for the island.
     *
     * @param worthBonus The new worth bonus to set.
     */
    public void setWorthBonus(BigDecimal worthBonus) {
        Preconditions.checkNotNull(worthBonus, "Cannot set the worth bonus to null.");
        this.worthBonus = worthBonus;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * The reason for changing the worth bonus.
     */
    public enum Reason {

        /**
         * The worth bonus was changed due to a command by a player or console.
         */
        COMMAND,

        /**
         * The worth bonus was changed due to schematic that was placed in the world for the island.
         */
        SCHEMATIC

    }

}
