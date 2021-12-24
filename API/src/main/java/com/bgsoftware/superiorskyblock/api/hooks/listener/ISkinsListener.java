package com.bgsoftware.superiorskyblock.api.hooks.listener;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

/**
 * Listener for changes of skins of players.
 */
public interface ISkinsListener {

    /**
     * Update the skin of a player.
     *
     * @param superiorPlayer The player to update the skin for.
     */
    void setSkinTexture(SuperiorPlayer superiorPlayer);

}
