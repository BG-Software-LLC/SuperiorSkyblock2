package com.bgsoftware.superiorskyblock.utils.logic;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeLevelBonusEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeWorthBonusEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.formatting.Formatters;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.player.SuperiorNPCPlayer;
import com.bgsoftware.superiorskyblock.utils.events.EventResult;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.math.BigDecimal;
import java.util.Locale;

public final class PortalsLogic {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private PortalsLogic() {

    }

    public static void handlePlayerPortal(Player player, Location portalLocation,
                                          PlayerTeleportEvent.TeleportCause teleportCause,
                                          Cancellable involvedEvent) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        if (superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        Island island = plugin.getGrid().getIslandAt(portalLocation);

        if (island == null || !plugin.getGrid().isIslandsWorld(portalLocation.getWorld()))
            return;

        if (involvedEvent != null)
            involvedEvent.setCancelled(true);

        if (superiorPlayer.isImmunedToPortals())
            return;

        World.Environment destinationEnvironment = getTargetWorld(portalLocation, teleportCause);

        if (plugin.getGrid().getIslandsWorld(island, destinationEnvironment) == null)
            return;

        if (!isIslandWorldEnabled(destinationEnvironment, island)) {
            if (!Message.WORLD_NOT_UNLOCKED.isEmpty(superiorPlayer.getUserLocale()))
                Message.SCHEMATICS.send(superiorPlayer, Message.WORLD_NOT_UNLOCKED.getMessage(
                        superiorPlayer.getUserLocale(), Formatters.CAPITALIZED_FORMATTER.format(destinationEnvironment.name())));
            return;
        }

        try {
            // If schematic was already generated, simply teleport player to destination location.
            if (island.wasSchematicGenerated(destinationEnvironment)) {
                superiorPlayer.teleport(island, destinationEnvironment);
                return;
            }

            String destinationEnvironmentName = destinationEnvironment.name().toLowerCase(Locale.ENGLISH);
            String islandSchematic = island.getSchematicName();

            Schematic schematic = plugin.getSchematics().getSchematic(islandSchematic.isEmpty() ?
                    plugin.getSchematics().getDefaultSchematic(destinationEnvironment) :
                    islandSchematic + "_" + destinationEnvironmentName);

            if (schematic == null) {
                Message.SCHEMATICS.send(superiorPlayer, ChatColor.RED + "The server hasn't added a " +
                        destinationEnvironmentName + " schematic. Please contact administrator to solve the problem. " +
                        "The format for " + destinationEnvironmentName + " schematic is \"" +
                        islandSchematic + "_" + destinationEnvironmentName + "\".");
                return;
            }

            Location schematicPlacementLocation = island.getCenter(destinationEnvironment).subtract(0, 1, 0);

            BigDecimal originalWorth = island.getRawWorth();
            BigDecimal originalLevel = island.getRawLevel();

            schematic.pasteSchematic(island, schematicPlacementLocation, () -> {
                island.setSchematicGenerate(destinationEnvironment);

                if (shouldOffsetSchematic(destinationEnvironment)) {
                    {
                        BigDecimal schematicWorth = island.getRawWorth().subtract(originalWorth);
                        EventResult<BigDecimal> eventResult = plugin.getEventsBus().callIslandChangeWorthBonusEvent(null, island,
                                IslandChangeWorthBonusEvent.Reason.SCHEMATIC, island.getBonusWorth().subtract(schematicWorth));
                        if (!eventResult.isCancelled())
                            island.setBonusWorth(eventResult.getResult());
                    }
                    {
                        BigDecimal schematicLevel = island.getRawLevel().subtract(originalLevel);
                        EventResult<BigDecimal> eventResult = plugin.getEventsBus().callIslandChangeLevelBonusEvent(null, island,
                                IslandChangeLevelBonusEvent.Reason.SCHEMATIC, island.getBonusLevel().subtract(schematicLevel));
                        if (!eventResult.isCancelled())
                            island.setBonusLevel(island.getBonusLevel().subtract(schematicLevel));
                    }
                }

                Location destinationLocation = island.getIslandHome(destinationEnvironment);

                if (destinationEnvironment == World.Environment.THE_END) {
                    plugin.getNMSDragonFight().awardTheEndAchievement(player);
                    plugin.getServices().getDragonBattleService().resetEnderDragonBattle(island);
                }

                superiorPlayer.teleport(schematic.adjustRotation(destinationLocation));
            });

        } catch (NullPointerException ignored) {
        }

    }

    public static boolean shouldOffsetSchematic(World.Environment environment) {
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

    public static World.Environment getTargetWorld(Location portalLocation, PlayerTeleportEvent.TeleportCause teleportCause) {
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

    public static boolean isIslandWorldEnabled(World.Environment environment, Island island) {
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
