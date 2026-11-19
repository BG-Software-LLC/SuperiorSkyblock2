package com.bgsoftware.superiorskyblock.core.menu.layout;

import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.layout.PagedHologramMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.PagedMenuView;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.CurrentPageButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.NextPageButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.PreviousPageButton;

import java.util.Arrays;
import java.util.List;

public class PagedHologramMenuLayoutImpl<V extends PagedMenuView<V, ?, E>, E> extends RegularHologramMenuLayoutImpl<V>
        implements PagedHologramMenuLayout<V> {

    private final int objectsPerPageCount;

    private PagedHologramMenuLayoutImpl(Builder<V, E> builder) {
        super(builder);
        this.objectsPerPageCount = (int) Arrays.stream(this.buttons)
                .filter(button -> button instanceof PagedMenuTemplateButton)
                .count();
    }

    @Override
    public int getObjectsPerPageCount() {
        return this.objectsPerPageCount;
    }

    public static class Builder<V extends PagedMenuView<V, ?, E>, E>
            extends RegularHologramMenuLayoutImpl.AbstractBuilder<V, Builder<V, E>>
            implements PagedHologramMenuLayout.Builder<V, E> {

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
            // Paged objects are shown in the order of the pattern of hologram menus.
            return this;
        }

        @Override
        public Builder<V, E> setCustomLayout(List<List<Integer>> slotsLayout) {
            // Paged objects are shown in the order of the pattern of hologram menus.
            return this;
        }

        @Override
        public PagedHologramMenuLayoutImpl<V, E> build() {
            return new PagedHologramMenuLayoutImpl<>(this);
        }

    }

}
