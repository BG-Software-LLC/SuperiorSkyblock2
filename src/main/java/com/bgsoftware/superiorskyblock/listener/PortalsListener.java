package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeLevelBonusEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeWorthBonusEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.Singleton;
import com.bgsoftware.superiorskyblock.core.collections.AutoRemovalCollection;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.events.EventsBus;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.player.SuperiorNPCPlayer;
import com.bgsoftware.superiorskyblock.world.EntityTeleports;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
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

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PortalsListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;
    private final Singleton<PlayersListener> playersListener;
    private final AutoRemovalCollection<UUID> generatingSchematicsIslands = AutoRemovalCollection.newHashSet(20, TimeUnit.SECONDS);

    public PortalsListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.playersListener = plugin.getListener(PlayersListener.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if (superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        if (preventPlayerPortal(e.getPlayer(), e.getFrom(), e.getCause(), false)) {
            e.setCancelled(true);
            return;
        }

        Island toIsland = plugin.getGrid().getIslandAt(e.getTo());

        if (toIsland == null)
            return;

        Island fromIsland = plugin.getGrid().getIslandAt(e.getFrom());

        if (playersListener.get().preventPlayerEnterIsland(superiorPlayer, e.getFrom(), fromIsland, e.getTo(), toIsland,
                IslandEnterEvent.EnterCause.PORTAL))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPortalEnter(EntityPortalEnterEvent e) {
        World world = e.getLocation().getWorld();

        // Simulate end portal
        if (world.getEnvironment() == World.Environment.THE_END && plugin.getGrid().isIslandsWorld(world)) {
            Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());
            if (island != null && island.wasSchematicGenerated(World.Environment.NORMAL)) {
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

        if (!(e.getEntity() instanceof Player) || ServerVersion.isLessThan(ServerVersion.v1_16))
            return;

        Material originalMaterial = e.getLocation().getBlock().getType();

        PlayerTeleportEvent.TeleportCause teleportCause = originalMaterial == Materials.NETHER_PORTAL.toBukkitType() ?
                PlayerTeleportEvent.TeleportCause.NETHER_PORTAL : PlayerTeleportEvent.TeleportCause.END_PORTAL;

        if (teleportCause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL ? Bukkit.getAllowNether() : Bukkit.getAllowEnd())
            return;

        if (teleportCause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            int ticksDelay = ((Player) e.getEntity()).getGameMode() == GameMode.CREATIVE ? 1 : 80;
            int portalTicks = plugin.getNMSEntities().getPortalTicks(e.getEntity());
            if (portalTicks != ticksDelay)
                return;
        }

        onPlayerPortal((Player) e.getEntity(), e.getLocation(), teleportCause, false);
    }

    public void onPlayerPortal(Player player, Location portalLocation,
                               PlayerTeleportEvent.TeleportCause teleportCause,
                               boolean isAdminCommand) {
        /* Alias for preventPlayerPortal */
        preventPlayerPortal(player, portalLocation, teleportCause, isAdminCommand);
    }

    public boolean preventPlayerPortal(Player player, Location portalLocation,
                                       PlayerTeleportEvent.TeleportCause teleportCause,
                                       boolean isAdminCommand) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        if (superiorPlayer instanceof SuperiorNPCPlayer)
            return false;

        if (!isAdminCommand && superiorPlayer.isImmunedToPortals())
            return true;

        Island island = plugin.getGrid().getIslandAt(portalLocation);

        if (island == null || !plugin.getGrid().isIslandsWorld(portalLocation.getWorld()))
            return false;

        World.Environment originalDestination = getTargetWorld(portalLocation, teleportCause);

        if (plugin.getGrid().getIslandsWorld(island, originalDestination) == null)
            return true;

        if (!isIslandWorldEnabled(originalDestination, island)) {
            if (!Message.WORLD_NOT_UNLOCKED.isEmpty(superiorPlayer.getUserLocale()))
                Message.SCHEMATICS.send(superiorPlayer, Message.WORLD_NOT_UNLOCKED.getMessage(
                        superiorPlayer.getUserLocale(), Formatters.CAPITALIZED_FORMATTER.format(originalDestination.name())));
            return true;
        }

        try {
            if (generatingSchematicsIslands.contains(island.getUniqueId()))
                return true; // We want to prevent the players from being teleported in this time.

            String destinationEnvironmentName = originalDestination.name().toLowerCase(Locale.ENGLISH);
            String islandSchematic = island.getSchematicName();

            Schematic originalSchematic = plugin.getSchematics().getSchematic(islandSchematic.isEmpty() ?
                    plugin.getSchematics().getDefaultSchematic(originalDestination) :
                    islandSchematic + "_" + destinationEnvironmentName);

            PortalType portalType = teleportCause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL ?
                    PortalType.NETHER : PortalType.ENDER;

            boolean schematicGenerated = island.wasSchematicGenerated(originalDestination);

            EventResult<EventsBus.PortalEventResult> eventResult = plugin.getEventsBus().callIslandEnterPortalEvent(
                    superiorPlayer, island, portalType, originalDestination, schematicGenerated ? null : originalSchematic,
                    schematicGenerated);

            if (eventResult.isCancelled())
                return true;

            World.Environment destination = eventResult.getResult().getDestination();
            Schematic schematic = eventResult.getResult().getSchematic();
            boolean ignoreInvalidSchematic = eventResult.getResult().isIgnoreInvalidSchematic();

            if (schematic == null && !ignoreInvalidSchematic) {
                Message.SCHEMATICS.send(superiorPlayer, ChatColor.RED + "The server hasn't added a " +
                        destinationEnvironmentName + " schematic. Please contact administrator to solve the problem. " +
                        "The format for " + destinationEnvironmentName + " schematic is \"" +
                        islandSchematic + "_" + destinationEnvironmentName + "\".");
                return true;
            }

            generatingSchematicsIslands.add(island.getUniqueId());

            // If schematic was already generated, or no schematic should be generated, simply
            // teleport player to destination location.
            if (schematic == null || island.wasSchematicGenerated(destination)) {
                superiorPlayer.teleport(island, destination, result -> {
                    generatingSchematicsIslands.remove(island.getUniqueId());
                });
                return true;
            }

            Location schematicPlacementLocation = island.getCenter(destination).subtract(0, 1, 0);

            BigDecimal originalWorth = island.getRawWorth();
            BigDecimal originalLevel = island.getRawLevel();

            schematic.pasteSchematic(island, schematicPlacementLocation, () -> {
                generatingSchematicsIslands.remove(island.getUniqueId());
                island.setSchematicGenerate(destination);

                if (shouldOffsetSchematic(destination)) {
                    {
                        BigDecimal schematicWorth = island.getRawWorth().subtract(originalWorth);
                        EventResult<BigDecimal> bonusEventResult = plugin.getEventsBus().callIslandChangeWorthBonusEvent(null, island,
                                IslandChangeWorthBonusEvent.Reason.SCHEMATIC, island.getBonusWorth().subtract(schematicWorth));
                        if (!eventResult.isCancelled())
                            island.setBonusWorth(bonusEventResult.getResult());
                    }
                    {
                        BigDecimal schematicLevel = island.getRawLevel().subtract(originalLevel);
                        EventResult<BigDecimal> bonusEventResult = plugin.getEventsBus().callIslandChangeLevelBonusEvent(null, island,
                                IslandChangeLevelBonusEvent.Reason.SCHEMATIC, island.getBonusLevel().subtract(schematicLevel));
                        if (!bonusEventResult.isCancelled())
                            island.setBonusLevel(bonusEventResult.getResult());
                    }
                }

                Location destinationLocation = island.getIslandHome(destination);

                if (destination == World.Environment.THE_END) {
                    plugin.getNMSDragonFight().awardTheEndAchievement(player);
                    plugin.getServices().getDragonBattleService().resetEnderDragonBattle(island);
                }

                superiorPlayer.teleport(schematic.adjustRotation(destinationLocation));
            }, error -> {
                generatingSchematicsIslands.remove(island.getUniqueId());
                error.printStackTrace();
                Message.CREATE_WORLD_FAILURE.send(player);
            });

        } catch (NullPointerException ignored) {
        }

        return true;
    }

    private boolean shouldOffsetSchematic(World.Environment environment) {
        switch (environment) {
            case NORMAL:
                return plugin.getSettings().getWorlds().getNormal().isSchematicOffset();
            case NETHER:
                return plugin.getSettings().getWorlds().getNether().isSchematicOffset();
            case THE_END:
                return plugin.getSettings().getWorlds().getEnd().isSchematicOffset();
            default:
                return false;
        }
    }

    private static World.Environment getTargetWorld(Location portalLocation, PlayerTeleportEvent.TeleportCause teleportCause) {
        World.Environment portalEnvironment = portalLocation.getWorld().getEnvironment();
        World.Environment environment;

        switch (teleportCause) {
            case END_PORTAL:
                environment = World.Environment.THE_END;
                break;
            case NETHER_PORTAL:
                environment = World.Environment.NETHER;
                break;
            default:
                environment = World.Environment.NORMAL;
                break;
        }

        return environment == portalEnvironment ? World.Environment.NORMAL : environment;
    }

    private static boolean isIslandWorldEnabled(World.Environment environment, Island island) {
        switch (environment) {
            case NORMAL:
                return island.isNormalEnabled();
            case NETHER:
                return island.isNetherEnabled();
            case THE_END:
                return island.isEndEnabled();
            default:
                return true;
        }
    }

}
