package com.bgsoftware.superiorskyblock.core.menu.layout;

import com.bgsoftware.common.reflection.ClassInfo;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuViewButton;
import com.bgsoftware.superiorskyblock.api.menu.layout.InventoryMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.DummyButton;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class RegularInventoryMenuLayoutImpl<V extends MenuView<V, ?>> extends AbstractMenuLayout<V> implements InventoryMenuLayout<V> {

    private static final ReflectField<Object> INVENTORY = new ReflectField<>(
            new ClassInfo("inventory.CraftInventory", ClassInfo.PackageType.CRAFTBUKKIT),
            Object.class, "inventory").removeFinal();

    protected final InventoryType inventoryType;

    protected RegularInventoryMenuLayoutImpl(AbstractBuilder<V, ?> builder) {
        super(builder);
        this.inventoryType = builder.inventoryType;
    }

    @Override
    public int getRowsCount() {
        return this.buttons.length / 9;
    }

    @Override
    public final Inventory buildInventory(V menuView) {
        String title = getTitle(menuView);

        Inventory inventory = createInventory(menuView, title);

        populateInventory(inventory, menuView);

        return inventory;
    }

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

    private Inventory createInventory(InventoryHolder holder, String title) {
        Inventory inventory;

        if (inventoryType != InventoryType.CHEST) {
            inventory = plugin.getProviders().getUIProvider().createInventory(holder, inventoryType, title);
        } else {
            inventory = plugin.getProviders().getUIProvider().createInventory(holder, this.buttons.length, title);
        }

        if (inventory.getHolder() == null) {
            Object menuHolder = plugin.getNMSAlgorithms().createMenuInventoryHolder(inventoryType, holder, title);
            if (menuHolder != null)
                INVENTORY.set(inventory, menuHolder);
        }

        return inventory;
    }

    protected static abstract class AbstractBuilder<V extends MenuView<V, ?>, B extends AbstractBuilder<V, B>>
            extends AbstractMenuLayout.AbstractBuilder<V, B> {

        protected InventoryType inventoryType = InventoryType.CHEST;
        protected int rowsSize = 6;

        public B setInventoryType(InventoryType inventoryType) {
            this.inventoryType = inventoryType;
            updateButtons();
            return (B) this;
        }

        public B setRowsCount(int rowsCount) {
            this.rowsSize = rowsCount;
            updateButtons();
            return (B) this;
        }

        @Override
        public B setButtons(MenuTemplateButton<V>[] buttons) {
            setRowsCount(buttons.length / 9);
            return super.setButtons(buttons);
        }

        private void updateButtons() {
            switch (inventoryType) {
                case CHEST:
                    this.rowsSize = Math.max(Math.min(this.rowsSize, 6), 1);
                    this.buttons = new MenuTemplateButton[this.rowsSize * 9];
                    break;
                default:
                    this.buttons = new MenuTemplateButton[inventoryType.getDefaultSize()];
                    break;
            }

            Arrays.fill(this.buttons, DummyButton.EMPTY_BUTTON);
        }

    }

    public static class Builder<V extends MenuView<V, ?>> extends AbstractBuilder<V, Builder<V>> implements InventoryMenuLayout.Builder<V> {

        @Override
        public RegularInventoryMenuLayoutImpl<V> build() {
            return new RegularInventoryMenuLayoutImpl<>(this);
        }

    }

}
