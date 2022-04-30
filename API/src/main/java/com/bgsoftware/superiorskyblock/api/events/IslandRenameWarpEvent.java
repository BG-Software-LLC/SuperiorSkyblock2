package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;

/**
 * IslandRenameWarpEvent is called when renaming a warp.
 */
public class IslandRenameWarpEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private final IslandWarp islandWarp;

    private String warpName;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that renamed the warp.
     * @param island         The island of the warp.
     * @param islandWarp     The warp that was renamed.
     * @param warpName       The new name of the warp.
     */
    public IslandRenameWarpEvent(SuperiorPlayer superiorPlayer, Island island, IslandWarp islandWarp, String warpName) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.islandWarp = islandWarp;
        this.warpName = warpName;
    }

    /**
     * Get the player that renamed the warp.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the warp that was renamed.
     */
    public IslandWarp getIslandWarp() {
        return islandWarp;
    }

    /**
     * Get the new name of the warp.
     */
    public String getWarpName() {
        return warpName;
    }

    /**
     * Set the new name for the warp.
     *
     * @param warpName The new warp name to set.
     */
    public void setWarpName(String warpName) {
        Preconditions.checkNotNull(warpName, "Cannot set warp name to null.");
        Preconditions.checkArgument(warpName.length() <= 255, "Warp names cannot be longer than 255 chars.");
        Preconditions.checkState(island.getWarp(warpName) == null, "Cannot rename warps to an already existing warps.");

        this.warpName = warpName;
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
