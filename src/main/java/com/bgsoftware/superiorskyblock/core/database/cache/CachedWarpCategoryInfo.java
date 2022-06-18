package com.bgsoftware.superiorskyblock.core.database.cache;

import com.bgsoftware.superiorskyblock.island.warp.SWarpCategory;
import org.bukkit.inventory.ItemStack;

public class CachedWarpCategoryInfo {

    public String name;
    public int slot = 0;
    public ItemStack icon = SWarpCategory.DEFAULT_WARP_ICON.clone();

    public CachedWarpCategoryInfo() {

    }

}
