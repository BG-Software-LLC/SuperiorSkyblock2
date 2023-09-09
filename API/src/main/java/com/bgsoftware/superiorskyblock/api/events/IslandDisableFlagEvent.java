package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandDisableFlagEvent is called when a flag is disabling for an island.
 */
public class IslandDisableFlagEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;
    private final IslandFlag islandFlag;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that disabled the island flag for the island.
     *                       If null, the flag was disabled by console.
     * @param island         The island that the flag was disabled for.
     * @param islandFlag     The flag that was disabled.
     */
    public IslandDisableFlagEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, IslandFlag islandFlag) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.islandFlag = islandFlag;
    }

    /**
     * Get the player that disabled the island flag for the island.
     * If null, the flag was disabled by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the flag that was disabled.
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
