package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

public final class MenusListener implements Listener {

    @EventHandler
    public void onMenuClick(InventoryClickEvent e){
        InventoryHolder inventoryHolder = e.getClickedInventory() == null ? null : e.getClickedInventory().getHolder();

        if(!(inventoryHolder instanceof SuperiorMenu) || !(e.getWhoClicked() instanceof Player))
            return;

        e.setCancelled(true);

        ((SuperiorMenu) inventoryHolder).onClick(e);
    }

    @EventHandler
    public void onMenuClose(InventoryCloseEvent e){
        InventoryHolder inventoryHolder = e.getInventory() == null ? null : e.getInventory().getHolder();

        if(!(inventoryHolder instanceof SuperiorMenu) || !(e.getPlayer() instanceof Player))
            return;

        ((SuperiorMenu) inventoryHolder).closeInventory(SSuperiorPlayer.of(e.getPlayer()));
    }

}
