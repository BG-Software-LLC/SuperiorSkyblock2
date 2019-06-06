package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.gui.menus.Menu;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class MenuHandler {

    private SuperiorSkyblockPlugin plugin;

    public Map<Inventory, Menu> menus;

    public MenuHandler(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;

        menus = new HashMap<>();
    }

    public void save() {
        // TODO close menus
    }

    public Map<Inventory, Menu> getMenus() {
        return menus;
    }
}
