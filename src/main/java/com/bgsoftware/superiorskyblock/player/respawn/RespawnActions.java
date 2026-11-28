package com.bgsoftware.superiorskyblock.player.respawn;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.player.algorithm.PlayerTeleportAlgorithm;
import com.bgsoftware.superiorskyblock.api.player.respawn.RespawnAction;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;

public class RespawnActions {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static final RespawnAction BED_TELEPORT = register(new RespawnAction("BED_TELEPORT") {

        @Override
        public boolean canPerform(PlayerRespawnEvent event) {
            Player player = event.getPlayer();
            return player.getBedSpawnLocation() != null;
        }

        @Override
        public void perform(PlayerRespawnEvent event) {
            event.setRespawnLocation(event.getPlayer().getBedSpawnLocation());
        }

    });

    public static final RespawnAction ISLAND_TELEPORT = register(new RespawnAction("ISLAND_TELEPORT") {

        @Override
        public boolean canPerform(PlayerRespawnEvent event) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(event.getPlayer());
            return superiorPlayer.getIsland() != null;
        }

        @Override
        public void perform(PlayerRespawnEvent event) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(event.getPlayer());
            Island island = superiorPlayer.getIsland();
            superiorPlayer.teleportWithResult(island,
                    result -> onTeleportCallback(superiorPlayer, island, result));
        }

    });

    public static final RespawnAction SPAWN_TELEPORT = register(new RespawnAction("SPAWN_TELEPORT") {

        @Override
        public boolean canPerform(PlayerRespawnEvent event) {
            return true;
        }

        @Override
        public void perform(PlayerRespawnEvent event) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(event.getPlayer());
            Island spawnIsland = plugin.getGrid().getSpawnIsland();
            superiorPlayer.teleportWithResult(spawnIsland,
                    result -> onTeleportCallback(superiorPlayer, spawnIsland, result));
        }

    });

    public static final RespawnAction VANILLA = register(new RespawnAction("VANILLA") {

        @Override
        public boolean canPerform(PlayerRespawnEvent event) {
            return true;
        }

        @Override
        public void perform(PlayerRespawnEvent event) {
            // Do nothing, let vanilla do its thing.
        }

    });

    private RespawnActions() {

    }

    public static void registerActions() {
        // Do nothing, only trigger all the register calls
    }

    private static RespawnAction register(RespawnAction respawnAction) {
        RespawnAction.register(respawnAction);
        return respawnAction;
    }

    private static void onTeleportCallback(SuperiorPlayer superiorPlayer, Island island, PlayerTeleportAlgorithm.TeleportResult result) {
        if (result == PlayerTeleportAlgorithm.TeleportResult.SUCCESS) {
            BukkitExecutor.sync(() -> {
                if (superiorPlayer.isOnline())
                    superiorPlayer.updateWorldBorder(island);
            }, 2L);
        }
    }

}
