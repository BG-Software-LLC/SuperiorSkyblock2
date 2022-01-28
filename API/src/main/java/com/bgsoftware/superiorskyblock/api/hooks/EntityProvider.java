package com.bgsoftware.superiorskyblock.api.hooks;

import org.bukkit.entity.Entity;

/**
 * EntityProvider is used to get amount of entities from stacking plugins.
 * The amount's value is used to calculate entities correctly.
 */
public interface EntityProvider {

    /**
     * Get the amount of entities from this entity.
     * If the entity is not stacked, the value of 1 should be returned.
     *
     * @param entity The entity to get amount of.
     */
    int getEntityAmount(Entity entity);

}
