package com.bgsoftware.superiorskyblock.database.cache;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.island.warps.SIslandWarp;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public final class CachedWarpInfo {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public String name;
    public String category;
    public Location location;
    public boolean isPrivate = !plugin.getSettings().isPublicWarps();
    public ItemStack icon = null;

    public CachedWarpInfo() {

    }

}
