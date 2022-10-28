package com.bgsoftware.superiorskyblock.api.player.algorithm;

import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class DelegatePlayerTeleportAlgorithm implements PlayerTeleportAlgorithm {

    protected final PlayerTeleportAlgorithm handle;

    protected DelegatePlayerTeleportAlgorithm(PlayerTeleportAlgorithm handle) {
        this.handle = handle;
    }

    @Override
    public CompletableFuture<Boolean> teleport(Player player, Location location) {
        return this.handle.teleport(player, location);
    }

    @Override
    public CompletableFuture<Boolean> teleport(Player player, Island island) {
        return this.handle.teleport(player, island);
    }

    @Override
    public CompletableFuture<Boolean> teleport(Player player, Island island, World.Environment environment) {
        return this.handle.teleport(player, island, environment);
    }

}
