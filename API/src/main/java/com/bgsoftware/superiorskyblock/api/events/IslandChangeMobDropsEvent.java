package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;

/**
 * IslandChangeMobDropsEvent is called when the mob-drops multiplier of the island is changed.
 */
public class IslandChangeMobDropsEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;

    private double mobDrops;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that changed the mob-drops multiplier of the island.
     *                       If set to null, it means the mob-drops multiplier was changed via the console.
     * @param island         The island that the mob-drops multiplier was changed for.
     * @param mobDrops       The new mob drops of the island
     */
    public IslandChangeMobDropsEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, double mobDrops) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.mobDrops = mobDrops;
    }

    /**
     * Get the player that changed the mob-drops multiplier.
     * If null, it means the mob-drops multiplier was changed by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the new mob-drops multiplier of the island.
     */
    public double getMobDrops() {
        return mobDrops;
    }

    /**
     * Set the new mob-drops multiplier for the island.
     *
     * @param mobDrops The mob-drops multiplier to set.
     */
    public void setMobDrops(double mobDrops) {
        Preconditions.checkArgument(mobDrops >= 0, "Cannot set the mob-drops to a negative multiplier.");
        this.mobDrops = mobDrops;
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
