package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
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
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
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

        PortalType portalType = (e.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) ? PortalType.NETHER : PortalType.ENDER;

        EntityPortalResult portalResult = this.portalsManager.get().handlePlayerPortal(superiorPlayer, e.getFrom(),
                portalType, e.getTo(), false);

        switch (portalResult) {
            case DESTINATION_WORLD_DISABLED:
                return;
            case PLAYER_IMMUNED_TO_PORTAL:
            case PENDING_TELEPORT:
            case DESTINATION_NOT_ISLAND_WORLD:
            case PORTAL_EVENT_CANCELLED:
            case INVALID_SCHEMATIC:
            case WORLD_NOT_UNLOCKED:
            case DESTINATION_ISLAND_NOT_PERMITTED:
            case SUCCEED:
                e.setCancelled(true);
                return;
            case PORTAL_NOT_IN_ISLAND:
                break;
            default:
                throw new IllegalStateException("No handling for result: " + portalResult);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPortalEnter(EntityPortalEnterEvent e) {
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if (island == null)
            return;

        World world = e.getLocation().getWorld();

        // Simulate end portal
        if (world.getEnvironment() == World.Environment.THE_END && plugin.getGrid().isIslandsWorld(world)) {
            if (island.wasSchematicGenerated(World.Environment.NORMAL)) {
                /* We teleport the player to his island instead of cancelling the event.
                Therefore, we must prevent the player from acting like he entered another island or left his island.*/

                SuperiorPlayer teleportedPlayer = e.getEntity() instanceof Player ?
                        plugin.getPlayers().getSuperiorPlayer((Player) e.getEntity()) : null;

                if (teleportedPlayer != null)
                    teleportedPlayer.setLeavingFlag(true);

                BukkitExecutor.sync(() -> {
                    EntityTeleports.teleportUntilSuccess(e.getEntity(), island.getIslandHome(World.Environment.NORMAL), 5, () -> {
                        if (teleportedPlayer != null)
                            teleportedPlayer.setLeavingFlag(false);
                    });
                }, 5L);
            }
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
            this.portalsManager.get().handlePlayerPortalFromIsland(superiorPlayer, island, e.getLocation(), portalType, false);
        } else {
            this.portalsManager.get().handleEntityPortalFromIsland(e.getEntity(), island, e.getLocation(), portalType);
        }
    }

}
