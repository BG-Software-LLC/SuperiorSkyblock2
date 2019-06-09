package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.gui.MenuTemplate;
import com.bgsoftware.superiorskyblock.gui.menus.Menu;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class MenuHandler {

    private Map<Inventory, Menu> menus;

    public MenuHandler(SuperiorSkyblockPlugin plugin) {
        menus = new HashMap<>();

        MenuTemplate.loadAll();
    }

    public void save() {
        // TODO close menus
        for (Inventory inventory : new HashSet<>(menus.keySet())) {
            for (HumanEntity viewer : new HashSet<>(inventory.getViewers())) {
                viewer.closeInventory();
            }
        }
    }

    public Map<Inventory, Menu> getMenus() {
        return menus;
    }
}
