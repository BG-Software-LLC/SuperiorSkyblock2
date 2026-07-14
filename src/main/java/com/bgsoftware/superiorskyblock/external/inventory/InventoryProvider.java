package com.bgsoftware.superiorskyblock.external.inventory;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public interface InventoryProvider {

    /**
     * Set the display name of the given item meta.
     *
     * @param itemMeta    The item meta to modify.
     * @param displayName The display name to set.
     */
    void setItemMetaDisplayName(ItemMeta itemMeta, String displayName);

    /**
     * Set the lore of the given item meta.
     *
     * @param itemMeta The item meta to modify.
     * @param lore     The lore to set.
     */
    void setItemMetaLore(ItemMeta itemMeta, List<String> lore);

    /**
     * Create a chest inventory with the specified size.
     *
     * @param inventoryHolder The inventory holder.
     * @param size            The inventory size.
     * @param title           The inventory title.
     * @return The created inventory.
     */
    Inventory createInventory(InventoryHolder inventoryHolder, int size, String title);

    /**
     * Create an inventory with the specified type.
     *
     * @param inventoryHolder The inventory holder.
     * @param inventoryType   The inventory type.
     * @param title           The inventory title.
     * @return The created inventory.
     */
    Inventory createInventory(InventoryHolder inventoryHolder, InventoryType inventoryType, String title);

}
