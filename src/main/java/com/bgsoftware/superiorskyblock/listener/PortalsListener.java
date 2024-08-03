package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.player.PlayerStatus;
import com.bgsoftware.superiorskyblock.api.service.portals.EntityPortalResult;
import com.bgsoftware.superiorskyblock.api.service.portals.PortalsManagerService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.player.SuperiorNPCPlayer;
import com.bgsoftware.superiorskyblock.world.EntityTeleports;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PortalsListener implements Listener {

    private final LazyReference<PortalsManagerService> portalsManager = new LazyReference<PortalsManagerService>() {
        @Override
        protected PortalsManagerService create() {
            return plugin.getServices().getService(PortalsManagerService.class);
        }
    };

    private final SuperiorSkyblockPlugin plugin;

    public PortalsListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if (superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        PortalType portalType = (e.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) ?
                PortalType.NETHER : PortalType.ENDER;

        EntityPortalResult portalResult = this.portalsManager.get().handlePlayerPortal(superiorPlayer, e.getFrom(),
                portalType, e.getTo(), true);
        handleEntityPortal(portalResult, e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityPortal(EntityPortalEvent e) {
        if (e.getTo() == null || e.getTo().getWorld() == null || e.getFrom() == null || e.getFrom().getWorld() == null)
            return;

        PortalType portalType = e.getTo().getWorld().getEnvironment() == World.Environment.THE_END ||
                e.getFrom().getWorld().getEnvironment() == World.Environment.THE_END ? PortalType.ENDER : PortalType.NETHER;

        EntityPortalResult portalResult = this.portalsManager.get().handleEntityPortal(e.getEntity(), e.getFrom(),
                portalType, e.getTo());
        handleEntityPortal(portalResult, e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPortalEnter(EntityPortalEnterEvent e) {
        // Simulate portals in the following cases:
        //  - Using an end portal in the end
        //  - The target world is disabled

        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if (island == null)
            return;

        World world = e.getLocation().getWorld();

        // Simulate end portal
        if (world.getEnvironment() == World.Environment.THE_END) {
            /* We teleport the player to his island instead of cancelling the event.
            Therefore, we must prevent the player from acting like he entered another island or left his island.*/

            SuperiorPlayer teleportedPlayer = e.getEntity() instanceof Player ?
                    plugin.getPlayers().getSuperiorPlayer((Player) e.getEntity()) : null;

            if (teleportedPlayer != null)
                teleportedPlayer.setPlayerStatus(PlayerStatus.LEAVING_ISLAND);

            BukkitExecutor.sync(() -> {
                EntityTeleports.teleportUntilSuccess(e.getEntity(),
                        island.getIslandHome(plugin.getSettings().getWorlds().getDefaultWorldDimension()), 5, () -> {
                            if (teleportedPlayer != null)
                                teleportedPlayer.removePlayerStatus(PlayerStatus.LEAVING_ISLAND);
                        });
            }, 5L);
        }

        if (ServerVersion.isLessThan(ServerVersion.v1_16))
            return;

        boolean isPlayer = e.getEntity() instanceof Player;

        Material originalMaterial = e.getLocation().getBlock().getType();

        PortalType portalType = originalMaterial == Materials.NETHER_PORTAL.toBukkitType() ? PortalType.NETHER : PortalType.ENDER;

        if (isPlayer && (portalType == PortalType.NETHER ? Bukkit.getAllowNether() : Bukkit.getAllowEnd()))
            return;

        if (portalType == PortalType.NETHER) {
            int ticksDelay = !isPlayer ? 0 : ((Player) e.getEntity()).getGameMode() == GameMode.CREATIVE ? 1 : 80;
            int portalTicks = plugin.getNMSEntities().getPortalTicks(e.getEntity());
            if (portalTicks != ticksDelay)
                return;
        }

        if (isPlayer) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getEntity());
            this.portalsManager.get().handlePlayerPortalFromIsland(superiorPlayer, island, e.getLocation(), portalType, true);
        } else {
            this.portalsManager.get().handleEntityPortalFromIsland(e.getEntity(), island, e.getLocation(), portalType);
        }
    }

    private void handleEntityPortal(EntityPortalResult portalResult, Cancellable event) {
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
                event.setCancelled(true);
                return;
            default:
                throw new IllegalStateException("No handling for result: " + portalResult);
        }
    }

}
