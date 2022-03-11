package com.bgsoftware.superiorskyblock.api.player.algorithm;

import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public interface PlayerTeleportAlgorithm {

    /**
     * Teleport a player to another location.
     *
     * @param player   The player to teleport.
     * @param location The location to teleport the player to.
     * @return CompletableFuture with boolean that indicates whether the teleportation was successful.
     */
    CompletableFuture<Boolean> teleport(Player player, Location location);

    /**
     * Teleport a player to an island.
     *
     * @param player The player to teleport.
     * @param island The island to teleport the player to.
     * @return CompletableFuture with boolean that indicates whether the teleportation was successful.
     */
    CompletableFuture<Boolean> teleport(Player player, Island island);

    /**
     * Teleport a player to an island in a specific environment.
     *
     * @param player      The player to teleport.
     * @param island      The island to teleport the player to.
     * @param environment The environment to teleport the player to.
     * @return CompletableFuture with boolean that indicates whether the teleportation was successful.
     */
    CompletableFuture<Boolean> teleport(Player player, Island island, World.Environment environment);

}
