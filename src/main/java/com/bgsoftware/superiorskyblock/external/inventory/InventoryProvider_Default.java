package com.bgsoftware.superiorskyblock.external.inventory;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class InventoryProvider_Default implements InventoryProvider {

    @Override
    public void setItemMetaDisplayName(ItemMeta itemMeta, String displayName) {
        itemMeta.setDisplayName(displayName);
    }

    @Override
    public void setItemMetaLore(ItemMeta itemMeta, List<String> lore) {
        itemMeta.setLore(lore);
    }

    @Override
    public Inventory createInventory(InventoryHolder inventoryHolder, int size, String title) {
        return Bukkit.createInventory(inventoryHolder, size, title);
    }

    @Override
    public Inventory createInventory(InventoryHolder inventoryHolder, InventoryType inventoryType, String title) {
        return Bukkit.createInventory(inventoryHolder, inventoryType, title);
    }

}
