package com.bgsoftware.superiorskyblock.core.zmenu.utils;

import fr.maxlego08.menu.MenuItemStack;

public class Permission {

    private final String permission;

    private final MenuItemStack itemStackEnabled;
    private final MenuItemStack itemStackDisabled;
    private final MenuItemStack itemStackPermission;

    public Permission(String permission, MenuItemStack itemStackEnabled, MenuItemStack itemStackDisabled, MenuItemStack itemStackPermission) {
        this.permission = permission;
        this.itemStackEnabled = itemStackEnabled;
        this.itemStackDisabled = itemStackDisabled;
        this.itemStackPermission = itemStackPermission;
    }

    public String getPermission() {
        return permission;
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
