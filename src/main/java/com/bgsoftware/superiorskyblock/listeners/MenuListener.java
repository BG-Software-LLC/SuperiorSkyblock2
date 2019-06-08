package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.gui.menus.Menu;
import com.bgsoftware.superiorskyblock.handlers.MenuHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

@SuppressWarnings("unused")
public final class MenuListener implements Listener {

    private SuperiorSkyblockPlugin plugin;

    public MenuListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        MenuHandler handler = plugin.getMenuHandler();

        if (handler == null)
            return;

        Menu menu = handler.getMenus().get(event.getInventory());
        if (menu == null)
            return;

        menu.onClick(event);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (plugin.getMenuHandler() == null)
            return;

        plugin.getMenuHandler().getMenus().remove(event.getInventory());
    }

}
