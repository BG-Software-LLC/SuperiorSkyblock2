package com.bgsoftware.superiorskyblock.core.menu.layout;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuViewButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuViewButton;
import com.bgsoftware.superiorskyblock.api.menu.layout.PagedMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.PagedMenuView;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.CurrentPageButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.NextPageButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.PreviousPageButton;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class PagedMenuLayoutImpl<V extends MenuView<V, ?>, E> extends AbstractMenuLayout<V> implements PagedMenuLayout<V> {

    private final int objectsPerPageCount;

    private PagedMenuLayoutImpl(String title, InventoryType inventoryType, MenuTemplateButton<V>[] buttons) {
        super(title, inventoryType, buttons);
        objectsPerPageCount = (int) Arrays.stream(buttons).filter(button -> button instanceof PagedMenuTemplateButton).count();
    }

    @Override
    public int getObjectsPerPageCount() {
        return this.objectsPerPageCount;
    }

    @Override
    protected void populateInventory(Inventory inventory, V menuView) {
        if (!(menuView instanceof PagedMenuView))
            return;

        // noinspection unchecked
        PagedMenuView<V, ?, E> pagedMenuView = (PagedMenuView<V, ?, E>) menuView;

        int currentPage = pagedMenuView.getCurrentPage();
        List<E> pagedObjects = pagedMenuView.getPagedObjects();
        int pagedObjectSlot = 0;

        // Set all regular buttons in the menu
        for (int slot = 0; slot < this.buttons.length; ++slot) {
            MenuViewButton<V> button = this.buttons[slot].createViewButton(menuView);

            if (button instanceof PagedMenuViewButton) {
                PagedMenuViewButton<V, E> pagedMenuButton = (PagedMenuViewButton<V, E>) button;
                int objectIndex = pagedObjectSlot + (this.objectsPerPageCount * (currentPage - 1));

                ++pagedObjectSlot;

                if (objectIndex >= pagedObjects.size()) {
                    inventory.setItem(slot, ((PagedMenuTemplateButton<V, E>) pagedMenuButton.getTemplate()).getNullItem());
                    continue;
                } else {
                    pagedMenuButton.updateObject(pagedObjects.get(objectIndex));
                }
            }

            ItemStack buttonItem;

            try {
                buttonItem = button.createViewItem();
            } catch (Exception error) {
                Log.entering(getClass().getName(), "setupInventory", "ENTER", slot);
                Log.error(error, "An unexpected error occurred while setting up menu:");
                return;
            }

            if (buttonItem == null)
                continue;

            if (button instanceof PreviousPageButton) {
                inventory.setItem(slot, new ItemBuilder(buttonItem)
                        .replaceAll("{0}", (currentPage == 1 ? "&c" : "&a"))
                        .build(menuView.getInventoryViewer()));
            } else if (button instanceof NextPageButton) {
                inventory.setItem(slot, new ItemBuilder(buttonItem)
                        .replaceAll("{0}", (pagedObjects.size() > currentPage * this.objectsPerPageCount ? "&a" : "&c"))
                        .build(menuView.getInventoryViewer()));
            } else if (button instanceof CurrentPageButton) {
                inventory.setItem(slot, new ItemBuilder(buttonItem)
                        .replaceAll("{0}", currentPage + "")
                        .build(menuView.getInventoryViewer()));
            } else {
                inventory.setItem(slot, buttonItem);
            }
        }
    }

    public static <V extends PagedMenuView<V, ?, E>, E> Builder<V, E> newBuilder() {
        return new Builder<>();
    }

    public static class Builder<V extends PagedMenuView<V, ?, E>, E>
            extends AbstractBuilder<V>
            implements PagedMenuLayout.Builder<V, E> {

        @Override
        public Builder<V, E> setPreviousPageSlots(List<Integer> slots) {
            mapButtons(slots, new PreviousPageButton.Builder<>());
            return this;
        }

        @Override
        public Builder<V, E> setNextPageSlots(List<Integer> slots) {
            mapButtons(slots, new NextPageButton.Builder<>());
            return this;
        }

        @Override
        public Builder<V, E> setCurrentPageSlots(List<Integer> slots) {
            mapButtons(slots, new CurrentPageButton.Builder<>());
            return this;
        }

        @Override
        public Builder<V, E> setPagedObjectSlots(List<Integer> slots, PagedMenuTemplateButton.Builder<V, E> buttonBuilder) {
            mapButtons(slots, buttonBuilder);
            return this;
        }

        @Override
        public PagedMenuLayoutImpl<V, E> build() {
            return new PagedMenuLayoutImpl<>(super.title, super.inventoryType, super.buttons);
        }

    }

}
