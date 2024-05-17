package com.bgsoftware.superiorskyblock.external.worlds;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.hooks.WorldsProvider;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.service.dragon.DragonBattleService;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.SBlockPosition;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateMap;
import com.bgsoftware.superiorskyblock.world.Dimensions;
import com.bgsoftware.superiorskyblock.world.WorldGenerator;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.BlockFace;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WorldsProvider_Default implements WorldsProvider {

    private final Set<BlockPosition> servedPositions = Sets.newHashSet();
    private final EnumerateMap<Dimension, World> islandWorlds = new EnumerateMap<>(Dimension.values());
    private final Map<UUID, Dimension> islandWorldsToDimensions = new HashMap<>();
    private final SuperiorSkyblockPlugin plugin;

    private final LazyReference<DragonBattleService> dragonBattleService = new LazyReference<DragonBattleService>() {
        @Override
        protected DragonBattleService create() {
            return plugin.getServices().getService(DragonBattleService.class);
        }
    };

    public WorldsProvider_Default(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void prepareWorlds() {
        Difficulty difficulty = Difficulty.valueOf(plugin.getSettings().getWorlds().getDifficulty());
        for (Dimension dimension : Dimension.values()) {
            SettingsManager.Worlds.DimensionConfig dimensionConfig = plugin.getSettings().getWorlds().getDimensionConfig(dimension);
            if (dimensionConfig != null && dimensionConfig.isEnabled()) {
                String worldName = dimensionConfig.getName();
                World world = loadWorld(worldName, difficulty, dimension);
                if (dimension.getEnvironment() == World.Environment.THE_END &&
                        dimensionConfig instanceof SettingsManager.Worlds.End &&
                        ((SettingsManager.Worlds.End) dimensionConfig).isDragonFight()) {
                    dragonBattleService.get().prepareEndWorld(world);
                }
            }
        }
    }

    @Override
    public World getIslandsWorld(Island island, Dimension dimension) {
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null.");
        return islandWorlds.get(dimension);
    }

    @Override
    @Deprecated
    public World getIslandsWorld(Island island, World.Environment environment) {
        return getIslandsWorld(island, Dimensions.fromEnvironment(environment));
    }

    @Override
    public Dimension getIslandsWorldDimension(World world) {
        Preconditions.checkNotNull(world, "world parameter cannot be null.");
        return islandWorldsToDimensions.get(world.getUID());
    }

    @Override
    public boolean isIslandsWorld(World world) {
        Preconditions.checkNotNull(world, "world parameter cannot be null.");
        return islandWorldsToDimensions.containsKey(world.getUID());
    }

    @Override
    public Location getNextLocation(Location previousLocation, int islandsHeight, int maxIslandSize, UUID islandOwner, UUID islandUUID) {
        Preconditions.checkNotNull(previousLocation, "previousLocation parameter cannot be null.");

        Location location = previousLocation.clone();
        location.setY(islandsHeight);
        BlockFace islandFace = getIslandFace(location);

        int islandRange = maxIslandSize * 3;

        if (islandFace == BlockFace.NORTH) {
            location.add(islandRange, 0, 0);
        } else if (islandFace == BlockFace.EAST) {
            if (location.getX() == -location.getZ())
                location.add(islandRange, 0, 0);
            else if (location.getX() == location.getZ())
                location.subtract(islandRange, 0, 0);
            else
                location.add(0, 0, islandRange);
        } else if (islandFace == BlockFace.SOUTH) {
            if (location.getX() == -location.getZ())
                location.subtract(0, 0, islandRange);
            else
                location.subtract(islandRange, 0, 0);
        } else if (islandFace == BlockFace.WEST) {
            if (location.getX() == location.getZ())
                location.add(islandRange, 0, 0);
            else
                location.subtract(0, 0, islandRange);
        }

        BlockPosition blockPosition = new SBlockPosition(location);

        if (servedPositions.contains(blockPosition) || plugin.getGrid().getIslandAt(location) != null) {
            return getNextLocation(location.clone(), islandsHeight, maxIslandSize, islandOwner, islandUUID);
        }

        servedPositions.add(blockPosition);

        return location;
    }

    @Override
    public void finishIslandCreation(Location islandLocation, UUID islandOwner, UUID islandUUID) {
        Preconditions.checkNotNull(islandLocation, "islandLocation parameter cannot be null.");
        servedPositions.remove(new SBlockPosition(islandLocation));
    }

    @Override
    public void prepareTeleport(Island island, Location location, Runnable finishCallback) {
        finishCallback.run();
    }

    @Override
    public boolean isNormalEnabled() {
        return isDimensionEnabled(Dimensions.NORMAL);
    }

    @Override
    public boolean isNormalUnlocked() {
        return isDimensionUnlocked(Dimensions.NORMAL);
    }

    @Override
    public boolean isNetherEnabled() {
        return isDimensionEnabled(Dimensions.NETHER);
    }

    @Override
    public boolean isNetherUnlocked() {
        return isDimensionUnlocked(Dimensions.NETHER);
    }

    @Override
    public boolean isEndEnabled() {
        return isDimensionEnabled(Dimensions.THE_END);
    }

    @Override
    public boolean isEndUnlocked() {
        return isDimensionUnlocked(Dimensions.THE_END);
    }

    @Override
    public boolean isDimensionEnabled(Dimension dimension) {
        SettingsManager.Worlds.DimensionConfig dimensionConfig = plugin.getSettings().getWorlds().getDimensionConfig(dimension);
        // If the config is null, it probably means another plugin registered it.
        // Therefore, we register it as enabled.
        return dimensionConfig == null || dimensionConfig.isEnabled();
    }

    @Override
    public boolean isDimensionUnlocked(Dimension dimension) {
        SettingsManager.Worlds.DimensionConfig dimensionConfig = plugin.getSettings().getWorlds().getDimensionConfig(dimension);
        // If the config is null, it probably means another plugin registered it.
        // Therefore, we register it as not unlocked by default.
        return dimensionConfig != null && dimensionConfig.isEnabled() && dimensionConfig.isUnlocked();
    }

    private BlockFace getIslandFace(Location location) {
        //Possibilities: North / East
        if (location.getX() >= location.getZ()) {
            return -location.getX() > location.getZ() ? BlockFace.NORTH : BlockFace.EAST;
        }
        //Possibilities: South / West
        else {
            return -location.getX() > location.getZ() ? BlockFace.WEST : BlockFace.SOUTH;
        }
    }

    private World loadWorld(String worldName, Difficulty difficulty, Dimension dimension) {
        if (Bukkit.getWorld(worldName) != null) {
            throw new RuntimeException("The world " + worldName + " is already loaded. This can occur by one of the following reasons:\n" +
                    "- Another plugin loaded it manually before SuperiorSkyblock.\n" +
                    "- Your level-name property in server.properties is set to " + worldName + ".");
        }

        World world = WorldCreator.name(worldName)
                .type(WorldType.NORMAL)
                .environment(dimension.getEnvironment())
                .generator(WorldGenerator.getWorldGenerator(dimension))
                .createWorld();

        world.setDifficulty(difficulty);
        islandWorlds.put(dimension, world);
        islandWorldsToDimensions.put(world.getUID(), dimension);

        plugin.getNMSWorld().removeAntiXray(world);

        if (Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv import " + worldName + " normal -g " + plugin.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv modify set generator " + plugin.getName() + " " + worldName);
        }

        return world;
    }

}
