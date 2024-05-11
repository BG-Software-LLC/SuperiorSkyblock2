package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.player.container.PlayersContainer;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface PlayersManager {

    /**
     * Get a player by it's name.
     *
     * @param name The name to check.
     * @return The player with that name.
     */
    @Nullable
    SuperiorPlayer getSuperiorPlayer(String name);

    /**
     * Get a player by a player.
     *
     * @param player The player to check.
     */
    SuperiorPlayer getSuperiorPlayer(Player player);

    /**
     * Get a player by it's uuid.
     *
     * @param uuid The uuid to check.
     * @return The player with that uuid.
     */
    SuperiorPlayer getSuperiorPlayer(UUID uuid);

    /**
     * Get all the players that joined the server.
     */
    List<SuperiorPlayer> getAllPlayers();

    /**
     * Get a player role by it's weight.
     *
     * @param weight The weight to check.
     * @return The player role with that weight.
     */
    @Nullable
    @Deprecated
    PlayerRole getPlayerRole(int weight);

    /**
     * Get a player role by it's id.
     *
     * @param id The id to check.
     * @return The player role with that weight.
     */
    @Nullable
    @Deprecated
    PlayerRole getPlayerRoleFromId(int id);

    /**
     * Get a player role by it's name.
     *
     * @param name The name to check.
     * @return The player role with that name.
     * If there's no role with that name, IllegalArgumentException will be thrown.
     * @deprecated see {@link RolesManager}
     */
    @Deprecated
    PlayerRole getPlayerRole(String name);

    /**
     * Get the default role that players are assigned with when they join an island.
     *
     * @deprecated see {@link RolesManager}
     */
    @Deprecated
    PlayerRole getDefaultRole();

    /**
     * Get the highest role in the ladder - aka, leader's role.
     *
     * @deprecated see {@link RolesManager}
     */
    @Deprecated
    PlayerRole getLastRole();

    /**
     * Get the guest's role.
     *
     * @deprecated see {@link RolesManager}
     */
    @Deprecated
    PlayerRole getGuestRole();

    /**
     * Get the co-op's role.
     *
     * @deprecated see {@link RolesManager}
     */
    @Deprecated
    PlayerRole getCoopRole();

    /**
     * Get a list of all the roles.
     *
     * @deprecated see {@link RolesManager}
     */
    @Deprecated
    List<PlayerRole> getRoles();

    /**
     * Get the players container.
     */
    PlayersContainer getPlayersContainer();

    /**
     * Set a new players container.
     *
     * @param playersContainer The new players container to set.
     */
    void setPlayersContainer(PlayersContainer playersContainer);

}
