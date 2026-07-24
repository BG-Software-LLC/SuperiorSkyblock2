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
import com.bgsoftware.superiorskyblock.core.IslandWorlds;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.collections.AutoRemovalCollection;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.SuperiorNPCPlayer;
import com.bgsoftware.superiorskyblock.service.IService;
import com.bgsoftware.superiorskyblock.world.EntityTeleports;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
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
                                                 PortalType portalType, Location destinationLocation,
                                                 boolean checkImmunedPortalsStatus) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null.");
        Preconditions.checkNotNull(portalLocation, "portalLocation cannot be null.");
        Preconditions.checkNotNull(portalType, "portalType cannot be null.");
        Preconditions.checkNotNull(destinationLocation, "destinationLocation cannot be null.");
        Preconditions.checkArgument(!(superiorPlayer instanceof SuperiorNPCPlayer), "superiorPlayer cannot be an NPC.");
        Preconditions.checkArgument(portalLocation.getWorld() != null, "portalLocation's world cannot be null");
        Preconditions.checkArgument(destinationLocation.getWorld() != null, "destinationLocation's world cannot be null");

        Dimension destinationDimension = plugin.getGrid().getIslandsWorldDimension(destinationLocation.getWorld());
        if (destinationDimension == null)
            return EntityPortalResult.DESTINATION_NOT_ISLAND_WORLD;

        return handlePlayerPortalInternal(superiorPlayer, portalLocation, portalType, destinationDimension, checkImmunedPortalsStatus, null);
    }

    @Override
    public EntityPortalResult handleEntityPortal(Entity entity, Location portalLocation,
                                                 PortalType portalType, Location destinationLocation) {
        Preconditions.checkNotNull(entity, "entity cannot be null.");
        Preconditions.checkNotNull(portalLocation, "portalLocation cannot be null.");
        Preconditions.checkNotNull(portalType, "portalType cannot be null.");
        Preconditions.checkNotNull(destinationLocation, "destinationLocation cannot be null.");
        Preconditions.checkArgument(portalLocation.getWorld() != null, "portalLocation's world cannot be null");
        Preconditions.checkArgument(destinationLocation.getWorld() != null, "destinationLocation's world cannot be null");

        Dimension destinationDimension = plugin.getGrid().getIslandsWorldDimension(destinationLocation.getWorld());
        if (destinationDimension == null)
            return EntityPortalResult.DESTINATION_NOT_ISLAND_WORLD;

        return handleEntityPortalInternal(entity, portalLocation, portalType, destinationDimension, null);
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

        Dimension portalDimension = plugin.getGrid().getIslandsWorldDimension(portalLocation.getWorld());
        if (portalDimension == null)
            return EntityPortalResult.PORTAL_NOT_IN_ISLAND;

        Dimension destinationDimension = getDestinationDimension(portalDimension, portalType);
        if (destinationDimension == null)
            return EntityPortalResult.DESTINATION_NOT_ISLAND_WORLD;

        return handlePlayerPortalInternal(superiorPlayer, portalLocation, portalType, destinationDimension, checkImmunedPortalsStatus, island);
    }

    @Override
    public EntityPortalResult handlePlayerPortalFromIsland(SuperiorPlayer superiorPlayer, Island island,
                                                           Location portalLocation, PortalType portalType,
                                                           Location destinationLocation, boolean checkImmunedPortalsStatus) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null.");
        Preconditions.checkNotNull(island, "island cannot be null.");
        Preconditions.checkNotNull(portalLocation, "portalLocation cannot be null.");
        Preconditions.checkNotNull(portalType, "portalType cannot be null.");
        Preconditions.checkNotNull(destinationLocation, "destinationLocation cannot be null.");
        Preconditions.checkArgument(!(superiorPlayer instanceof SuperiorNPCPlayer), "superiorPlayer cannot be an NPC.");
        Preconditions.checkArgument(portalLocation.getWorld() != null, "portalLocation's world cannot be null");
        Preconditions.checkArgument(island.isInside(portalLocation), "portalLocation is not inside the island.");
        Preconditions.checkArgument(destinationLocation.getWorld() != null, "destinationLocation's world cannot be null");
        Preconditions.checkArgument(island.isInside(destinationLocation), "destinationLocation is not inside the island.");

        Dimension destinationDimension = plugin.getGrid().getIslandsWorldDimension(destinationLocation.getWorld());
        if (destinationDimension == null)
            return EntityPortalResult.DESTINATION_NOT_ISLAND_WORLD;

        return handlePlayerPortalInternal(superiorPlayer, portalLocation, portalType, destinationDimension, checkImmunedPortalsStatus, island);
    }

    @Override
    public EntityPortalResult handleEntityPortalFromIsland(Entity entity, Island island, Location portalLocation,
                                                           PortalType portalType) {
        Preconditions.checkNotNull(entity, "entity cannot be null.");
        Preconditions.checkNotNull(island, "island cannot be null.");
        Preconditions.checkNotNull(portalLocation, "portalLocation cannot be null.");
        Preconditions.checkNotNull(portalType, "portalType cannot be null.");
        Preconditions.checkArgument(!(entity instanceof HumanEntity), "entity cannot be a Player.");
        Preconditions.checkArgument(portalLocation.getWorld() != null, "portalLocation's world cannot be null");
        Preconditions.checkArgument(island.isInside(portalLocation), "portalLocation is not inside the island.");

        Dimension portalDimension = plugin.getGrid().getIslandsWorldDimension(portalLocation.getWorld());
        if (portalDimension == null)
            return EntityPortalResult.PORTAL_NOT_IN_ISLAND;

        Dimension destinationDimension = getDestinationDimension(portalDimension, portalType);
        if (destinationDimension == null)
            return EntityPortalResult.DESTINATION_NOT_ISLAND_WORLD;

        return handleEntityPortalInternal(entity, portalLocation, portalType, destinationDimension, island);
    }

    @Override
    public EntityPortalResult handleEntityPortalFromIsland(Entity entity, Island island, Location portalLocation,
                                                           PortalType portalType, Location destinationLocation) {
        Preconditions.checkNotNull(entity, "entity cannot be null.");
        Preconditions.checkNotNull(island, "island cannot be null.");
        Preconditions.checkNotNull(portalLocation, "portalLocation cannot be null.");
        Preconditions.checkNotNull(portalType, "portalType cannot be null.");
        Preconditions.checkNotNull(destinationLocation, "destinationLocation cannot be null.");
        Preconditions.checkArgument(!(entity instanceof HumanEntity), "entity cannot be a Player.");
        Preconditions.checkArgument(portalLocation.getWorld() != null, "portalLocation's world cannot be null");
        Preconditions.checkArgument(island.isInside(portalLocation), "portalLocation is not inside the island.");
        Preconditions.checkArgument(destinationLocation.getWorld() != null, "destinationLocation's world cannot be null");
        Preconditions.checkArgument(island.isInside(destinationLocation), "destinationLocation is not inside the island.");

        Dimension destinationDimension = plugin.getGrid().getIslandsWorldDimension(destinationLocation.getWorld());
        if (destinationDimension == null)
            return EntityPortalResult.DESTINATION_NOT_ISLAND_WORLD;

        return handleEntityPortalInternal(entity, portalLocation, portalType, destinationDimension, island);
    }

    private EntityPortalResult handlePlayerPortalInternal(SuperiorPlayer superiorPlayer, Location portalLocation,
                                                          PortalType portalType, Dimension destinationDimension,
                                                          boolean checkImmunedPortalsStatus, @Nullable Island island) {
        if (island == null) {
            island = plugin.getGrid().getIslandAt(portalLocation);

            if (island == null)
                return EntityPortalResult.PORTAL_NOT_IN_ISLAND;
        }

        if (checkImmunedPortalsStatus && superiorPlayer.hasPlayerStatus(PlayerStatus.PORTALS_IMMUNED))
            return EntityPortalResult.PLAYER_IMMUNED_TO_PORTAL;

        EntityPortalResult portalResult = simulateEntityPortalFromIsland(superiorPlayer.asPlayer(), island,
                portalType, destinationDimension);

        if (portalResult == EntityPortalResult.WORLD_NOT_UNLOCKED) {
            Message.WORLD_NOT_UNLOCKED.send(superiorPlayer, Formatters.CAPITALIZED_FORMATTER.format(destinationDimension.getName()));
        } else if (portalResult == EntityPortalResult.DESTINATION_WORLD_DISABLED) {
            Message.WORLD_NOT_ENABLED.send(superiorPlayer, Formatters.CAPITALIZED_FORMATTER.format(destinationDimension.getName()));
        }

        return portalResult;
    }

    private EntityPortalResult handleEntityPortalInternal(Entity entity, Location portalLocation, PortalType portalType,
                                                          Dimension destinationDimension, @Nullable Island island) {
        if (island == null) {
            island = plugin.getGrid().getIslandAt(portalLocation);

            if (island == null)
                return EntityPortalResult.PORTAL_NOT_IN_ISLAND;
        }

        return simulateEntityPortalFromIsland(entity, island, portalType, destinationDimension);
    }

    private EntityPortalResult simulateEntityPortalFromIsland(Entity entity, Island island, PortalType portalType,
                                                              Dimension portalDestination) {
        {
            SettingsManager.Worlds.DimensionConfig destinationConfig = portalDestination == null ? null :
                    plugin.getSettings().getWorlds().getDimensionConfig(portalDestination);
            if (portalDestination == null || destinationConfig == null || !destinationConfig.isEnabled()) {
                return EntityPortalResult.DESTINATION_WORLD_DISABLED;
            }
        }

        if (plugin.getGrid().getIslandsWorld(island, portalDestination) == null) {
            return EntityPortalResult.DESTINATION_NOT_ISLAND_WORLD;
        }

        if (!island.isDimensionEnabled(portalDestination)) {
            return EntityPortalResult.WORLD_NOT_UNLOCKED;
        }

        try {
            // We want to prevent the players from being teleported in this time.
            if (generatingSchematicsIslands.contains(island.getUniqueId()))
                return EntityPortalResult.SCHEMATIC_GENERATING_COOLDOWN;

            String destinationDimensionName = portalDestination.getName().toLowerCase(Locale.ENGLISH);
            String islandSchematic = island.getSchematicName();

            Schematic originalSchematic = plugin.getSchematics().getSchematic(islandSchematic.isEmpty() ?
                    plugin.getSchematics().getDefaultSchematic(portalDestination) :
                    islandSchematic + "_" + destinationDimensionName);

            boolean schematicGenerated = island.wasSchematicGenerated(portalDestination);
            SuperiorPlayer superiorPlayer = entity instanceof Player ? plugin.getPlayers().getSuperiorPlayer(entity) : null;

            Dimension destination;
            Schematic schematic;
            boolean ignoreInvalidSchematic;

            if (superiorPlayer != null) {
                PluginEvent<PluginEventArgs.IslandEnterPortal> event = PluginEventsFactory.callIslandEnterPortalEvent(
                        island, superiorPlayer, portalType, portalDestination, schematicGenerated ? null : originalSchematic,
                        schematicGenerated);

                if (event.isCancelled())
                    return EntityPortalResult.PORTAL_EVENT_CANCELLED;

                destination = event.getArgs().destination;
                schematic = event.getArgs().schematic;
                ignoreInvalidSchematic = event.getArgs().ignoreInvalidSchematic;
            } else {
                destination = portalDestination;
                schematic = schematicGenerated ? null : originalSchematic;
                ignoreInvalidSchematic = schematicGenerated;
            }

            if (schematic == null && !ignoreInvalidSchematic) {
                if (superiorPlayer != null) {
                    String schematicName = islandSchematic + "_" + destinationDimensionName;
                    Message.SCHEMATIC_NOT_ADDED.send(superiorPlayer, destinationDimensionName, schematicName);
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

            IslandWorlds.accessIslandWorldAsync(island, destination, true, islandWorldResult -> {
                islandWorldResult.ifRight(Throwable::printStackTrace).ifLeft(world -> {
                    Location centerLocation = island.getCenter(destination);
                    Location schematicPlacementLocation = centerLocation.getBlock().getRelative(BlockFace.DOWN).getLocation();

                    BigDecimal originalWorth = island.getRawWorth();
                    BigDecimal originalLevel = island.getRawLevel();

                    schematic.pasteSchematic(island, schematicPlacementLocation, () -> {
                        generatingSchematicsIslands.remove(island.getUniqueId());
                        island.setSchematicGenerate(destination);

                        SettingsManager.Worlds.DimensionConfig destinationConfig = plugin.getSettings().getWorlds().getDimensionConfig(destination);
                        if (destinationConfig != null && destinationConfig.isSchematicOffset()) {
                            {
                                BigDecimal schematicWorth = island.getRawWorth().subtract(originalWorth);
                                PluginEvent<PluginEventArgs.IslandChangeWorthBonus> event = PluginEventsFactory.callIslandChangeWorthBonusEvent(
                                        island, (SuperiorPlayer) null, IslandChangeWorthBonusEvent.Reason.SCHEMATIC, island.getBonusWorth().subtract(schematicWorth));
                                if (!event.isCancelled())
                                    island.setBonusWorth(event.getArgs().worthBonus);
                            }
                            {
                                BigDecimal schematicLevel = island.getRawLevel().subtract(originalLevel);
                                PluginEvent<PluginEventArgs.IslandChangeLevelBonus> event = PluginEventsFactory.callIslandChangeLevelBonusEvent(
                                        island, (SuperiorPlayer) null, IslandChangeLevelBonusEvent.Reason.SCHEMATIC, island.getBonusLevel().subtract(schematicLevel));
                                if (!event.isCancelled())
                                    island.setBonusLevel(event.getArgs().levelBonus);
                            }
                        }

                        Location homeLocation = schematic.adjustRotation(centerLocation);
                        island.setIslandHome(homeLocation);

                        if (destination.getEnvironment() == World.Environment.THE_END && superiorPlayer != null) {
                            plugin.getNMSDragonFight().awardTheEndAchievement((Player) entity);
                            this.dragonBattleService.get().resetEnderDragonBattle(island, destination);
                        }

                        if (superiorPlayer != null) {
                            superiorPlayer.teleport(homeLocation);
                        } else {
                            EntityTeleports.teleport(entity, homeLocation);
                        }
                    }, error -> {
                        generatingSchematicsIslands.remove(island.getUniqueId());
                        error.printStackTrace();
                        if (superiorPlayer != null)
                            Message.CREATE_WORLD_FAILURE.send(superiorPlayer);
                    });
                });
            });

        } catch (NullPointerException ignored) {
        }

        return EntityPortalResult.SUCCEED;
    }

    @Nullable
    private Dimension getDestinationDimension(Dimension dimension, PortalType portalType) {
        SettingsManager.Worlds.DimensionConfig dimensionConfig = plugin.getSettings().getWorlds().getDimensionConfig(dimension);
        if (dimensionConfig == null)
            return null;

        return dimensionConfig.getPortalDestination(portalType);
    }

}
