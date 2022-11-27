package com.bgsoftware.superiorskyblock.api.menu.layout;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.Collection;
import java.util.List;

/**
 * The layout class is used to describe the layout of buttons for a menu.
 * It is later used by the plugin to create a new inventory for the menu.
 */
public interface MenuLayout<V extends MenuView<V, ?>> {

    /**
     * Get a button by a slot.
     *
     * @param slot The slot to get a button from.
     * @return The button in this slot.
     * If no button was explicitly set by the builder, a dummy button will be return.
     */
    MenuTemplateButton<V> getButton(int slot);

    /**
     * Get all the buttons in the layout.
     */
    Collection<MenuTemplateButton<V>> getButtons();

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
     * Create a new {@link Builder} object for a new {@link MenuLayout}.
     */
    static <V extends MenuView<V, ?>> Builder<V> newBuilder() {
        return SuperiorSkyblockAPI.getMenus().createPatternBuilder();
    }

    interface Builder<V extends MenuView<V, ?>> {

        /**
         * Set a title for menu views created by this layout.
         *
         * @param title The title to set.
         */
        Builder<V> setTitle(String title);

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
         * Set a button in a slot for this layout.
         *
         * @param slot   The slot to set the button.
         * @param button The button to set.
         */
        Builder<V> setButton(int slot, MenuTemplateButton<V> button);

        /**
         * Fill this layout with the given buttons.
         *
         * @param buttons The buttons to fill this layout with.
         */
        Builder<V> setButtons(MenuTemplateButton<V>[] buttons);

        /**
         * Set a button in slots for this layout.
         *
         * @param slots         The slot to set the button.
         * @param buttonBuilder The builder of the button to set.
         */
        Builder<V> setButtons(List<Integer> slots, MenuTemplateButton<V> buttonBuilder);

        /**
         * Map the button in the given slot to the provided button builder.
         * This will set the slot with the provided button type with the data of the current button.
         * If no button exists for this slot, this will be the same as {@link #setButton(int, MenuTemplateButton)}
         *
         * @param slot          The slot to set the button.
         * @param buttonBuilder The builder of the button to map.
         */
        Builder<V> mapButton(int slot, MenuTemplateButton.Builder<V> buttonBuilder);

        /**
         * Map the button in the given slots to the provided button builder.
         * This will set the slots with the provided button type with the data of the current buttons.
         * If no buttons exist for the slots, this will be the same as {@link #setButtons(List, MenuTemplateButton)}
         *
         * @param slots         The slots to set the button.
         * @param buttonBuilder The builder of the button to map.
         */
        Builder<V> mapButtons(List<Integer> slots, MenuTemplateButton.Builder<V> buttonBuilder);

        /**
         * Get the {@link MenuLayout} from this builder.
         */
        MenuLayout<V> build();

    }

}
