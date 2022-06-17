package com.bgsoftware.superiorskyblock.core.database.cache;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class CachedWarpInfo {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public String name;
    public String category;
    public Location location;
    public boolean isPrivate = !plugin.getSettings().isPublicWarps();
    public ItemStack icon = null;

    public CachedWarpInfo() {

    }

}
