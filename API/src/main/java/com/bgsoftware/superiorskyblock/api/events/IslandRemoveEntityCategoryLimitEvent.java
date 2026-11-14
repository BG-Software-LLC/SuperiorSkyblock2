package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.entity.EntityCategory;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandRemoveEntityCategoryLimitEvent is called when an entity-category-limit of an island is removed.
 */
public class IslandRemoveEntityCategoryLimitEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;
    private final EntityCategory entityCategory;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that removed the entity category limit of an island.
     *                       If set to null, it means the limit was removed via the console.
     * @param island         The island that the entity category limit was removed for.
     * @param entityCategory The entity category that the limit was removed for.
     */
    public IslandRemoveEntityCategoryLimitEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, EntityCategory entityCategory) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.entityCategory = entityCategory;
    }

    /**
     * Get the player that removed the entity category limit.
     * If null, it means the limit was removed by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the entity category that the limit was removed for.
     */
    public EntityCategory getEntityCategory() {
        return entityCategory;
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
