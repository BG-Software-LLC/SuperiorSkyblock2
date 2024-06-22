package com.bgsoftware.superiorskyblock.api.hooks;

import org.bukkit.entity.Entity;

public interface EntitiesProvider {

    /**
     * Should the plugin track the entity {@param entity}?
     * This is relevant for spawning, de-spawning and entity limits.
     * <p>
     * Please note: The entity provided to this function may be in unloaded chunks.
     * Do not attempt to change its attributes in this method in any way.
     *
     * @param entity The entity to check.
     */
    boolean shouldTrackEntity(Entity entity);

}
