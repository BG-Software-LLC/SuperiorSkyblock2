package com.bgsoftware.superiorskyblock.api;

import com.bgsoftware.superiorskyblock.api.handlers.BlockValuesManager;
import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.handlers.KeysManager;
import com.bgsoftware.superiorskyblock.api.handlers.MenusManager;
import com.bgsoftware.superiorskyblock.api.handlers.MissionsManager;
import com.bgsoftware.superiorskyblock.api.handlers.PlayersManager;
import com.bgsoftware.superiorskyblock.api.handlers.SchematicManager;

public interface SuperiorSkyblock {

    /**
     * Get the grid of the core.
     */
    GridManager getGrid();

    /**
     * Get the blocks manager of the core.
     */
    BlockValuesManager getBlockValues();

    /**
     * Get the schematics manager of the core.
     */
    SchematicManager getSchematics();

    /**
     * Get the players manager of the core.
     */
    PlayersManager getPlayers();

    /**
     * Get the missions manager of the core.
     */
    MissionsManager getMissions();

    /**
     * Get the menus manager of the core.
     */
    MenusManager getMenus();

    /**
     * Get the keys manager of the core.
     */
    KeysManager getKeys();

}
