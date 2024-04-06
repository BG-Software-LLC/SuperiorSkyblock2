package com.bgsoftware.superiorskyblock.island.builder;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import org.bukkit.inventory.ItemStack;

public class WarpRecord {

    public final String name;
    public final String category;
    public final LazyWorldLocation location;
    public final boolean isPrivate;
    @Nullable
    public final ItemStack icon;

    public WarpRecord(String name, String category, LazyWorldLocation location, boolean isPrivate, @Nullable ItemStack icon) {
        this.name = name;
        this.category = category;
        this.location = location;
        this.isPrivate = isPrivate;
        this.icon = icon;
    }

}
