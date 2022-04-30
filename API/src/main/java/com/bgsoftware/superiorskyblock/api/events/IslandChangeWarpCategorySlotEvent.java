package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;

/**
 * IslandChangeWarpCategorySlotEvent is called when the slot of a warp-category was changed.
 */
public class IslandChangeWarpCategorySlotEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private final WarpCategory warpCategory;
    private final int maxSlot;

    private int slot;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that changed the slot of the warp-category.
     * @param island         The island of the warp-category.
     * @param warpCategory   The warp-category that its slot was changed.
     * @param slot           The new slot of the warp-category.
     * @param maxSlot        The maximum slot that the warp category can occupy.
     */
    public IslandChangeWarpCategorySlotEvent(SuperiorPlayer superiorPlayer, Island island, WarpCategory warpCategory,
                                             int slot, int maxSlot) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.warpCategory = warpCategory;
        this.slot = slot;
        this.maxSlot = maxSlot;
    }

    /**
     * Get the player that changed the slot of the warp-category.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the warp-category that its slot was changed.
     */
    public WarpCategory getWarpCategory() {
        return warpCategory;
    }

    /**
     * Get the new slot of the warp-category.
     */
    public int getSlot() {
        return slot;
    }

    /**
     * Set the new slot for the warp-category.
     *
     * @param slot The new slot to set.
     */
    public void setSlot(int slot) {
        Preconditions.checkArgument(slot < maxSlot, "Cannot set the slot to outside of the inventory space.");
        Preconditions.checkState(island.getWarpCategory(slot) == null, "Cannot change the slot of" +
                " the category to an already existing another category's slot");

        this.slot = slot;
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
