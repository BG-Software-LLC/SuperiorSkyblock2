package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.UUID;

public interface PlayersManager {

    SuperiorPlayer getSuperiorPlayer(String name);

    SuperiorPlayer getSuperiorPlayer(UUID uuid);




}
