package com.bgsoftware.superiorskyblock.island.builder;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.wrappers.WorldPosition;
import org.bukkit.inventory.ItemStack;

public class WarpRecord {

    public final String name;
    public final String category;
    public final WorldPosition worldPosition;
    public final String worldName;
    public final boolean isPrivate;
    @Nullable
    public final ItemStack icon;

    public WarpRecord(String name, String category, WorldPosition worldPosition, String worldName, boolean isPrivate, @Nullable ItemStack icon) {
        this.name = name;
        this.category = category;
        this.worldPosition = worldPosition;
        this.worldName = worldName;
        this.isPrivate = isPrivate;
        this.icon = icon;
    }

}
