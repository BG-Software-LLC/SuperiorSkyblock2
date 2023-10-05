package com.bgsoftware.superiorskyblock.api.menu.layout;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.PagedMenuView;

import java.util.List;

/**
 * Similar to {@link MenuLayout}, but used for layout of page-based menus.
 * See {@link MenuLayout}
 */
public interface PagedMenuLayout<V extends MenuView<V, ?>> extends MenuLayout<V> {

    /**
     * Get the amount of paged objects in the layout.
     */
    int getObjectsPerPageCount();

    /**
     * Create a new {@link Builder} object for a new {@link PagedMenuLayout}.
     */
    static <V extends PagedMenuView<V, ?, E>, E> Builder<V, E> newBuilder() {
        return SuperiorSkyblockAPI.getMenus().createPagedPatternBuilder();
    }

    interface Builder<V extends MenuView<V, ?>, E> extends MenuLayout.Builder<V> {

        /**
         * Set the previous-page button slots for this layout.
         *
         * @param slots The slots to set.
         */
        Builder<V, E> setPreviousPageSlots(List<Integer> slots);

        /**
         * Set the next-page button slots for this layout.
         *
         * @param slots The slots to set.
         */
        Builder<V, E> setNextPageSlots(List<Integer> slots);

        /**
         * Set the current-page display button slots for this layout.
         *
         * @param slots The slots to set.
         */
        Builder<V, E> setCurrentPageSlots(List<Integer> slots);

        /**
         * Set the page-object button slots for this layout.
         *
         * @param slots         The slots to set.
         * @param buttonBuilder The builder used for the paged-object.
         */
        Builder<V, E> setPagedObjectSlots(List<Integer> slots, PagedMenuTemplateButton.Builder<V, E> buttonBuilder);

        /**
         * Set a custom order for the paged objects for this layout.
         *
         * @param slotsOrder The correct order of the slots
         */
        Builder<V, E> setCustomLayoutOrder(List<Integer> slotsOrder);

        /**
         * Get the {@link PagedMenuLayout} from this builder.
         */
        @Override
        PagedMenuLayout<V> build();

    }

}
