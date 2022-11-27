package com.bgsoftware.superiorskyblock.api.menu.button;

import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import org.bukkit.inventory.ItemStack;

public interface PagedMenuViewButton<V extends MenuView<V, ?>, E> extends MenuViewButton<V> {

    /**
     * Set the paged object for this view button.
     *
     * @param pagedObject The object to set.
     */
    void updateObject(E pagedObject);

    /**
     * Get the paged object for this view button.
     */
    E getPagedObject();

    /**
     * Modify the button item for this view button.
     * This is used for parsing additional placeholders for the {@link #getPagedObject()}.
     *
     * @param buttonItem The original item, created by {@link PagedMenuTemplateButton#createViewButton(MenuView)}
     */
    ItemStack modifyViewItem(ItemStack buttonItem);

}
