package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.KeyCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandRemoveBlockCategoryLimitEvent is called when a block-category-limit of an island is removed.
 */
public class IslandRemoveBlockCategoryLimitEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;
    private final KeyCategory blockCategory;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that removed the block category limit of an island.
     *                       If set to null, it means the limit was removed via the console.
     * @param island The island that the block category limit was removed for.
     * @param blockCategory The block category that the limit was removed for.
     */
    public IslandRemoveBlockCategoryLimitEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, KeyCategory blockCategory) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.blockCategory = blockCategory;
    }

    /**
     * Get the player that removed the block category limit.
     * If null, it means the limit was removed by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the block category that the limit was removed for.
     */
    public KeyCategory getBlockCategory() {
        return blockCategory;
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
