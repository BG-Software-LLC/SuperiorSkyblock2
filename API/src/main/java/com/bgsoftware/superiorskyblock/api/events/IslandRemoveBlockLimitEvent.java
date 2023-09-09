package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandRemoveBlockLimitEvent is called when a block-limit of an island is removed.
 */
public class IslandRemoveBlockLimitEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;
    private final Key block;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that removed the block limit of an island.
     *                       If set to null, it means the limit was removed via the console.
     * @param island         The island that the block limit was removed for.
     * @param block          The block that the limit was removed for.
     */
    public IslandRemoveBlockLimitEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, Key block) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.block = block;
    }

    /**
     * Get the player that removed the block limit.
     * If null, it means the limit was removed by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the block that the limit was removed for.
     */
    public Key getBlock() {
        return block;
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
