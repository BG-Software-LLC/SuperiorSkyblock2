package com.bgsoftware.superiorskyblock.api.menu.button;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import org.bukkit.inventory.ItemStack;

/**
 * Similar to {@link MenuTemplateButton}, but used for buttons in page-based menus.
 * See {@link MenuTemplateButton}
 */
public interface PagedMenuTemplateButton<V extends MenuView<V, ?>, E> extends MenuTemplateButton<V> {

    /**
     * Get the item to display inside the menu for items that don't have a valid paged-object.
     * For example, inside the top-islands menu, this will return the question-mark head (by default).
     * In most cases, this will return null.
     */
    @Nullable
    ItemStack getNullItem();

    /**
     * Get the index within the menu of this paged object button.
     */
    int getButtonIndex();

    /**
     * Create a view-button object to be used by the provided menu view.
     * Unlike the template button, the view button handles the logic for buttons within the view.
     *
     * @param menuView The view to create the button for.
     */
    @Override
    PagedMenuViewButton<V, E> createViewButton(V menuView);

    /**
     * Create a new {@link Builder} object for a new {@link PagedMenuTemplateButton}.
     *
     * @param viewButtonCreator The creator used to create a view-button by the builder.
     *                          You will probably want to implement your own {@link PagedMenuViewButton} which will be
     *                          returned by this creator.
     */
    static <V extends MenuView<V, ?>, E> Builder<V, E> newBuilder(Class<?> viewButtonType, PagedMenuViewButtonCreator<V, E> viewButtonCreator) {
        return SuperiorSkyblockAPI.getMenus().createPagedButtonBuilder(viewButtonType, viewButtonCreator);
    }

    interface Builder<V extends MenuView<V, ?>, E> extends MenuTemplateButton.Builder<V> {

        /**
         * Set the item to display inside the menu for items that don't have a valid paged-object.
         *
         * @param nullItem The item to set.
         */
        Builder<V, E> setNullItem(ItemStack nullItem);

        /**
         * Get the {@link PagedMenuTemplateButton} from this builder.
         */
        PagedMenuTemplateButton<V, E> build();

    }

    interface PagedMenuViewButtonCreator<V extends MenuView<V, ?>, E> {

        /**
         * Create a new {@link PagedMenuViewButton}.
         * You will probably want to implement your own {@link PagedMenuViewButton} which will be
         * returned by this creator.
         */
        PagedMenuViewButton<V, E> create(PagedMenuTemplateButton<V, E> templateButton, V menuView);

    }

}
