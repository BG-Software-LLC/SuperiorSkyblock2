package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;

/**
 * IslandChangeWarpsLimitEvent is called when the warps limit of an island is changed.
 */
public class IslandChangeWarpsLimitEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;

    private int warpsLimit;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that changed the warps limit of an island.
     *                       If set to null, it means the limit was changed via the console.
     * @param island         The island that the warps limit was changed for.
     * @param warpsLimit     The new warps limit of an island.
     */
    public IslandChangeWarpsLimitEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, int warpsLimit) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.warpsLimit = warpsLimit;
    }

    /**
     * Get the player that changed the warps limit.
     * If null, it means the limit was changed by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the new warps limit of the island.
     */
    public int getWarpsLimit() {
        return warpsLimit;
    }

    /**
     * Set the new warps limit of the island.
     *
     * @param warpsLimit The new warps limit to set.
     */
    public void setWarpsLimit(int warpsLimit) {
        Preconditions.checkArgument(warpsLimit >= 0, "Cannot set the warps limit to a negative limit.");
        this.warpsLimit = warpsLimit;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
