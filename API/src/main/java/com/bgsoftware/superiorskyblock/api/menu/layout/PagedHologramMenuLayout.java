package com.bgsoftware.superiorskyblock.api.menu.layout;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.PagedMenuView;

/**
 * The layout of a hologram menu that shows objects in pages.
 * When the page changes, the objects of the old page slide out of the menu and the objects of the new page
 * slide into it, see {@link com.bgsoftware.superiorskyblock.api.menu.hologram.HologramMenuStyle#getPageAnimationDuration()}.
 * Custom layouts and custom orders of paged objects are not supported by hologram menus.
 */
public interface PagedHologramMenuLayout<V extends MenuView<V, ?>> extends HologramMenuLayout<V>, PagedMenuLayout<V> {

    /**
     * Create a new {@link Builder} object for a new {@link PagedHologramMenuLayout}.
     */
    static <V extends PagedMenuView<V, ?, E>, E> Builder<V, E> newBuilder() {
        return SuperiorSkyblockAPI.getMenus().createHologramPagedLayoutBuilder();
    }

    interface Builder<V extends MenuView<V, ?>, E> extends HologramMenuLayout.Builder<V>, PagedMenuLayout.Builder<V, E> {

        @Override
        PagedHologramMenuLayout<V> build();

    }

}
