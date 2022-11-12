package com.bgsoftware.superiorskyblock.player.respawn;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.player.respawn.RespawnAction;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
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
            assert superiorPlayer.getIsland() != null;
            superiorPlayer.teleport(superiorPlayer.getIsland());
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
            superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
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

}
