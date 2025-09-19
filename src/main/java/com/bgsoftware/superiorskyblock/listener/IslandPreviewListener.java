package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import com.bgsoftware.superiorskyblock.player.SuperiorNPCPlayer;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class IslandPreviewListener extends AbstractGameEventListener {

    public IslandPreviewListener(SuperiorSkyblockPlugin plugin) {
        super(plugin);

        registerCallback(GameEventType.PLAYER_QUIT_EVENT, GameEventPriority.MONITOR, this::onPlayerQuit);
        registerCallback(GameEventType.ENTITY_TELEPORT_EVENT, GameEventPriority.NORMAL, this::onPlayerTeleport);
    }

    private void onPlayerQuit(GameEvent<GameEventArgs.PlayerQuitEvent> e) {
        Player player = e.getArgs().player;
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        if (superiorPlayer instanceof SuperiorNPCPlayer) {
            ((SuperiorNPCPlayer) superiorPlayer).release();
            return;
        }

        // Cancelling island preview mode
        if (plugin.getGrid().getIslandPreview(superiorPlayer) != null) {
            plugin.getGrid().cancelIslandPreview(superiorPlayer);
            /* cancelIslandPreview changes the GameMode and teleports the player later.
            In this case tho, we want the things to be instant - no async, no nothing. */
            player.setGameMode(GameMode.SURVIVAL);
            player.teleport(plugin.getGrid().getSpawnIsland().getCenter(
                    plugin.getSettings().getWorlds().getDefaultWorldDimension()));
        }
    }

    private void onPlayerTeleport(GameEvent<GameEventArgs.EntityTeleportEvent> e) {
        Entity entity = e.getArgs().entity;
        if (!(entity instanceof Player))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer((Player) entity);

        if (superiorPlayer instanceof SuperiorNPCPlayer) {
            ((SuperiorNPCPlayer) superiorPlayer).release();
            return;
        }

        if (((Player) entity).getGameMode() == GameMode.SPECTATOR &&
                plugin.getGrid().getIslandPreview(superiorPlayer) != null)
            e.setCancelled();
    }

}
