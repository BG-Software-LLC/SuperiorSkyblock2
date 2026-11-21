package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.KeyCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;

/**
 * IslandChangeBlockCategoryLimitEvent is called when a block-category-limit of an island is changed.
 */
public class IslandChangeBlockCategoryLimitEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;
    private final KeyCategory blockCategory;

    private int blockCategoryLimit;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that changed the block category limit of an island.
     *                            If set to null, it means the limit was changed via the console.
     * @param island The island that the block category limit was changed for.
     * @param blockCategory The block category that the limit was changed for.
     * @param blockCategoryLimit The new block category limit of the block category.
     */
    public IslandChangeBlockCategoryLimitEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, KeyCategory blockCategory, int blockCategoryLimit) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.blockCategory = blockCategory;
        this.blockCategoryLimit = blockCategoryLimit;
    }

    /**
     * Get the player that changed the block category limit.
     * If null, it means the limit was changed by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the block category that the limit was changed for.
     */
    public KeyCategory getBlockCategory() {
        return blockCategory;
    }

    /**
     * Get the new block category limit of the block category.
     */
    public int getBlockCategoryLimit() {
        return blockCategoryLimit;
    }

    /**
     * Set the new block category limit for the block category.
     *
     * @param blockCategoryLimit The new block category limit to set.
     */
    public void setBlockCategoryLimit(int blockCategoryLimit) {
        Preconditions.checkArgument(blockCategoryLimit >= 0, "Cannot set the block category limit to a negative limit.");
        this.blockCategoryLimit = blockCategoryLimit;
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
