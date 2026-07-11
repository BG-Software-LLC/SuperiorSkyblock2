package com.bgsoftware.superiorskyblock.api.menu.layout;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.PagedMenuView;

/**
 * Similar to {@link DialogMenuLayout}, but used for layout of page-based menus.
 * See {@link DialogMenuLayout}
 */
public interface PagedDialogMenuLayout<V extends MenuView<V, ?>> extends DialogMenuLayout<V>, PagedMenuLayout<V> {

    /**
     * Create a new {@link Builder} object for a new {@link PagedDialogMenuLayout}.
     */
    static <V extends PagedMenuView<V, ?, E>, E> Builder<V, E> newBuilder() {
        return SuperiorSkyblockAPI.getMenus().createDialogPagedLayoutBuilder();
    }

    interface Builder<V extends MenuView<V, ?>, E> extends DialogMenuLayout.Builder<V>, PagedMenuLayout.Builder<V, E> {

        /**
         * Get the {@link PagedDialogMenuLayout} from this builder.
         */
        @Override
        PagedDialogMenuLayout<V> build();

    }

}
