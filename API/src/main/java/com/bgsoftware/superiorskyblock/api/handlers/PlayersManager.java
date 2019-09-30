package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.List;
import java.util.UUID;

public interface PlayersManager {

    SuperiorPlayer getSuperiorPlayer(String name);

    SuperiorPlayer getSuperiorPlayer(UUID uuid);

    PlayerRole getPlayerRole(int index);

    PlayerRole getPlayerRole(String name);

    PlayerRole getDefaultRole();

    PlayerRole getLastRole();

    PlayerRole getGuestRole();

    List<PlayerRole> getRoles();
}
