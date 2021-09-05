package com.bgsoftware.superiorskyblock.utils.logic;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.player.SuperiorNPCPlayer;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.math.BigDecimal;

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

        if (!isIslandWorldEnabled(destinationEnvironment, island)) {
            if (!Locale.WORLD_NOT_UNLOCKED.isEmpty(superiorPlayer.getUserLocale()))
                Locale.sendSchematicMessage(superiorPlayer, Locale.WORLD_NOT_UNLOCKED.getMessage(
                        superiorPlayer.getUserLocale(), StringUtils.format(destinationEnvironment.name())));
            return;
        }

        try {
            Location destinationLocation = island.getTeleportLocation(destinationEnvironment);

            // If schematic was already generated, simply teleport player to destination location.
            if (island.wasSchematicGenerated(destinationEnvironment)) {
                superiorPlayer.teleport(destinationLocation);
                return;
            }

            String destinationEnvironmentName = destinationEnvironment.name().toLowerCase();
            String islandSchematic = island.getSchematicName();

            Schematic schematic = plugin.getSchematics().getSchematic(islandSchematic.isEmpty() ?
                    plugin.getSchematics().getDefaultSchematic(destinationEnvironment) :
                    islandSchematic + "_" + destinationEnvironmentName);

            if(schematic == null){
                Locale.sendSchematicMessage(superiorPlayer, ChatColor.RED + "The server hasn't added a " +
                        destinationEnvironmentName + " schematic. Please contact administrator to solve the problem. " +
                        "The format for " + destinationEnvironmentName + " schematic is \"" +
                        islandSchematic + "_" + destinationEnvironmentName + "\".");
                return;
            }

            island.setSchematicGenerate(destinationEnvironment);

            Location schematicPlacementLocation = island.getCenter(destinationEnvironment).subtract(0, 1, 0);

            BigDecimal originalWorth = island.getRawWorth();
            BigDecimal originalLevel = island.getRawLevel();

            schematic.pasteSchematic(island, schematicPlacementLocation, () -> {
                if (shouldOffsetSchematic(destinationEnvironment)) {
                    BigDecimal schematicWorth = island.getRawWorth().subtract(originalWorth),
                            schematicLevel = island.getRawLevel().subtract(originalLevel);
                    island.setBonusWorth(island.getBonusWorth().subtract(schematicWorth));
                    island.setBonusLevel(island.getBonusLevel().subtract(schematicLevel));
                }

                if (destinationEnvironment == World.Environment.THE_END) {
                    plugin.getNMSDragonFight().awardTheEndAchievement(player);
                    if (plugin.getSettings().getWorlds().getEnd().isDragonFight())
                        plugin.getNMSDragonFight().startDragonBattle(island, destinationLocation);
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
