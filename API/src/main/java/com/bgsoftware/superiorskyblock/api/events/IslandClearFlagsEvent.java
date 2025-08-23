package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandClearFlagsEvent is called when flags are cleared on an island.
 */
public class IslandClearFlagsEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param island         The island that the flags were cleared in.
     * @param superiorPlayer The player that cleared the flags, or null if it was done by console.
     */
    public IslandClearFlagsEvent(Island island, @Nullable SuperiorPlayer superiorPlayer) {
        super(island);
        this.superiorPlayer = superiorPlayer;
    }

    /**
     * Get the player that cleared the flags to the island, or null if it was done by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
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
