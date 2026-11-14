package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.entity.EntityCategory;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;

/**
 * IslandChangeEntityCategoryLimitEvent is called when an entity-category-limit of an island is changed.
 */
public class IslandChangeEntityCategoryLimitEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;
    private final EntityCategory entityCategory;

    private int entityCategoryLimit;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer      The player that changed the entity category limit of an island.
     *                            If set to null, it means the limit was changed via the console.
     * @param island              The island that the entity category limit was changed for.
     * @param entityCategory      The entity category that the limit was changed for.
     * @param entityCategoryLimit The new entity category limit of the entity category.
     */
    public IslandChangeEntityCategoryLimitEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, EntityCategory entityCategory, int entityCategoryLimit) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.entityCategory = entityCategory;
        this.entityCategoryLimit = entityCategoryLimit;
    }

    /**
     * Get the player that changed the entity category limit.
     * If null, it means the limit was changed by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the entity category that the limit was changed for.
     */
    public EntityCategory getEntityCategory() {
        return entityCategory;
    }

    /**
     * Get the new entity category limit of the entity category.
     */
    public int getEntityCategoryLimit() {
        return entityCategoryLimit;
    }

    /**
     * Set the new entity category limit for the entity category.
     *
     * @param entityCategoryLimit The new entity category limit to set.
     */
    public void setEntityCategoryLimit(int entityCategoryLimit) {
        Preconditions.checkArgument(entityCategoryLimit >= 0, "Cannot set the entity category limit to a negative limit.");
        this.entityCategoryLimit = entityCategoryLimit;
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
