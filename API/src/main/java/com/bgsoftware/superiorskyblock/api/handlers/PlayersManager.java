package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface PlayersManager {

    /**
     * Get a player by it's name.
     * @param name The name to check.
     * @return The player with that name.
     */
    @Nullable
    SuperiorPlayer getSuperiorPlayer(@Nullable String name);

    /**
     * Get a player by a player.
     * @param player The player to check.
     */
    SuperiorPlayer getSuperiorPlayer(Player player);

    /**
     * Get a player by it's uuid.
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
     * @param weight The weight to check.
     * @return The player role with that weight.
     */
    @Nullable
    PlayerRole getPlayerRole(int weight);

    /**
     * Get a player role by it's id.
     * @param id The id to check.
     * @return The player role with that weight.
     */
    @Nullable
    PlayerRole getPlayerRoleFromId(int id);

    /**
     * Get a player role by it's name.
     * @param name The name to check.
     * @return The player role with that name.
     * If there's no role with that name, IllegalArgumentException will be thrown.
     */
    PlayerRole getPlayerRole(String name);

    /**
     * Get the default role that players are assigned with when they join an island.
     */
    PlayerRole getDefaultRole();

    /**
     * Get the highest role in the ladder - aka, leader's role.
     */
    PlayerRole getLastRole();

    /**
     * Get the guest's role.
     */
    PlayerRole getGuestRole();

    /**
     * Get the co-op's role.
     */
    PlayerRole getCoopRole();

    /**
     * Get a list of all the roles.
     */
    List<PlayerRole> getRoles();
}
