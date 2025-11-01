package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandWarpTeleportEvent is called when a player teleports to island warp.
 */
public class IslandWarpTeleportEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private final IslandWarp islandWarp;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param island         The island that the player teleports to.
     * @param superiorPlayer The player who teleports to the island warp.
     * @param islandWarp     The island warp that the player teleports to.
     */
    public IslandWarpTeleportEvent(Island island, SuperiorPlayer superiorPlayer, IslandWarp islandWarp) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.islandWarp = islandWarp;
    }

    /**
     * Get the player who teleports to the island warp.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the island warp that the player teleports to.
     */
    public IslandWarp getIslandWarp() {
        return this.islandWarp;
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
