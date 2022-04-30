package com.bgsoftware.superiorskyblock.api.menu;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.inventory.InventoryHolder;

import javax.annotation.Nullable;

public interface ISuperiorMenu extends InventoryHolder {

    /**
     * Get the player currently viewing the menu.
     */
    SuperiorPlayer getInventoryViewer();

    /**
     * Set whether closing the menu should open the previous menu.
     */
    void setPreviousMove(boolean previousMove);

    /**
     * Clone and open this menu to the {@link #getInventoryViewer()}
     *
     * @param previousMenu The previous menu to set.
     */
    void cloneAndOpen(@Nullable ISuperiorMenu previousMenu);

    /**
     * Get the previous menu of this menu.
     */
    @Nullable
    ISuperiorMenu getPreviousMenu();

}
