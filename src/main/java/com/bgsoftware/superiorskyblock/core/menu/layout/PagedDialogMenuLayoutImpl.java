package com.bgsoftware.superiorskyblock.core.menu.layout;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogMenuType;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.layout.PagedDialogMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.layout.PagedInventoryMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.layout.PagedMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.PagedMenuView;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.CurrentPageButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.NextPageButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.PreviousPageButton;
import com.bgsoftware.superiorskyblock.core.menu.layout.order.CustomPagedLayoutOrder;
import com.bgsoftware.superiorskyblock.core.menu.layout.order.PagedLayoutOrder;
import org.bukkit.event.inventory.InventoryType;

import java.util.List;

public class PagedDialogMenuLayoutImpl<V extends PagedMenuView<V, ?, E>, E> extends RegularDialogMenuLayoutImpl<V> implements PagedDialogMenuLayout<V> {

    private PagedDialogMenuLayoutImpl(Builder<V, E> builder) {
        super(builder);
    }

    @Override
    public int getObjectsPerPageCount() {
        return this.buttons.length;
    }

    public static class Builder<V extends PagedMenuView<V, ?, E>, E>
            extends RegularDialogMenuLayoutImpl.AbstractBuilder<V, Builder<V, E>>
            implements PagedDialogMenuLayout.Builder<V, E> {

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
        public Builder<V, E> setCustomLayoutOrder(List<Integer> slotsOrder) {
            slotsOrder.removeIf(slot -> !(super.buttons[slot] instanceof PagedMenuTemplateButton));
            if (!slotsOrder.isEmpty())
                this.layoutOrder = new CustomPagedLayoutOrder<>(slotsOrder);
            return this;
        }

        @Override
        public PagedDialogMenuLayoutImpl<V, E> build() {
            return new PagedDialogMenuLayoutImpl<>(this);
        }

    }

}
