package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandEnableFlagEvent is called when a flag is enabled for an island.
 */
public class IslandEnableFlagEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;
    private final IslandFlag islandFlag;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that enabled the island flag for the island.
     *                       If null, the flag was enabled by console.
     * @param island         The island that the flag was enabled for.
     * @param islandFlag     The flag that was enabled.
     */
    public IslandEnableFlagEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, IslandFlag islandFlag) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.islandFlag = islandFlag;
    }

    /**
     * Get the player that enabled the island flag for the island.
     * If null, the flag was enabled by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the flag that was enabled.
     */
    public IslandFlag getIslandFlag() {
        return islandFlag;
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
