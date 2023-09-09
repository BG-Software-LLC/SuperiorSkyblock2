package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandDeleteWarpEvent is called when a warp is deleted from an island.
 */
public class IslandDeleteWarpEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;
    private final IslandWarp islandWarp;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that deleted the warp.
     *                       If null, then the warp was deleted by the console.
     * @param island         The island that the warp was deleted from.
     * @param islandWarp     The warp that was deleted.
     */
    public IslandDeleteWarpEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, IslandWarp islandWarp) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.islandWarp = islandWarp;
    }

    /**
     * Get the player that deleted the warp.
     * If null, then the warp was deleted by the console or not by a command.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the warp that was deleted.
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
