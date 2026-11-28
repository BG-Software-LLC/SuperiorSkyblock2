package com.bgsoftware.superiorskyblock.core.menu.layout;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.layout.PagedDialogMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.PagedMenuView;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.CurrentPageButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.NextPageButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.PreviousPageButton;
import com.bgsoftware.superiorskyblock.core.menu.layout.custom.CustomPagedLayout;
import com.bgsoftware.superiorskyblock.core.menu.layout.custom.PagedLayout;

import java.util.ArrayList;
import java.util.Collections;
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
        public PagedDialogMenuLayoutImpl<V, E> build() {
            return new PagedDialogMenuLayoutImpl<>(this);
        }

    }

}
