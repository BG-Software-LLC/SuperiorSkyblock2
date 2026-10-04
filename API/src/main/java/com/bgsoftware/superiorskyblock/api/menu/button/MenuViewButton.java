package com.bgsoftware.superiorskyblock.api.menu.button;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton.MenuViewButtonCreator;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * The {@link MenuViewButton} is used to run custom logic for buttons inside menu views.
 * You will want to implement this interface with your own custom logic, and then later use your custom
 * view button with the {@link MenuViewButtonCreator}.
 */
public interface MenuViewButton<V extends MenuView<V, ?>> {

    /**
     * Get the template that was used to create this view button.
     */
    MenuTemplateButton<V> getTemplate();

    /**
     * Get the view that this button is used in.
     */
    V getView();

    /**
     * Create a new view item for this button.
     * This should use {@link MenuTemplateButton#getButtonItem()}
     */
    ItemStack createViewItem();

    /**
     * Method callback when clicking this button from an inventory menu.
     *
     * @param clickEvent The click event.
     * @deprecated Use {@link #onButtonClick(ButtonClickContext)} instead.
     */
    @Deprecated
    void onButtonClick(InventoryClickEvent clickEvent);


    /**
     * Method callback when clicking this button, from either an inventory menu or a Dialog UI.
     *
     * @param context The click context. See {@link ButtonClickContext} for more info.
     */
    void onButtonClick(ButtonClickContext<V> context);

}
