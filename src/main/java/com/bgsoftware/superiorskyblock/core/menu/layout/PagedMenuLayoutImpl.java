package com.bgsoftware.superiorskyblock.core.menu.layout;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuViewButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuViewButton;
import com.bgsoftware.superiorskyblock.api.menu.layout.PagedMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.PagedMenuView;
import com.bgsoftware.superiorskyblock.core.Mutable;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.CurrentPageButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.NextPageButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.PreviousPageButton;
import com.bgsoftware.superiorskyblock.core.menu.layout.order.CustomPagedLayoutOrder;
import com.bgsoftware.superiorskyblock.core.menu.layout.order.PagedLayoutOrder;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class PagedMenuLayoutImpl<V extends MenuView<V, ?>, E> extends AbstractMenuLayout<V> implements PagedMenuLayout<V> {

    @Nullable
    private final PagedLayoutOrder<V> customLayoutOrder;
    private final int objectsPerPageCount;

    private PagedMenuLayoutImpl(String title, InventoryType inventoryType, MenuTemplateButton<V>[] buttons, @Nullable PagedLayoutOrder<V> layoutOrder) {
        super(title, inventoryType, buttons);
        this.customLayoutOrder = layoutOrder;
        this.objectsPerPageCount = layoutOrder == null ? countPagedButtons(buttons) : layoutOrder.getObjectsPerPageCount();
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

        Mutable<Integer> pagedObjectSlot = new Mutable<>(0);

        // Set all regular buttons in the menu
        for (int slot = 0; slot < this.buttons.length; ++slot) {
            MenuViewButton<V> button = this.buttons[slot].createViewButton(menuView);

            if (this.customLayoutOrder != null && button instanceof PagedMenuViewButton)
                continue;

            populateInventoryWithButton(inventory, button, slot, pagedMenuView, pagedObjectSlot);
        }

        if (this.customLayoutOrder == null)
            return;

        PagedLayoutOrder.MenuButtonsIterator<V> buttonsIterator = this.customLayoutOrder.createIterator(this.buttons);
        while (buttonsIterator.hasNext()) {
            MenuTemplateButton<V> templateButton = buttonsIterator.next();

            if (!(templateButton instanceof PagedMenuTemplateButton))
                continue;

            MenuViewButton<V> button = templateButton.createViewButton(menuView);
            int slot = buttonsIterator.getSlot();

            populateInventoryWithButton(inventory, button, slot, pagedMenuView, pagedObjectSlot);
        }

    }

    private void populateInventoryWithButton(Inventory inventory, MenuViewButton<V> button, int slot,
                                             PagedMenuView<V, ?, E> menuView, Mutable<Integer> pagedObjectSlot) {
        int currentPage = menuView.getCurrentPage();
        List<E> pagedObjects = menuView.getPagedObjects();

        if (button instanceof PagedMenuViewButton) {
            PagedMenuViewButton<V, E> pagedMenuButton = (PagedMenuViewButton<V, E>) button;
            int objectIndex = pagedObjectSlot.getValue() + (this.objectsPerPageCount * (currentPage - 1));

            pagedObjectSlot.setValue(pagedObjectSlot.getValue() + 1);

            if (objectIndex >= pagedObjects.size()) {
                inventory.setItem(slot, ((PagedMenuTemplateButton<V, E>) pagedMenuButton.getTemplate()).getNullItem());
                return;
            } else {
                pagedMenuButton.updateObject(pagedObjects.get(objectIndex));
            }
        }

        ItemStack buttonItem;

        try {
            buttonItem = button.createViewItem();
        } catch (Exception error) {
            Log.entering("ENTER", slot);
            Log.error(error, "An unexpected error occurred while setting up menu:");
            return;
        }

        if (buttonItem == null)
            return;

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

    private static int countPagedButtons(MenuTemplateButton<?>[] buttons) {
        return (int) Arrays.stream(buttons).filter(button -> button instanceof PagedMenuTemplateButton).count();
    }

    public static <V extends PagedMenuView<V, ?, E>, E> Builder<V, E> newBuilder() {
        return new Builder<>();
    }

    public static class Builder<V extends PagedMenuView<V, ?, E>, E>
            extends AbstractBuilder<V>
            implements PagedMenuLayout.Builder<V, E> {

        @Nullable
        private PagedLayoutOrder<V> layoutOrder;

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
        public PagedMenuLayout.Builder<V, E> setCustomLayoutOrder(List<Integer> slotsOrder) {
            slotsOrder.removeIf(slot -> !(super.buttons[slot] instanceof PagedMenuTemplateButton));
            if (!slotsOrder.isEmpty())
                this.layoutOrder = new CustomPagedLayoutOrder<>(slotsOrder);
            return this;
        }

        @Override
        public PagedMenuLayoutImpl<V, E> build() {
            return new PagedMenuLayoutImpl<>(super.title, super.inventoryType, super.buttons, this.layoutOrder);
        }

    }

}
