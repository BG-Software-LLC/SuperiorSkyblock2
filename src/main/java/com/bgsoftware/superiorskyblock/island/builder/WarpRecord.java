package com.bgsoftware.superiorskyblock.island.builder;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class WarpRecord {

    public final String name;
    public final String category;
    public final Location location;
    public final boolean isPrivate;
    @Nullable
    public final ItemStack icon;

    public WarpRecord(String name, String category, Location location, boolean isPrivate, @Nullable ItemStack icon) {
        this.name = name;
        this.category = category;
        this.location = location;
        this.isPrivate = isPrivate;
        this.icon = icon;
    }

}
