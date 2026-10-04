package com.bgsoftware.superiorskyblock.api.player.algorithm;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import org.bukkit.Location;
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
    default CompletableFuture<Boolean> teleport(Player player, Location location) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        teleportWithResult(player, location).whenComplete((value, error) -> {
            if(error != null) {
                result.completeExceptionally(error);
            } else {
                result.complete(value == TeleportResult.SUCCESS);
            }
        });
        return result;
    }

    /**
     * Teleport a player to another location.
     *
     * @param player   The player to teleport.
     * @param location The location to teleport the player to.
     * @return CompletableFuture with the result of the teleportation.
     */
    CompletableFuture<TeleportResult> teleportWithResult(Player player, Location location);

    /**
     * Teleport a player to an island.
     *
     * @param player The player to teleport.
     * @param island The island to teleport the player to.
     * @return CompletableFuture with boolean that indicates whether the teleportation was successful.
     */
    default CompletableFuture<Boolean> teleport(Player player, Island island) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        teleportWithResult(player, island).whenComplete((value, error) -> {
            if(error != null) {
                result.completeExceptionally(error);
            } else {
                result.complete(value == TeleportResult.SUCCESS);
            }
        });
        return result;
    }

    /**
     * Teleport a player to an island.
     *
     * @param player The player to teleport.
     * @param island The island to teleport the player to.
     * @return CompletableFuture with the result of the teleportation.
     */
    CompletableFuture<TeleportResult> teleportWithResult(Player player, Island island);

    /**
     * Teleport a player to an island in a specific dimension.
     *
     * @param player    The player to teleport.
     * @param island    The island to teleport the player to.
     * @param dimension The dimension to teleport the player to.
     * @return CompletableFuture with boolean that indicates whether the teleportation was successful.
     */
    default CompletableFuture<Boolean> teleport(Player player, Island island, Dimension dimension) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        teleportWithResult(player, island, dimension).whenComplete((value, error) -> {
            if(error != null) {
                result.completeExceptionally(error);
            } else {
                result.complete(value == TeleportResult.SUCCESS);
            }
        });
        return result;
    }

    /**
     * Teleport a player to an island in a specific dimension.
     *
     * @param player    The player to teleport.
     * @param island    The island to teleport the player to.
     * @param dimension The dimension to teleport the player to.
     * @return CompletableFuture with the result of the teleportation.
     */
    CompletableFuture<TeleportResult> teleportWithResult(Player player, Island island, Dimension dimension);

    enum TeleportResult {

        /**
         * The player was successfully teleported.
         */
        SUCCESS,

        /**
         * A general failure occurred when teleporting the player.
         * This can happen if the platform implementation of Entity#teleport fails.
         */
        GENERAL_FAILURE,

        /**
         * An unexpected error occurred while teleporting the player.
         */
        UNEXPECTED_ERROR,

        /**
         * The player was offline when trying to teleport it.
         */
        OFFLINE_PLAYER,

        /**
         * The location the player was teleported to is unsafe.
         */
        UNSAFE_SPOT,

        /**
         * A custom reason for not teleporting the player.
         * In this case, the plugin will not do anything, and it is the responsibility of other plugins
         * to inform the player about the failure of the teleportation.
         */
        CUSTOM

    }

}
