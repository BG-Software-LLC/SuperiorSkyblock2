package com.bgsoftware.superiorskyblock.service.dragon;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.service.dragon.DragonBattleResetResult;
import com.bgsoftware.superiorskyblock.api.service.dragon.DragonBattleService;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.service.IService;
import com.bgsoftware.superiorskyblock.world.Dimensions;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;

public class DragonBattleServiceImpl implements DragonBattleService, IService {

    private final SuperiorSkyblockPlugin plugin;

    public DragonBattleServiceImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Class<?> getAPIClass() {
        return DragonBattleService.class;
    }

    @Override
    public void prepareEndWorld(World bukkitWorld) {
        Preconditions.checkNotNull(bukkitWorld, "world parameter cannot be null");
        Preconditions.checkArgument(bukkitWorld.getEnvironment() == World.Environment.THE_END, "world must be the_end environment");
        plugin.getNMSDragonFight().prepareEndWorld(bukkitWorld);
    }

    @Nullable
    @Override
    public EnderDragon getEnderDragon(Island island, Dimension dimension) {
        Preconditions.checkNotNull(island, "island parameter cannot be null");
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null");
        Preconditions.checkArgument(dimension.getEnvironment() == World.Environment.THE_END, "dimension must be the_end environment");

        return plugin.getNMSDragonFight().getEnderDragon(island, dimension);
    }

    @Override
    @Deprecated
    public EnderDragon getEnderDragon(Island island) {
        return getEnderDragon(island, Dimensions.THE_END);
    }

    @Override
    public void stopEnderDragonBattle(Island island, Dimension dimension) {
        Preconditions.checkNotNull(island, "island parameter cannot be null");
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null");
        Preconditions.checkArgument(dimension.getEnvironment() == World.Environment.THE_END, "dimension must be the_end environment");

        plugin.getNMSDragonFight().removeDragonBattle(island, dimension);
    }

    @Override
    @Deprecated
    public void stopEnderDragonBattle(Island island) {
        stopEnderDragonBattle(island, Dimensions.THE_END);
    }

    @Override
    public DragonBattleResetResult resetEnderDragonBattle(Island island, Dimension dimension) {
        Preconditions.checkNotNull(island, "island parameter cannot be null");
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null");
        Preconditions.checkArgument(dimension.getEnvironment() == World.Environment.THE_END, "dimension must be the_end environment");

        SettingsManager.Worlds.End dimensionConfig = (SettingsManager.Worlds.End) plugin.getSettings().getWorlds().getDimensionConfig(dimension);

        if (dimensionConfig == null || !dimensionConfig.isEnabled() || !dimensionConfig.isDragonFight())
            return DragonBattleResetResult.DRAGON_BATTLES_DISABLED;

        if (!island.isDimensionEnabled(dimension))
            return DragonBattleResetResult.WORLD_NOT_UNLOCKED;

        if (!island.wasSchematicGenerated(dimension))
            return DragonBattleResetResult.WORLD_NOT_GENERATED;

        stopEnderDragonBattle(island, dimension);

        Location islandCenter = island.getCenter(dimension);

        plugin.getNMSDragonFight().startDragonBattle(island,
                dimensionConfig.getPortalOffset().applyToLocation(islandCenter));

        return DragonBattleResetResult.SUCCESS;
    }

    @Override
    @Deprecated
    public DragonBattleResetResult resetEnderDragonBattle(Island island) {
        return resetEnderDragonBattle(island, Dimensions.THE_END);
    }

}
