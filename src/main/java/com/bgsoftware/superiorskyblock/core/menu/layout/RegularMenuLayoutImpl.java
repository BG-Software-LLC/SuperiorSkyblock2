package com.bgsoftware.superiorskyblock.core.menu.layout;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuViewButton;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RegularMenuLayoutImpl<V extends MenuView<V, ?>> extends AbstractMenuLayout<V> implements MenuLayout<V> {

    private RegularMenuLayoutImpl(String title, InventoryType inventoryType, MenuTemplateButton<V>[] buttons) {
        super(title, inventoryType, buttons);
    }

    @Override
    protected void populateInventory(Inventory inventory, V menuView) {
        // Set all buttons in the menu
        for (int slot = 0; slot < this.buttons.length; ++slot) {
            MenuViewButton<V> button = this.buttons[slot].createViewButton(menuView);
            ItemStack buttonItem = button.createViewItem();
            if (buttonItem != null) {
                inventory.setItem(slot, buttonItem);
            }
        }
    }

    public static <V extends MenuView<V, ?>> Builder<V> newBuilder() {
        return new Builder<>();
    }

    public static class Builder<V extends MenuView<V, ?>> extends AbstractBuilder<V> implements MenuLayout.Builder<V> {

        @Override
        public RegularMenuLayoutImpl<V> build() {
            return new RegularMenuLayoutImpl<>(super.title, super.inventoryType, super.buttons);
        }

    }

}
