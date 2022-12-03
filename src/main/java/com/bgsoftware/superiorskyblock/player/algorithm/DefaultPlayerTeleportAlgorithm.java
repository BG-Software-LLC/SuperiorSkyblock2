package com.bgsoftware.superiorskyblock.player.algorithm;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.player.algorithm.PlayerTeleportAlgorithm;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
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
        return this.teleport(player, island, plugin.getSettings().getWorlds().getDefaultWorld());
    }

    @Override
    public CompletableFuture<Boolean> teleport(Player player, Island island, World.Environment environment) {
        Location homeLocation = island.getIslandHome(environment);

        Preconditions.checkNotNull(homeLocation, "Cannot find a suitable home location for island " +
                island.getUniqueId());

        Log.debug(Debug.TELEPORT_PLAYER, player.getName(), island.getOwner().getName(), environment);

        CompletableFuture<Boolean> teleportResult = new CompletableFuture<>();

        EntityTeleports.findIslandSafeLocation(island, environment).whenComplete((safeSpot, error) -> {
            if (error != null) {
                Log.debugResult(Debug.TELEPORT_PLAYER, "Teleport Location", null);
                teleportResult.completeExceptionally(error);
            } else if (safeSpot == null) {
                Log.debugResult(Debug.TELEPORT_PLAYER, "Teleport Location", null);
                teleportResult.complete(false);
            } else {
                Log.debugResult(Debug.TELEPORT_PLAYER, "Teleport Location", safeSpot);
                teleport(player, safeSpot).whenComplete((teleport, teleportError) -> {
                    if (teleportError != null) {
                        Log.debugResult(Debug.TELEPORT_PLAYER, "Teleport Result", false);
                        teleportResult.completeExceptionally(teleportError);
                    } else {
                        Log.debugResult(Debug.TELEPORT_PLAYER, "Teleport Result", true);
                        teleportResult.complete(teleport);
                    }
                });
            }
        });

        return teleportResult;
    }

}
