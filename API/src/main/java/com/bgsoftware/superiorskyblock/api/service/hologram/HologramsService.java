package com.bgsoftware.superiorskyblock.api.service.hologram;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface HologramsService {

    /**
     * Create a new hologram.
     *
     * @param location The location of the new hologram.
     * @return The new created hologram.
     */
    Hologram createHologram(Location location);

    /**
     * Checks whether an entity is a hologram.
     *
     * @param entity The entity to check.
     */
    boolean isHologram(Entity entity);

}
