package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;

/**
 * IslandChangeBorderSizeEvent is called when the border-size of the island is changed.
 */
public class IslandChangeBorderSizeEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;

    private int borderSize;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that changed the border size of the island.
     *                       If set to null, it means the limit was changed by console.
     * @param island         The island that the border size was changed for.
     * @param borderSize     The new border size of the island
     */
    public IslandChangeBorderSizeEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, int borderSize) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.borderSize = borderSize;
    }

    /**
     * Get the player that changed the border-size of the island.
     * If null, it means the size was changed by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the new border size of the island.
     */
    public int getBorderSize() {
        return borderSize;
    }

    /**
     * Set the new border size for the island.
     *
     * @param borderSize The new border size to set.
     */
    public void setBorderSize(int borderSize) {
        Preconditions.checkArgument(borderSize >= 1, "Cannot set the border size to values lower than 1.");
        this.borderSize = borderSize;
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
