package com.bgsoftware.superiorskyblock.api.factory;

import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;
import com.bgsoftware.superiorskyblock.api.player.algorithm.PlayerTeleportAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

public class DelegatePlayersFactory implements PlayersFactory {

    protected final PlayersFactory handle;

    protected DelegatePlayersFactory(PlayersFactory handle) {
        this.handle = handle;
    }

    @Override
    public SuperiorPlayer createPlayer(SuperiorPlayer original) {
        return this.handle.createPlayer(original);
    }

    @Override
    @Deprecated
    public PlayerTeleportAlgorithm createPlayerTeleportAlgorithm(SuperiorPlayer superiorPlayer) {
        return this.handle.createPlayerTeleportAlgorithm(superiorPlayer);
    }

    @Override
    public PlayerTeleportAlgorithm createPlayerTeleportAlgorithm(SuperiorPlayer superiorPlayer, PlayerTeleportAlgorithm original) {
        return this.handle.createPlayerTeleportAlgorithm(superiorPlayer, original);
    }

    @Override
    public PersistentDataContainer createPersistentDataContainer(SuperiorPlayer superiorPlayer, PersistentDataContainer original) {
        return this.handle.createPersistentDataContainer(superiorPlayer, original);
    }

}
