package com.bgsoftware.superiorskyblock.player.algorithm;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.player.algorithm.PlayerTeleportAlgorithm;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.core.IslandWorlds;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.world.Dimensions;
import com.bgsoftware.superiorskyblock.world.EntityTeleports;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class DefaultPlayerTeleportAlgorithm implements PlayerTeleportAlgorithm {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final DefaultPlayerTeleportAlgorithm INSTANCE = new DefaultPlayerTeleportAlgorithm();

    private DefaultPlayerTeleportAlgorithm() {

    }

    public static DefaultPlayerTeleportAlgorithm getInstance() {
        return INSTANCE;
    }

    @Override
    public CompletableFuture<Boolean> teleport(Player player, Location location) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        EntityTeleports.teleport(player, location, completableFuture::complete);
        return completableFuture;
    }

    @Override
    public CompletableFuture<Boolean> teleport(Player player, Island island) {
        return this.teleport(player, island, plugin.getSettings().getWorlds().getDefaultWorldDimension());
    }

    @Override
    public CompletableFuture<Boolean> teleport(Player player, Island island, Dimension dimension) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        IslandWorlds.accessIslandWorldAsync(island, dimension, true, islandWorldResult -> {
            islandWorldResult.ifRight(result::completeExceptionally).ifLeft(world ->
                    teleportInternal(player, island, dimension, result));
        });
        return result;
    }

    @Override
    @Deprecated
    public CompletableFuture<Boolean> teleport(Player player, Island island, World.Environment environment) {
        return teleport(player, island, Dimensions.fromEnvironment(environment));
    }

    public void teleportInternal(Player player, Island island, Dimension dimension, CompletableFuture<Boolean> result) {
        Location homeLocation = island.getIslandHome(dimension);

        Preconditions.checkNotNull(homeLocation, "Cannot find a suitable home location for island " +
                island.getUniqueId());

        Log.debug(Debug.TELEPORT_PLAYER, player.getName(), island.getOwner().getName(), dimension);

        EntityTeleports.findIslandSafeLocation(island, dimension).whenComplete((safeSpot, error) -> {
            if (error != null) {
                Log.debugResult(Debug.TELEPORT_PLAYER, "Teleport Location", null);
                result.completeExceptionally(error);
            } else if (safeSpot == null) {
                Log.debugResult(Debug.TELEPORT_PLAYER, "Teleport Location", null);
                result.complete(false);
            } else {
                Log.debugResult(Debug.TELEPORT_PLAYER, "Teleport Location", safeSpot);
                teleport(player, safeSpot).whenComplete((teleport, teleportError) -> {
                    if (teleportError != null) {
                        Log.debugResult(Debug.TELEPORT_PLAYER, "Teleport Result", false);
                        result.completeExceptionally(teleportError);
                    } else {
                        Log.debugResult(Debug.TELEPORT_PLAYER, "Teleport Result", true);
                        result.complete(teleport);
                    }
                });
            }
        });
    }

}
