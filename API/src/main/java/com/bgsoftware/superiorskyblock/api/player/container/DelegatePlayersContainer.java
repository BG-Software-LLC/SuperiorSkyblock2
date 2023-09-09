package com.bgsoftware.superiorskyblock.api.player.container;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.List;
import java.util.UUID;

public class DelegatePlayersContainer implements PlayersContainer {

    protected final PlayersContainer handle;

    protected DelegatePlayersContainer(PlayersContainer handle) {
        this.handle = handle;
    }

    @Nullable
    @Override
    public SuperiorPlayer getSuperiorPlayer(String name) {
        return this.handle.getSuperiorPlayer(name);
    }

    @Nullable
    @Override
    public SuperiorPlayer getSuperiorPlayer(UUID uuid) {
        return this.handle.getSuperiorPlayer(uuid);
    }

    @Override
    public List<SuperiorPlayer> getAllPlayers() {
        return this.handle.getAllPlayers();
    }

    @Override
    public void addPlayer(SuperiorPlayer superiorPlayer) {
        this.handle.addPlayer(superiorPlayer);
    }

    @Override
    public void removePlayer(SuperiorPlayer superiorPlayer) {
        this.handle.removePlayer(superiorPlayer);
    }

}
