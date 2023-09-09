package com.bgsoftware.superiorskyblock.core.menu.view;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.view.args.IslandViewArgs;

public class IslandMenuView extends AbstractMenuView<IslandMenuView, IslandViewArgs> {

    private final Island island;

    public IslandMenuView(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
                          Menu<IslandMenuView, IslandViewArgs> menu, IslandViewArgs args) {
        super(inventoryViewer, previousMenuView, menu);
        this.island = args.getIsland();
    }

    public Island getIsland() {
        return island;
    }

}
