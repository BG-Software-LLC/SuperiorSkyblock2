package com.bgsoftware.superiorskyblock.api.menu;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.inventory.InventoryHolder;

import javax.annotation.Nullable;

public interface ISuperiorMenu extends InventoryHolder {

    SuperiorPlayer getInventoryViewer();

    void setPreviousMove(boolean previousMove);

    void cloneAndOpen(@Nullable ISuperiorMenu previousMenu);

    @Nullable
    ISuperiorMenu getPreviousMenu();

}
