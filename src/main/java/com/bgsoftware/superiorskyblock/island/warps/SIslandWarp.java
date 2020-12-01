package com.bgsoftware.superiorskyblock.island.warps;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public final class SIslandWarp implements IslandWarp {

    public static ItemBuilder DEFAULT_WARP_ICON;

    private final WarpCategory warpCategory;

    private String name;
    private Location location;
    private boolean privateFlag = true;
    private ItemStack icon;
    private double price = 0D;

    public SIslandWarp(String name, Location location, WarpCategory warpCategory){
        this.name = name;
        this.location = new Location(location.getWorld(), location.getBlockX() + 0.5, location.getBlockY(),
                location.getBlockZ() + 0.5, location.getYaw(), location.getPitch());
        this.warpCategory = warpCategory;
    }

    @Override
    public Island getIsland() {
        return warpCategory.getIsland();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        SuperiorSkyblockPlugin.debug("Action: Update Warp Name, Island: " + getIsland().getOwner().getName() + ", Warp: " + this.name + ", New Name: " + name);
        this.name = name;
        getIsland().getDataHandler().saveWarps();
    }

    @Override
    public Location getLocation() {
        return location.clone();
    }

    @Override
    public void setLocation(Location location) {
        SuperiorSkyblockPlugin.debug("Action: Update Warp Location, Island: " + getIsland().getOwner().getName() + ", Warp: " + this.name + ", New Location: " + SBlockPosition.of(location));
        this.location = location.clone();
        getIsland().getDataHandler().saveWarps();
    }

    @Override
    public boolean hasPrivateFlag() {
        return privateFlag;
    }

    @Override
    public void setPrivateFlag(boolean privateFlag) {
        SuperiorSkyblockPlugin.debug("Action: Update Warp Private, Island: " + getIsland().getOwner().getName() + ", Warp: " + this.name + ", Private: " + privateFlag);
        this.privateFlag = privateFlag;
        getIsland().getDataHandler().saveWarps();
    }

    @Override
    public ItemStack getRawIcon() {
        return icon == null ? null : icon.clone();
    }

    @Override
    public ItemStack getIcon(SuperiorPlayer superiorPlayer) {
        if(icon == null)
            return null;

        ItemBuilder itemBuilder = new ItemBuilder(icon)
                .replaceAll("{0}", name);
        return superiorPlayer == null ? itemBuilder.build() : itemBuilder.build(superiorPlayer);
    }

    @Override
    public void setIcon(ItemStack icon) {
        SuperiorSkyblockPlugin.debug("Action: Update Warp Icon, Island: " + getIsland().getOwner().getName() + ", Warp: " + this.name);
        this.icon = icon == null ? null : icon.clone();
        getIsland().getDataHandler().saveWarps();
    }

    @Override
    public WarpCategory getCategory() {
        return warpCategory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SIslandWarp that = (SIslandWarp) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}
