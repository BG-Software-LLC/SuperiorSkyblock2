package com.bgsoftware.superiorskyblock.api.factory;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

public interface PlayersFactory {

    /**
     * Create a new player wrapper.
     * @param original The original player wrapper that was created.
     */
    SuperiorPlayer createPlayer(SuperiorPlayer original);

}
