package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.hologram.Hologram;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface NMSHolograms {

    Hologram createHologram(Location location);

    boolean isHologram(Entity entity);

}
