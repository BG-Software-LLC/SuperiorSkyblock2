package com.bgsoftware.superiorskyblock.core.zmenu.utils;

import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import fr.maxlego08.menu.MenuItemStack;

public class Permission {

    private final IslandPrivilege islandPrivilege;

    private final MenuItemStack itemStackEnabled;
    private final MenuItemStack itemStackDisabled;
    private final MenuItemStack itemStackPermission;

    public Permission(IslandPrivilege islandPrivilege, MenuItemStack itemStackEnabled, MenuItemStack itemStackDisabled, MenuItemStack itemStackPermission) {
        this.islandPrivilege = islandPrivilege;
        this.itemStackEnabled = itemStackEnabled;
        this.itemStackDisabled = itemStackDisabled;
        this.itemStackPermission = itemStackPermission;
    }

    public IslandPrivilege getIslandPrivilege() {
        return islandPrivilege;
    }

    public MenuItemStack getItemStackEnabled() {
        return itemStackEnabled;
    }

    public MenuItemStack getItemStackDisabled() {
        return itemStackDisabled;
    }

    public MenuItemStack getItemStackPermission() {
        return itemStackPermission;
    }
}
