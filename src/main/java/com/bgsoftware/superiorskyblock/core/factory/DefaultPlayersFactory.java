package com.bgsoftware.superiorskyblock.core.factory;

import com.bgsoftware.superiorskyblock.api.factory.PlayersFactory;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;
import com.bgsoftware.superiorskyblock.api.player.algorithm.PlayerTeleportAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

public class DefaultPlayersFactory implements PlayersFactory {

    private static final DefaultPlayersFactory INSTANCE = new DefaultPlayersFactory();

    public static DefaultPlayersFactory getInstance() {
        return INSTANCE;
    }

    private DefaultPlayersFactory() {
    }

    @Override
    public SuperiorPlayer createPlayer(SuperiorPlayer original) {
        return original;
    }

    @Override
    public PlayerTeleportAlgorithm createPlayerTeleportAlgorithm(SuperiorPlayer superiorPlayer, PlayerTeleportAlgorithm original) {
        return original;
    }

    @Override
    public PersistentDataContainer createPersistentDataContainer(SuperiorPlayer superiorPlayer, PersistentDataContainer original) {
        return original;
    }

}
