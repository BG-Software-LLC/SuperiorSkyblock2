package com.bgsoftware.superiorskyblock.core.menu.view.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.IIslandMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.IslandViewArgs;

public class IslandMenuView extends AbstractMenuView<IslandMenuView, IslandViewArgs> implements IIslandMenuView {

    private final Island island;

    public IslandMenuView(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
                          Menu<IslandMenuView, IslandViewArgs> menu, IslandViewArgs args) {
        super(inventoryViewer, previousMenuView, menu);
        this.island = args.getIsland();
    }

    @Override
    public Island getIsland() {
        return island;
    }

    @Override
    public String replaceTitle(String title) {
        return title.replace("{}", this.island.getName());
    }

}
