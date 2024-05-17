package com.bgsoftware.superiorskyblock.service.portals;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeLevelBonusEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeWorthBonusEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.player.PlayerStatus;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.service.dragon.DragonBattleService;
import com.bgsoftware.superiorskyblock.api.service.portals.EntityPortalResult;
import com.bgsoftware.superiorskyblock.api.service.portals.PortalsManagerService;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.collections.AutoRemovalCollection;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.events.EventsBus;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.SuperiorNPCPlayer;
import com.bgsoftware.superiorskyblock.service.IService;
import com.bgsoftware.superiorskyblock.world.Dimensions;
import com.bgsoftware.superiorskyblock.world.EntityTeleports;
import com.google.common.base.Preconditions;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PortalsManagerServiceImpl implements PortalsManagerService, IService {

    private final AutoRemovalCollection<UUID> generatingSchematicsIslands = AutoRemovalCollection.newHashSet(20, TimeUnit.SECONDS);

    private final LazyReference<DragonBattleService> dragonBattleService = new LazyReference<DragonBattleService>() {
        @Override
        protected DragonBattleService create() {
            return plugin.getServices().getService(DragonBattleService.class);
        }
    };

    private final SuperiorSkyblockPlugin plugin;

    public PortalsManagerServiceImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Class<?> getAPIClass() {
        return PortalsManagerService.class;
    }

    @Override
    public EntityPortalResult handlePlayerPortal(SuperiorPlayer superiorPlayer, Location portalLocation,
                                                 PortalType portalType, Location unused,
                                                 boolean checkImmunedPortalsStatus) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null.");
        Preconditions.checkNotNull(portalLocation, "portalLocation cannot be null.");
        Preconditions.checkNotNull(portalType, "portalType cannot be null.");
        Preconditions.checkArgument(!(superiorPlayer instanceof SuperiorNPCPlayer), "superiorPlayer cannot be an NPC.");
        Preconditions.checkArgument(portalLocation.getWorld() != null, "portalLocation's world cannot be null");

        return handlePlayerPortalInternal(superiorPlayer, portalLocation, portalType, checkImmunedPortalsStatus, null);
    }

    @Override
    public EntityPortalResult handleEntityPortal(Entity entity, Location portalLocation,
                                                 PortalType portalType, Location unused) {
        Preconditions.checkNotNull(entity, "entity cannot be null.");
        Preconditions.checkNotNull(portalLocation, "portalLocation cannot be null.");
        Preconditions.checkNotNull(portalType, "portalType cannot be null.");
        Preconditions.checkArgument(portalLocation.getWorld() != null, "portalLocation's world cannot be null");

        return handleEntityPortalInternal(entity, portalLocation, portalType, null);
    }

    @Override
    public EntityPortalResult handlePlayerPortalFromIsland(SuperiorPlayer superiorPlayer, Island island,
                                                           Location portalLocation, PortalType portalType,
                                                           boolean checkImmunedPortalsStatus) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null.");
        Preconditions.checkNotNull(island, "island cannot be null.");
        Preconditions.checkNotNull(portalLocation, "portalLocation cannot be null.");
        Preconditions.checkNotNull(portalType, "portalType cannot be null.");
        Preconditions.checkArgument(!(superiorPlayer instanceof SuperiorNPCPlayer), "superiorPlayer cannot be an NPC.");
        Preconditions.checkArgument(portalLocation.getWorld() != null, "portalLocation's world cannot be null");
        Preconditions.checkArgument(island.isInside(portalLocation), "portalLocation is not inside the island.");

        return handlePlayerPortalInternal(superiorPlayer, portalLocation, portalType, checkImmunedPortalsStatus, island);
    }

    @Override
    public EntityPortalResult handleEntityPortalFromIsland(Entity entity, Island island, Location portalLocation, PortalType portalType) {
        Preconditions.checkNotNull(entity, "entity cannot be null.");
        Preconditions.checkNotNull(island, "island cannot be null.");
        Preconditions.checkNotNull(portalLocation, "portalLocation cannot be null.");
        Preconditions.checkNotNull(portalType, "portalType cannot be null.");
        Preconditions.checkArgument(!(entity instanceof HumanEntity), "entity cannot be a Player.");
        Preconditions.checkArgument(portalLocation.getWorld() != null, "portalLocation's world cannot be null");
        Preconditions.checkArgument(island.isInside(portalLocation), "portalLocation is not inside the island.");

        return handleEntityPortalInternal(entity, portalLocation, portalType, island);
    }

    private EntityPortalResult handlePlayerPortalInternal(SuperiorPlayer superiorPlayer, Location portalLocation,
                                                          PortalType portalType, boolean checkImmunedPortalsStatus,
                                                          @Nullable Island island) {
        if (island == null) {
            island = plugin.getGrid().getIslandAt(portalLocation);

            if (island == null)
                return EntityPortalResult.PORTAL_NOT_IN_ISLAND;
        }

        if (checkImmunedPortalsStatus && superiorPlayer.hasPlayerStatus(PlayerStatus.PORTALS_IMMUNED))
            return EntityPortalResult.PLAYER_IMMUNED_TO_PORTAL;

        EntityPortalResult portalResult = simulateEntityPortalFromIsland(superiorPlayer.asPlayer(), island,
                portalLocation, portalType);

        if (portalResult == EntityPortalResult.WORLD_NOT_UNLOCKED && !Message.WORLD_NOT_UNLOCKED.isEmpty(superiorPlayer.getUserLocale())) {
            Dimension originalDestination = getTargetWorld(portalLocation, portalType);
            Message.SCHEMATICS.send(superiorPlayer, Message.WORLD_NOT_UNLOCKED.getMessage(
                    superiorPlayer.getUserLocale(), Formatters.CAPITALIZED_FORMATTER.format(originalDestination.getName())));
        }

        return portalResult;
    }

    private EntityPortalResult handleEntityPortalInternal(Entity entity, Location portalLocation,
                                                          PortalType portalType, @Nullable Island island) {
        if (island == null) {
            island = plugin.getGrid().getIslandAt(portalLocation);

            if (island == null)
                return EntityPortalResult.PORTAL_NOT_IN_ISLAND;
        }

        return simulateEntityPortalFromIsland(entity, island, portalLocation, portalType);
    }

    private EntityPortalResult simulateEntityPortalFromIsland(Entity entity, Island island, Location portalLocation,
                                                              PortalType portalType) {
        Dimension originalDestination = getTargetWorld(portalLocation, portalType);

        SettingsManager.Worlds.DimensionConfig dimensionConfig = plugin.getSettings().getWorlds().getDimensionConfig(originalDestination);
        if (dimensionConfig != null && !dimensionConfig.isEnabled()) {
            return EntityPortalResult.DESTINATION_WORLD_DISABLED;
        }

        if (plugin.getGrid().getIslandsWorld(island, originalDestination) == null) {
            return EntityPortalResult.DESTINATION_NOT_ISLAND_WORLD;
        }

        if (!island.isDimensionEnabled(originalDestination)) {
            return EntityPortalResult.WORLD_NOT_UNLOCKED;
        }

        try {
            // We want to prevent the players from being teleported in this time.
            if (generatingSchematicsIslands.contains(island.getUniqueId()))
                return EntityPortalResult.SCHEMATIC_GENERATING_COOLDOWN;

            String destinationEnvironmentName = originalDestination.getName().toLowerCase(Locale.ENGLISH);
            String islandSchematic = island.getSchematicName();

            Schematic originalSchematic = plugin.getSchematics().getSchematic(islandSchematic.isEmpty() ?
                    plugin.getSchematics().getDefaultSchematic(originalDestination) :
                    islandSchematic + "_" + destinationEnvironmentName);

            boolean schematicGenerated = island.wasSchematicGenerated(originalDestination);
            SuperiorPlayer superiorPlayer = entity instanceof Player ? plugin.getPlayers().getSuperiorPlayer(entity) : null;

            Dimension destination;
            Schematic schematic;
            boolean ignoreInvalidSchematic;

            if (superiorPlayer != null) {
                EventResult<EventsBus.PortalEventResult> eventResult = plugin.getEventsBus().callIslandEnterPortalEvent(
                        superiorPlayer, island, portalType, originalDestination, schematicGenerated ? null : originalSchematic,
                        schematicGenerated);

                if (eventResult.isCancelled())
                    return EntityPortalResult.PORTAL_EVENT_CANCELLED;

                destination = eventResult.getResult().getDestination();
                schematic = eventResult.getResult().getSchematic();
                ignoreInvalidSchematic = eventResult.getResult().isIgnoreInvalidSchematic();
            } else {
                destination = originalDestination;
                schematic = schematicGenerated ? null : originalSchematic;
                ignoreInvalidSchematic = schematicGenerated;
            }

            if (schematic == null && !ignoreInvalidSchematic) {
                if (superiorPlayer != null) {
                    Message.SCHEMATICS.send(superiorPlayer, ChatColor.RED + "The server hasn't added a " +
                            destinationEnvironmentName + " schematic. Please contact administrator to solve the problem. " +
                            "The format for " + destinationEnvironmentName + " schematic is \"" +
                            islandSchematic + "_" + destinationEnvironmentName + "\".");
                }
                return EntityPortalResult.INVALID_SCHEMATIC;
            }

            generatingSchematicsIslands.add(island.getUniqueId());

            // If schematic was already generated, or no schematic should be generated, simply
            // teleport player to destination location.
            if (schematic == null || island.wasSchematicGenerated(destination)) {
                if (superiorPlayer != null) {
                    superiorPlayer.teleport(island, destination, result -> {
                        generatingSchematicsIslands.remove(island.getUniqueId());
                    });
                } else {
                    EntityTeleports.findIslandSafeLocation(island, destination).whenComplete((safeSpot, error) -> {
                        generatingSchematicsIslands.remove(island.getUniqueId());

                        if (error == null && safeSpot != null)
                            EntityTeleports.teleport(entity, safeSpot);
                    });
                }
                return EntityPortalResult.SUCCEED;
            }

            Location schematicPlacementLocation = island.getCenter(destination).subtract(0, 1, 0);
            schematicPlacementLocation.setY(plugin.getSettings().getIslandHeight());

            BigDecimal originalWorth = island.getRawWorth();
            BigDecimal originalLevel = island.getRawLevel();

            schematic.pasteSchematic(island, schematicPlacementLocation, () -> {
                generatingSchematicsIslands.remove(island.getUniqueId());
                island.setSchematicGenerate(destination);

                SettingsManager.Worlds.DimensionConfig destinationConfig = plugin.getSettings().getWorlds().getDimensionConfig(destination);
                if (destinationConfig != null && destinationConfig.isSchematicOffset()) {
                    {
                        BigDecimal schematicWorth = island.getRawWorth().subtract(originalWorth);
                        EventResult<BigDecimal> bonusEventResult = plugin.getEventsBus().callIslandChangeWorthBonusEvent(null, island,
                                IslandChangeWorthBonusEvent.Reason.SCHEMATIC, island.getBonusWorth().subtract(schematicWorth));
                        if (!bonusEventResult.isCancelled())
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

                if (destination.getEnvironment() == World.Environment.THE_END && superiorPlayer != null) {
                    plugin.getNMSDragonFight().awardTheEndAchievement((Player) entity);
                    this.dragonBattleService.get().resetEnderDragonBattle(island, destination);
                }

                if (superiorPlayer != null) {
                    superiorPlayer.teleport(schematic.adjustRotation(destinationLocation));
                } else {
                    EntityTeleports.teleport(entity, schematic.adjustRotation(destinationLocation));
                }
            }, error -> {
                generatingSchematicsIslands.remove(island.getUniqueId());
                error.printStackTrace();
                if (superiorPlayer != null)
                    Message.CREATE_WORLD_FAILURE.send(superiorPlayer);
            });

        } catch (NullPointerException ignored) {
        }

        return EntityPortalResult.SUCCEED;
    }

    private static Dimension getTargetWorld(Location portalLocation, PortalType portalType) {
        World.Environment portalEnvironment = portalLocation.getWorld().getEnvironment();
        World.Environment environment;

        switch (portalType) {
            case ENDER:
                environment = World.Environment.THE_END;
                break;
            case NETHER:
                environment = World.Environment.NETHER;
                break;
            default:
                environment = World.Environment.NORMAL;
                break;
        }

        return environment == portalEnvironment ? Dimensions.NORMAL : Dimensions.fromEnvironment(environment);
    }

}
