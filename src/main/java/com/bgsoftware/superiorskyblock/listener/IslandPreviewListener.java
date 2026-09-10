package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.IslandPreview;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataType;
import com.bgsoftware.superiorskyblock.api.player.algorithm.PlayerTeleportAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import com.bgsoftware.superiorskyblock.player.SuperiorNPCPlayer;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class IslandPreviewListener extends AbstractGameEventListener {

    private static final String PREVIEW_RESTORE_KEY = "superiorskyblock:preview-restore";

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
        IslandPreview islandPreview = plugin.getGrid().getIslandPreview(superiorPlayer);
        if (islandPreview != null) {
            GameMode gameMode = islandPreview.getPreviousGameMode();
            if (BukkitExecutor.isFolia()) {
                saveForRestore(superiorPlayer, gameMode);
                plugin.getGrid().cancelIslandPreview(superiorPlayer, false);
                player.setGameMode(gameMode);
                return;
            }
            plugin.getGrid().cancelIslandPreview(superiorPlayer);
            /* cancelIslandPreview changes the GameMode and teleports the player later.
            In this case tho, we want the things to be instant - no async, no nothing. */
            player.setGameMode(gameMode);
            player.teleport(plugin.getGrid().getSpawnIsland().getCenter(
                    plugin.getSettings().getWorlds().getDefaultWorldDimension()));
        }
    }

    public static void saveForRestore(SuperiorPlayer superiorPlayer, GameMode gameMode) {
        superiorPlayer.getPersistentDataContainer().put(PREVIEW_RESTORE_KEY, PersistentDataType.STRING, gameMode.name());
        superiorPlayer.savePersistentDataContainer();
    }

    public static boolean restoreOnJoin(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Player player) {
        if (!BukkitExecutor.isFolia() || superiorPlayer.isPersistentDataContainerEmpty())
            return false;
        PersistentDataContainer data = superiorPlayer.getPersistentDataContainer();
        Object storedGameMode = data.get(PREVIEW_RESTORE_KEY);
        if (!(storedGameMode instanceof String))
            return false;
        GameMode gameMode;
        try {
            gameMode = GameMode.valueOf((String) storedGameMode);
        } catch (IllegalArgumentException ex) {
            Log.warn("Invalid island preview restore mode for " + superiorPlayer.getUniqueId());
            return false;
        }
        superiorPlayer.teleportWithResult(plugin.getGrid().getSpawnIsland(), result -> {
            if (result == PlayerTeleportAlgorithm.TeleportResult.SUCCESS && superiorPlayer.asPlayer() == player
                    && player.isOnline() && storedGameMode.equals(data.get(PREVIEW_RESTORE_KEY))
                    && plugin.getGrid().getIslandPreview(superiorPlayer) == null) {
                player.setGameMode(gameMode);
                data.remove(PREVIEW_RESTORE_KEY);
                superiorPlayer.savePersistentDataContainer();
            }
        });
        return true;
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

        if (((Player) entity).getGameMode() == plugin.getSettings().getIslandPreviews().getGameMode() &&
                plugin.getGrid().getIslandPreview(superiorPlayer) != null)
            e.setCancelled();
    }

}
