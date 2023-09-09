package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;

import java.math.BigDecimal;

/**
 * IslandChangeLevelBonusEvent is called when the level-bonus of the island is changed.
 */
public class IslandChangeLevelBonusEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;
    private final Reason reason;

    private BigDecimal levelBonus;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that changed the level bonus of the island.
     *                       If set to null, it means the bonus was changed by console.
     * @param island         The island that the level bonus was changed for.
     * @param reason         The reason for changing the level bonus.
     * @param levelBonus     The new level bonus of the island
     */
    public IslandChangeLevelBonusEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, Reason reason, BigDecimal levelBonus) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.reason = reason;
        this.levelBonus = levelBonus;
    }

    /**
     * Get the player that changed the level bonus.
     * If null, it means the bonus was changed by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the reason for changing the level bonus.
     */
    public Reason getReason() {
        return reason;
    }

    /**
     * Get the new level bonus of the island.
     */
    public BigDecimal getLevelBonus() {
        return levelBonus;
    }

    /**
     * Set the new level bonus for the island.
     *
     * @param levelBonus The new level bonus to set.
     */
    public void setLevelBonus(BigDecimal levelBonus) {
        Preconditions.checkNotNull(levelBonus, "Cannot set the level bonus to null.");
        this.levelBonus = levelBonus;
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
     * The reason for changing the level bonus.
     */
    public enum Reason {

        /**
         * The level bonus was changed due to a command by a player or console.
         */
        COMMAND,

        /**
         * The level bonus was changed due to schematic that was placed in the world for the island.
         */
        SCHEMATIC

    }

}
