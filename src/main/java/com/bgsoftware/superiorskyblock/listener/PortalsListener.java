package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.player.PlayerStatus;
import com.bgsoftware.superiorskyblock.api.service.portals.EntityPortalResult;
import com.bgsoftware.superiorskyblock.api.service.portals.PortalsManagerService;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.IslandWorlds;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import com.bgsoftware.superiorskyblock.player.SuperiorNPCPlayer;
import com.bgsoftware.superiorskyblock.world.EntityTeleports;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PortalsListener extends AbstractGameEventListener {

    private final LazyReference<PortalsManagerService> portalsManager = new LazyReference<PortalsManagerService>() {
        @Override
        protected PortalsManagerService create() {
            return plugin.getServices().getService(PortalsManagerService.class);
        }
    };

    public PortalsListener(SuperiorSkyblockPlugin plugin) {
        super(plugin);
        registerListeners();
    }

    private void registerListeners() {
        registerCallback(GameEventType.ENTITY_PORTAL_EVENT, GameEventPriority.MONITOR, this::onEntityPortal);
        registerCallback(GameEventType.ENTITY_ENTER_PORTAL_EVENT, GameEventPriority.HIGHEST, this::onEntityEnterPortal);
    }

    private void onEntityPortal(GameEvent<GameEventArgs.EntityPortalEvent> e) {
        if (e.getArgs().entity instanceof Player)
            handlePlayerPortal(e);
        else
            handleEntityPortalResult(e);
    }

    private void onEntityEnterPortal(GameEvent<GameEventArgs.EntityEnterPortalEvent> e) {
        // Simulate portals in the following cases:
        //  - Using an end portal in the end
        //  - The target world is disabled

        Entity entity = e.getArgs().entity;

        Island island;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            island = plugin.getGrid().getIslandAt(entity.getLocation(wrapper.getHandle()));
        }

        if (island == null)
            return;

        Location portalLocation = e.getArgs().portalLocation;

        World world = portalLocation.getWorld();

        // Simulate end portal
        if (world.getEnvironment() == World.Environment.THE_END) {
            /* We teleport the player to his island instead of cancelling the event.
            Therefore, we must prevent the player from acting like he entered another island or left his island.*/

            SuperiorPlayer teleportedPlayer = entity instanceof Player ?
                    plugin.getPlayers().getSuperiorPlayer((Player) entity) : null;

            if (teleportedPlayer != null)
                teleportedPlayer.setPlayerStatus(PlayerStatus.LEAVING_ISLAND);

            BukkitExecutor.sync(() -> {
                Dimension dimension = plugin.getSettings().getWorlds().getDefaultWorldDimension();
                IslandWorlds.accessIslandWorldAsync(island, dimension, true, islandWorldResult -> {
                    islandWorldResult.ifRight(error -> {
                        if (teleportedPlayer != null)
                            teleportedPlayer.removePlayerStatus(PlayerStatus.LEAVING_ISLAND);
                    }).ifLeft(unused -> {
                        EntityTeleports.teleportUntilSuccess(entity,
                                island.getIslandHome(dimension), 5, () -> {
                                    if (teleportedPlayer != null)
                                        teleportedPlayer.removePlayerStatus(PlayerStatus.LEAVING_ISLAND);
                                });
                    });
                });

            }, 5L);
        }

        if (ServerVersion.isLessThan(ServerVersion.v1_16))
            return;

        boolean isPlayer = entity instanceof Player;

        Material originalMaterial = portalLocation.getBlock().getType();

        PortalType portalType = originalMaterial == Materials.NETHER_PORTAL.toBukkitType() ? PortalType.NETHER : PortalType.ENDER;

        if (isPlayer && (portalType == PortalType.NETHER ? Bukkit.getAllowNether() : Bukkit.getAllowEnd()))
            return;

        if (portalType == PortalType.NETHER) {
            int ticksDelay = !isPlayer ? 0 : ((Player) entity).getGameMode() == GameMode.CREATIVE ? 1 : 80;
            int portalTicks = plugin.getNMSEntities().getPortalTicks(entity);
            if (portalTicks != ticksDelay)
                return;
        }

        if (isPlayer) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(entity);
            this.portalsManager.get().handlePlayerPortalFromIsland(superiorPlayer, island, portalLocation, portalType, true);
        } else {
            this.portalsManager.get().handleEntityPortalFromIsland(entity, island, portalLocation, portalType);
        }
    }

    private void handlePlayerPortal(GameEvent<GameEventArgs.EntityPortalEvent> e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer((Player) e.getArgs().entity);

        if (superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        PortalType portalType = (e.getArgs().cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) ?
                PortalType.NETHER : PortalType.ENDER;

        EntityPortalResult portalResult = this.portalsManager.get().handlePlayerPortal(
                superiorPlayer, e.getArgs().from, portalType, e.getArgs().to, true);

        handleEntityPortalResult(portalResult, e);
    }

    public void handleEntityPortalResult(GameEvent<GameEventArgs.EntityPortalEvent> e) {
        Location from = e.getArgs().from;
        Location to = e.getArgs().to;

        if (to == null || to.getWorld() == null || from == null || from.getWorld() == null)
            return;

        Entity entity = e.getArgs().entity;

        PortalType portalType = (e.getArgs().cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) ?
                PortalType.NETHER : PortalType.ENDER;

        EntityPortalResult portalResult = this.portalsManager.get().handleEntityPortal(entity, from, portalType, to);

        handleEntityPortalResult(portalResult, e);
    }

    private void handleEntityPortalResult(EntityPortalResult portalResult, GameEvent<GameEventArgs.EntityPortalEvent> event) {
        switch (portalResult) {
            case PORTAL_NOT_IN_ISLAND:
                return;
            case DESTINATION_WORLD_DISABLED:
            case PLAYER_IMMUNED_TO_PORTAL:
            case SCHEMATIC_GENERATING_COOLDOWN:
            case DESTINATION_NOT_ISLAND_WORLD:
            case PORTAL_EVENT_CANCELLED:
            case INVALID_SCHEMATIC:
            case WORLD_NOT_UNLOCKED:
            case DESTINATION_ISLAND_NOT_PERMITTED:
            case SUCCEED:
                event.setCancelled();
                return;
            default:
                throw new IllegalStateException("No handling for result: " + portalResult);
        }
    }

}
