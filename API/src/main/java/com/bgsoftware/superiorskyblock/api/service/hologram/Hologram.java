package com.bgsoftware.superiorskyblock.api.service.hologram;

import org.bukkit.entity.ArmorStand;

public interface Hologram {

    /**
     * Set the name to be displayed for this hologram.
     *
     * @param name The new name to set.
     */
    void setHologramName(String name);

    /**
     * Remove the hologram from existence.
     */
    void removeHologram();

    /**
     * Get the actual armor stand entity for this hologram.
     * This is a custom armor stand for the hologram, and therefore it may not function
     * similar to Bukkit's regular entities.
     */
    ArmorStand getHandle();

}
