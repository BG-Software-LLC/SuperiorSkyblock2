package com.bgsoftware.superiorskyblock.core.menu.layout;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuViewButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuViewButton;
import com.bgsoftware.superiorskyblock.api.menu.layout.PagedInventoryMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.PagedMenuView;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.CurrentPageButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.NextPageButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.PreviousPageButton;
import com.bgsoftware.superiorskyblock.core.menu.layout.custom.CustomPagedLayout;
import com.bgsoftware.superiorskyblock.core.menu.layout.custom.PagedLayout;
import com.bgsoftware.superiorskyblock.core.mutable.MutableInt;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PagedInventoryMenuLayoutImpl<V extends PagedMenuView<V, ?, E>, E> extends RegularInventoryMenuLayoutImpl<V> implements PagedInventoryMenuLayout<V> {

    @Nullable
    private final PagedLayout<V> customLayoutOrder;
    private final int objectsPerPageCount;

    private PagedInventoryMenuLayoutImpl(Builder<V, E> builder) {
        super(builder);
        this.customLayoutOrder = builder.layoutOrder;
        this.objectsPerPageCount = builder.layoutOrder == null ? countPagedButtons(buttons) : builder.layoutOrder.getObjectsPerPageCount();
        if (this.customLayoutOrder != null) {
            // Update button indexes with the custom layout order
            PagedLayout.MenuButtonsIterator<V> buttonsIterator = this.customLayoutOrder.createIterator(this.buttons);
            int buttonIndex = 0;

            while (buttonsIterator.hasNext()) {
                buttonsIterator.next();

                for (int slot : buttonsIterator.getSlots()) {
                    MenuTemplateButton<V> templateButton = this.buttons[slot];

                    if (templateButton instanceof PagedMenuTemplateButton) {
                        ((PagedMenuTemplateButton<V, ?>) templateButton).setButtonIndex(buttonIndex);
                    }
                }

                buttonIndex++;
            }
        }
    }

    @Override
    public int getObjectsPerPageCount() {
        return this.objectsPerPageCount;
    }

    @Override
    protected void populateInventory(Inventory inventory, V menuView) {
        MutableInt pagedObjectSlot = new MutableInt(0);

        // Set all regular buttons in the menu
        for (int slot = 0; slot < this.buttons.length; ++slot) {
            MenuViewButton<V> button = this.buttons[slot].createViewButton(menuView);

            if (this.customLayoutOrder != null && button instanceof PagedMenuViewButton) {
                continue;
            }

            ItemStack buttonItem = createButtonItem(button, menuView, pagedObjectSlot);

            if (buttonItem != null) {
                inventory.setItem(slot, buttonItem);
            }
        }

        if (this.customLayoutOrder == null) {
            return;
        }

        PagedLayout.MenuButtonsIterator<V> buttonsIterator = this.customLayoutOrder.createIterator(this.buttons);

        while (buttonsIterator.hasNext()) {
            MenuTemplateButton<V> templateButton = buttonsIterator.next();

            if (!(templateButton instanceof PagedMenuTemplateButton)) {
                continue;
            }

            MenuViewButton<V> button = templateButton.createViewButton(menuView);
            ItemStack buttonItem = createButtonItem(button, menuView, pagedObjectSlot);

            if (buttonItem == null) {
                continue;
            }

            for (int slot : buttonsIterator.getSlots()) {
                inventory.setItem(slot, buttonItem);
            }
        }
    }

    private ItemStack createButtonItem(MenuViewButton<V> button, PagedMenuView<V, ?, E> menuView, MutableInt pagedObjectSlot) {
        int currentPage = menuView.getCurrentPage();
        List<E> pagedObjects = menuView.getPagedObjects();

        if (button instanceof PagedMenuViewButton) {
            PagedMenuViewButton<V, E> pagedMenuButton = (PagedMenuViewButton<V, E>) button;
            int objectIndex = pagedObjectSlot.get() + (this.objectsPerPageCount * (currentPage - 1));

            pagedObjectSlot.set(pagedObjectSlot.get() + 1);

            if (objectIndex >= pagedObjects.size()) {
                return ((PagedMenuTemplateButton<V, E>) pagedMenuButton.getTemplate()).getNullItem();
            }

            pagedMenuButton.updateObject(pagedObjects.get(objectIndex));
        }

        ItemStack buttonItem;

        try {
            buttonItem = button.createViewItem();
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while setting up menu:");
            return null;
        }

        if (buttonItem == null) {
            return null;
        }

        if (button instanceof PreviousPageButton) {
            return new ItemBuilder(buttonItem)
                    .replaceAll("{0}", (currentPage == 1 ? "&c" : "&a"))
                    .build(menuView.getInventoryViewer());
        } else if (button instanceof NextPageButton) {
            return new ItemBuilder(buttonItem)
                    .replaceAll("{0}", (pagedObjects.size() > currentPage * this.objectsPerPageCount ? "&a" : "&c"))
                    .build(menuView.getInventoryViewer());
        } else if (button instanceof CurrentPageButton) {
            return new ItemBuilder(buttonItem)
                    .replaceAll("{0}", currentPage + "")
                    .build(menuView.getInventoryViewer());
        }

        return buttonItem;
    }

    private static int countPagedButtons(MenuTemplateButton<?>[] buttons) {
        return (int) Arrays.stream(buttons).filter(button -> button instanceof PagedMenuTemplateButton).count();
    }

    public static class Builder<V extends PagedMenuView<V, ?, E>, E>
            extends RegularInventoryMenuLayoutImpl.AbstractBuilder<V, Builder<V, E>>
            implements PagedInventoryMenuLayout.Builder<V, E> {

        @Nullable
        private PagedLayout<V> layoutOrder;

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
        public Builder<V, E> setCustomLayoutOrder(List<Integer> slotsOrder) {
            List<List<Integer>> validSlotsLayout = new ArrayList<>();

            for (int slot : slotsOrder) {
                if (slot >= 0 && slot < super.buttons.length &&
                        super.buttons[slot] instanceof PagedMenuTemplateButton) {
                    validSlotsLayout.add(Collections.singletonList(slot));
                }
            }

            if (!validSlotsLayout.isEmpty()) {
                this.layoutOrder = new CustomPagedLayout<>(validSlotsLayout);
            }

            return this;
        }

        @Override
        public Builder<V, E> setCustomLayout(List<List<Integer>> slotsLayout) {
            List<List<Integer>> validSlotsLayout = new ArrayList<>();

            for (List<Integer> slots : slotsLayout) {
                List<Integer> validSlots = new ArrayList<>();

                for (int slot : slots) {
                    if (slot >= 0 && slot < super.buttons.length &&
                            super.buttons[slot] instanceof PagedMenuTemplateButton) {
                        validSlots.add(slot);
                    }
                }

                if (!validSlots.isEmpty()) {
                    validSlotsLayout.add(validSlots);
                }
            }

            if (!validSlotsLayout.isEmpty()) {
                this.layoutOrder = new CustomPagedLayout<>(validSlotsLayout);
            }

            return this;
        }

        @Override
        public PagedInventoryMenuLayout<V> build() {
            return new PagedInventoryMenuLayoutImpl<>(this);
        }

    }

}
