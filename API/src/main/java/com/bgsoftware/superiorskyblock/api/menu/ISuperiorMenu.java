package com.bgsoftware.superiorskyblock.api.menu;

import org.bukkit.inventory.InventoryHolder;

import javax.annotation.Nullable;

public interface ISuperiorMenu extends InventoryHolder {

    void setPreviousMove(boolean previousMove);

    void cloneAndOpen(@Nullable ISuperiorMenu previousMenu);

    @Nullable
    ISuperiorMenu getPreviousMenu();

}
