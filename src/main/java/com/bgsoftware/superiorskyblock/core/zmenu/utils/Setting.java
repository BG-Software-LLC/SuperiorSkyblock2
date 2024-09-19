package com.bgsoftware.superiorskyblock.core.zmenu.utils;

import fr.maxlego08.menu.MenuItemStack;

public class Setting {

    private final String name;
    private final MenuItemStack itemStackEnabled;
    private final MenuItemStack itemStackDisabled;
    private final int position;

    public Setting(String name, MenuItemStack itemStackEnabled, MenuItemStack itemStackDisabled, int position) {
        this.name = name;
        this.itemStackEnabled = itemStackEnabled;
        this.itemStackDisabled = itemStackDisabled;
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public MenuItemStack getItemStackEnabled() {
        return itemStackEnabled;
    }

    public MenuItemStack getItemStackDisabled() {
        return itemStackDisabled;
    }

    public int getPosition() {
        return position;
    }
}
