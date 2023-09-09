package com.bgsoftware.superiorskyblock.api.player.container;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.List;
import java.util.UUID;

public interface PlayersContainer {

    /**
     * Get a player by its name.
     *
     * @param name The name of the player.
     */
    @Nullable
    SuperiorPlayer getSuperiorPlayer(String name);

    /**
     * Get a player by its uuid.
     *
     * @param uuid The uuid of the player.
     */
    @Nullable
    SuperiorPlayer getSuperiorPlayer(UUID uuid);

    /**
     * Get all the players.
     */
    List<SuperiorPlayer> getAllPlayers();

    /**
     * Add a player to the container.
     *
     * @param superiorPlayer The player to add.
     */
    void addPlayer(SuperiorPlayer superiorPlayer);

    /**
     * Remove a player from the container.
     *
     * @param superiorPlayer The player to remove.
     */
    void removePlayer(SuperiorPlayer superiorPlayer);

}
