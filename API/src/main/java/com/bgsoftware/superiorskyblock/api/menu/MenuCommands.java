package com.bgsoftware.superiorskyblock.api.menu;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Singleton class used to parse and execute commands from menus.
 * You can get the instance of this executor by calling {@link #getInstance()}
 */
public interface MenuCommands {

    /**
     * @deprecated Use {@link #runCommand(MenuView, String, ButtonClickContext)} instead.
     */
    @Deprecated
    void runCommand(MenuView<?, ?> menuView, String command, InventoryClickEvent clickEvent);

    /**
     * Execute a single menu command in the context of a button click.
     *
     * @param menuView The view the command is executed from.
     * @param command  The raw command string from menu config.
     * @param context  The button click context (menu or dialog).
     */
    void runCommand(MenuView<?, ?> menuView, String command, ButtonClickContext<?> context);

    static MenuCommands getInstance() {
        return SuperiorSkyblockAPI.getMenus().getMenuCommands();
    }

}
