package com.bgsoftware.superiorskyblock.service.hologram;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.hologram.Hologram;
import com.bgsoftware.superiorskyblock.api.service.hologram.HologramsService;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class HologramsServiceImpl implements HologramsService {

    private final SuperiorSkyblockPlugin plugin;

    public HologramsServiceImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Hologram createHologram(Location location) {
        return plugin.getNMSHolograms().createHologram(location);
    }

    @Override
    public boolean isHologram(Entity entity) {
        return plugin.getNMSHolograms().isHologram(entity);
    }

}
