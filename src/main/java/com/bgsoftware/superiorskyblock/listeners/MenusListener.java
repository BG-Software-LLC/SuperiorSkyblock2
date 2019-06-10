package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public final class MenusListener implements Listener {

    @EventHandler
    public void onMenuClick(InventoryClickEvent e){
        InventoryHolder inventoryHolder = e.getClickedInventory() == null ? null : e.getClickedInventory().getHolder();

        if(!(inventoryHolder instanceof SuperiorMenu))
            return;

        ((SuperiorMenu) inventoryHolder).onClick(e);
    }

}
