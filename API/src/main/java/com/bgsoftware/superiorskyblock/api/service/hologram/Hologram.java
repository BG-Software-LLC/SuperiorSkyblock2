package com.bgsoftware.superiorskyblock.api.service.hologram;

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

}
