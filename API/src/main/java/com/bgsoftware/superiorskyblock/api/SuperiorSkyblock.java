package com.bgsoftware.superiorskyblock.api;

import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.handlers.PlayersManager;
import com.bgsoftware.superiorskyblock.api.handlers.SchematicManager;

public interface SuperiorSkyblock {

    GridManager getGrid();

    SchematicManager getSchematics();

    PlayersManager getPlayers();

}
