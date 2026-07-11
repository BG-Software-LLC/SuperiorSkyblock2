package com.bgsoftware.superiorskyblock.api.menu.layout;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

/**
 * The layout class is used to describe the layout of buttons for an inventory menu.
 * It is later used by the plugin to create a new inventory for the menu.
 */
public interface InventoryMenuLayout<V extends MenuView<V, ?>> extends MenuLayout<V> {

    /**
     * Get the amount of rows for the layout.
     */
    int getRowsCount();

    /**
     * Create a new inventory from this layout.
     *
     * @param menuView The view to create the inventory for.
     */
    Inventory buildInventory(V menuView);

    /**
     * Create a new {@link Builder} object for a new {@link InventoryMenuLayout}.
     */
    static <V extends MenuView<V, ?>> Builder<V> newBuilder() {
        return SuperiorSkyblockAPI.getMenus().createInventoryLayoutBuilder();
    }

    interface Builder<V extends MenuView<V, ?>> extends MenuLayout.Builder<V> {

        /**
         * Set the inventory type for menu views created by this layout.
         *
         * @param inventoryType The inventory type to set.
         */
        Builder<V> setInventoryType(InventoryType inventoryType);

        /**
         * Set the rows count for menu views created by this layout.
         *
         * @param rowsCount The amount of rows to set.
         */
        Builder<V> setRowsCount(int rowsCount);

        /**
         * Get the {@link InventoryMenuLayout} from this builder.
         */
        InventoryMenuLayout<V> build();

    }

}
