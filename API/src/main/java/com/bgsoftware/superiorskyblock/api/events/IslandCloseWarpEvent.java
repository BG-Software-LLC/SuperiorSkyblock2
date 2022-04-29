package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandCloseWarpEvent is called when closing the warp to the public.
 */
public class IslandCloseWarpEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private final IslandWarp islandWarp;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that closed the warp to the public.
     * @param island         The island of the warp.
     * @param islandWarp     The warp that was closed.
     */
    public IslandCloseWarpEvent(SuperiorPlayer superiorPlayer, Island island, IslandWarp islandWarp) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.islandWarp = islandWarp;
    }

    /**
     * Get the player that closed the warp to the public.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the warp that was closed.
     */
    public IslandWarp getIslandWarp() {
        return islandWarp;
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
