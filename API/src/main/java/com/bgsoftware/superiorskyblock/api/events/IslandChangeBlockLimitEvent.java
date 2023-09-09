package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;

/**
 * IslandChangeBlockLimitEvent is called when a block-limit of an island is changed.
 */
public class IslandChangeBlockLimitEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;
    private final Key block;

    private int blockLimit;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that changed the block limit of an island.
     *                       If set to null, it means the limit was changed via the console.
     * @param island         The island that the block limit was changed for.
     * @param block          The block that the limit was changed for.
     * @param blockLimit     The new block limit of the block
     */
    public IslandChangeBlockLimitEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, Key block, int blockLimit) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.block = block;
        this.blockLimit = blockLimit;
    }

    /**
     * Get the player that changed the block limit.
     * If null, it means the limit was changed by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the block that the limit was changed for.
     */
    public Key getBlock() {
        return block;
    }

    /**
     * Get the new block limit of the block.
     */
    public int getBlockLimit() {
        return blockLimit;
    }

    /**
     * Set the new block limit of the block.
     *
     * @param blockLimit The new block limit to set.
     */
    public void setBlockLimit(int blockLimit) {
        Preconditions.checkArgument(blockLimit >= 0, "Cannot set the block limit to a negative limit.");
        this.blockLimit = blockLimit;
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
