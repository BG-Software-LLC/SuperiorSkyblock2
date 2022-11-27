package com.bgsoftware.superiorskyblock.api.menu;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Singleton class used to parse and execute commands from menus.
 * You can get the instance of this executor by calling {@link #getInstance()}
 */
public interface MenuCommands {

    void runCommand(MenuView<?, ?> menuView, String command, InventoryClickEvent clickEvent);

    static MenuCommands getInstance() {
        return SuperiorSkyblockAPI.getMenus().getMenuCommands();
    }

}
