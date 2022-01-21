package com.bgsoftware.superiorskyblock.api.factory;

import com.bgsoftware.superiorskyblock.api.player.algorithm.PlayerTeleportAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

public interface PlayersFactory {

    /**
     * Create a new player wrapper.
     *
     * @param original The original player wrapper that was created.
     */
    SuperiorPlayer createPlayer(SuperiorPlayer original);

    /**
     * Create a teleport algorithm for a player.
     *
     * @param superiorPlayer The player to set the algorithm to.
     */
    PlayerTeleportAlgorithm createPlayerTeleportAlgorithm(SuperiorPlayer superiorPlayer);

}
