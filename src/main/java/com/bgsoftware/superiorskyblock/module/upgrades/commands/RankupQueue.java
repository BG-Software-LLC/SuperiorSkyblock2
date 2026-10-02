package com.bgsoftware.superiorskyblock.module.upgrades.commands;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.threads.SerialTaskQueue;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CancellationException;
import java.util.function.Supplier;

final class RankupQueue {

    private static final SerialTaskQueue<UUID> ISLAND_QUEUE = new SerialTaskQueue<>();
    private static final SerialTaskQueue<UUID> PLAYER_QUEUE = new SerialTaskQueue<>();

    private RankupQueue() {
    }

    static CompletableFuture<String> parseCommand(SuperiorPlayer superiorPlayer, Supplier<String> parser) {
        Player player = superiorPlayer.asPlayer();
        if (player == null)
            return BukkitExecutor.submit(parser);
        return BukkitExecutor.submit(player, parser).handle((command, error) -> {
            if (error == null)
                return CompletableFuture.completedFuture(command);
            if (error instanceof CancellationException && !superiorPlayer.isOnline())
                return BukkitExecutor.submit(parser);
            CompletableFuture<String> failed = new CompletableFuture<>();
            failed.completeExceptionally(error);
            return failed;
        }).thenCompose(future -> future);
    }

    static CompletableFuture<Void> submit(Island island, SuperiorPlayer superiorPlayer,
                                          Supplier<CompletableFuture<Void>> task) {
        UUID islandUUID = island.getUniqueId();
        if (superiorPlayer == null)
            return ISLAND_QUEUE.submit(islandUUID, task);
        return PLAYER_QUEUE.submit(superiorPlayer.getUniqueId(), () -> ISLAND_QUEUE.submit(islandUUID, task));
    }

}
