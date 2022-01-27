package com.bgsoftware.superiorskyblock.database.cache;

import com.bgsoftware.superiorskyblock.island.warps.SWarpCategory;
import org.bukkit.inventory.ItemStack;

public final class CachedWarpCategoryInfo {

    public String name;
    public int slot = 0;
    public ItemStack icon = SWarpCategory.DEFAULT_WARP_ICON.clone();

    public CachedWarpCategoryInfo() {

    }

}
