package com.bgsoftware.superiorskyblock.api.world.algorithm;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandCreationAlgorithm {

    /**
     * Create a new island on the server.
     * This method should not only create the Island object itself, but also paste a schematic.
     * Teleportation and island initialization will be handled by the plugin.
     *
     * @param islandUUID The uuid of the island.
     * @param owner      The owner of the island.
     * @param lastIsland The location of the last generated island.
     * @param islandName The name of the island.
     * @param schematic  The schematic used to create the island.
     */
    CompletableFuture<IslandCreationResult> createIsland(UUID islandUUID, SuperiorPlayer owner, BlockPosition lastIsland,
                                                         String islandName, Schematic schematic);

    /**
     * Class representing result of a creation process.
     */
    class IslandCreationResult {

        private final Island island;
        private final Location islandLocation;
        private final boolean shouldTeleportPlayer;

        /**
         * Constructor of the result.
         *
         * @param island               The created island.
         * @param islandLocation       The location of the island.
         * @param shouldTeleportPlayer Whether to teleport the player to his island or not.
         */
        public IslandCreationResult(Island island, Location islandLocation, boolean shouldTeleportPlayer) {
            this.island = island;
            this.islandLocation = islandLocation;
            this.shouldTeleportPlayer = shouldTeleportPlayer;
        }

        /**
         * Get the created island object.
         */
        public Island getIsland() {
            return island;
        }

        /**
         * Get the location of the new island.
         */
        public Location getIslandLocation() {
            return islandLocation;
        }

        /**
         * Get whether the player that created the island should be teleported to it.
         */
        public boolean shouldTeleportPlayer() {
            return shouldTeleportPlayer;
        }

    }

}
