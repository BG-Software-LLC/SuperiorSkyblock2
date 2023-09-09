package com.bgsoftware.superiorskyblock.api.island.warps;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class DelegateIslandWarp implements IslandWarp {

    protected final IslandWarp handle;

    protected DelegateIslandWarp(IslandWarp handle) {
        this.handle = handle;
    }

    @Override
    public Island getIsland() {
        return this.handle.getIsland();
    }

    @Override
    public String getName() {
        return this.handle.getName();
    }

    @Override
    public void setName(String name) {
        this.handle.setName(name);
    }

    @Override
    public Location getLocation() {
        return this.handle.getLocation();
    }

    @Override
    public void setLocation(Location location) {
        this.handle.setLocation(location);
    }

    @Override
    public boolean hasPrivateFlag() {
        return this.handle.hasPrivateFlag();
    }

    @Override
    public void setPrivateFlag(boolean privateFlag) {
        this.handle.setPrivateFlag(privateFlag);
    }

    @Nullable
    @Override
    public ItemStack getRawIcon() {
        return this.handle.getRawIcon();
    }

    @Nullable
    @Override
    public ItemStack getIcon(@Nullable SuperiorPlayer superiorPlayer) {
        return this.handle.getIcon(superiorPlayer);
    }

    @Override
    public void setIcon(@Nullable ItemStack icon) {
        this.handle.setIcon(icon);
    }

    @Override
    public WarpCategory getCategory() {
        return this.handle.getCategory();
    }

}
