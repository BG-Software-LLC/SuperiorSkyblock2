package com.bgsoftware.superiorskyblock.core.menu.view.args;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.view.ViewArgs;

public class IslandViewArgs implements ViewArgs {

    private final Island island;

    public IslandViewArgs(Island island) {
        this.island = island;
    }

    public Island getIsland() {
        return island;
    }

}
