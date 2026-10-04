package com.bgsoftware.superiorskyblock.api.menu.layout;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.PagedMenuView;

/**
 * Similar to {@link InventoryMenuLayout}, but used for layout of page-based menus.
 * See {@link InventoryMenuLayout}
 */
public interface PagedInventoryMenuLayout<V extends MenuView<V, ?>> extends InventoryMenuLayout<V>, PagedMenuLayout<V> {

    /**
     * Create a new {@link Builder} object for a new {@link PagedInventoryMenuLayout}.
     */
    static <V extends PagedMenuView<V, ?, E>, E> Builder<V, E> newBuilder() {
        return SuperiorSkyblockAPI.getMenus().createInventoryPagedLayoutBuilder();
    }

    interface Builder<V extends MenuView<V, ?>, E> extends InventoryMenuLayout.Builder<V>, PagedMenuLayout.Builder<V, E> {

        /**
         * Get the {@link PagedInventoryMenuLayout} from this builder.
         */
        @Override
        PagedInventoryMenuLayout<V> build();

    }

}
