package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;

/**
 * IslandChangeCropGrowthEvent is called when the crop-growth multiplier of the island is changed.
 */
public class IslandChangeCropGrowthEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;

    private double cropGrowth;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that changed the crop-growth multiplier of the island.
     *                       If set to null, it means the crop-growth multiplier was changed via the console.
     * @param island         The island that the crop-growth multiplier was changed for.
     * @param cropGrowth     The new crop-growth multiplier of the island
     */
    public IslandChangeCropGrowthEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, double cropGrowth) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.cropGrowth = cropGrowth;
    }

    /**
     * Get the player that changed the crop-growth multiplier.
     * If null, it means the crop-growth multiplier was changed by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the new crop-growth multiplier of the island.
     */
    public double getCropGrowth() {
        return cropGrowth;
    }

    /**
     * Set the new crop-growth multiplier for the island.
     *
     * @param cropGrowth The crop-growth multiplier to set.
     */
    public void setCropGrowth(double cropGrowth) {
        Preconditions.checkArgument(cropGrowth >= 0, "Cannot set the crop growth to a negative multiplier.");
        this.cropGrowth = cropGrowth;
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
