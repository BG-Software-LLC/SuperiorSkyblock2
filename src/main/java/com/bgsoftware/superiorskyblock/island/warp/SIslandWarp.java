package com.bgsoftware.superiorskyblock.island.warp;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.database.bridge.IslandsDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class SIslandWarp implements IslandWarp {

    private final WarpCategory warpCategory;

    private String name;
    private Location location;
    private boolean isPrivate;
    private ItemStack icon;

    public SIslandWarp(String name, Location location, WarpCategory warpCategory, boolean isPrivate, @Nullable ItemStack icon) {
        this.name = name;
        this.location = location.clone().add(0.5, 0, 0.5);
        this.warpCategory = warpCategory;
        this.isPrivate = isPrivate;
        this.icon = icon;
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
        Preconditions.checkNotNull(name, "name parameter cannot be null.");

        Log.debug(Debug.SET_WARP_NAME, getOwnerName(), this.name, name);

        String oldName = this.name;
        this.name = name;

        IslandsDatabaseBridge.updateWarpName(getIsland(), this, oldName);
    }

    @Override
    public Location getLocation() {
        return location.clone();
    }

    @Override
    public void setLocation(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");

        Log.debug(Debug.SET_WARP_LOCATION, getOwnerName(), this.name, location);

        this.location = location.clone();

        IslandsDatabaseBridge.updateWarpLocation(getIsland(), this);
    }

    @Override
    public boolean hasPrivateFlag() {
        return isPrivate;
    }

    @Override
    public void setPrivateFlag(boolean privateFlag) {
        Log.debug(Debug.SET_WARP_PRIVATE, getOwnerName(), this.name, privateFlag);

        this.isPrivate = privateFlag;

        IslandsDatabaseBridge.updateWarpPrivateStatus(getIsland(), this);
    }

    @Override
    public ItemStack getRawIcon() {
        return icon == null ? null : icon.clone();
    }

    @Override
    public ItemStack getIcon(SuperiorPlayer superiorPlayer) {
        if (icon == null)
            return null;

        try {
            ItemBuilder itemBuilder = new ItemBuilder(icon)
                    .replaceAll("{0}", name);
            return superiorPlayer == null ? itemBuilder.build() : itemBuilder.build(superiorPlayer);
        } catch (Exception error) {
            setIcon(null);
            return null;
        }
    }

    @Override
    public void setIcon(ItemStack icon) {
        Log.debug(Debug.SET_WARP_ICON, getOwnerName(), this.name, icon);

        this.icon = icon == null ? null : icon.clone();

        IslandsDatabaseBridge.updateWarpIcon(getIsland(), this);
    }

    @Override
    public WarpCategory getCategory() {
        return warpCategory;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SIslandWarp that = (SIslandWarp) o;
        return name.equals(that.name);
    }

    private String getOwnerName() {
        SuperiorPlayer superiorPlayer = getIsland().getOwner();
        return superiorPlayer == null ? "None" : superiorPlayer.getName();
    }

}
