package com.bgsoftware.superiorskyblock.api.menu.button.click;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/**
 * Represents the context of a button click, either from an inventory menu or a dialog.
 */
public interface ButtonClickContext<V extends MenuView<V, ?>> {

    /**
     * Get the player who triggered this button click.
     */
    Player getPlayer();

    /**
     * Get the menu view the click was happening in.
     */
    V getMenuView();

    /**
     * Get the slot the player clicked in the menu.
     */
    int getClickedSlot();

    /**
     * Get the clicked button.
     */
    MenuTemplateButton<V> getClickedButton();

    /**
     * Get the click type the player did.
     *
     * In dialogs, this will always return {@link ClickType#LEFT}
     */
    ClickType getClickType();

}
