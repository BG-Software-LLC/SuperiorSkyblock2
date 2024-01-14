package com.bgsoftware.superiorskyblock.api.hooks;

import org.bukkit.entity.Entity;

public interface EntitiesProvider {

    /**
     * Should the plugin track the entity {@param entity}?
     * This is relevant for spawning, de-spawning and entity limits.
     *
     * @param entity The entity to check.
     */
    boolean shouldTrackEntity(Entity entity);

}
