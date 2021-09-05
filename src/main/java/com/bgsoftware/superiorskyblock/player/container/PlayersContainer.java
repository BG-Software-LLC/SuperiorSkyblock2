package com.bgsoftware.superiorskyblock.player.container;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface PlayersContainer {

    @Nullable
    SuperiorPlayer getSuperiorPlayer(String name);

    @Nullable
    SuperiorPlayer getSuperiorPlayer(UUID uuid);

    List<SuperiorPlayer> getAllPlayers();

    void addPlayer(SuperiorPlayer superiorPlayer);

    void removePlayer(SuperiorPlayer superiorPlayer);

}
