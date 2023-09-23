package com.bgsoftware.superiorskyblock.service.dragon;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.service.dragon.DragonBattleResetResult;
import com.bgsoftware.superiorskyblock.api.service.dragon.DragonBattleService;
import com.bgsoftware.superiorskyblock.service.IService;
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
        Preconditions.checkState(bukkitWorld.getEnvironment() == World.Environment.THE_END, "world must be the_end environment.");
        plugin.getNMSDragonFight().prepareEndWorld(bukkitWorld);
    }

    @Nullable
    @Override
    public EnderDragon getEnderDragon(Island island) {
        return plugin.getNMSDragonFight().getEnderDragon(island);
    }

    @Override
    public void stopEnderDragonBattle(Island island) {
        plugin.getNMSDragonFight().removeDragonBattle(island);
    }

    @Override
    public DragonBattleResetResult resetEnderDragonBattle(Island island) {
        if (!plugin.getSettings().getWorlds().getEnd().isDragonFight())
            return DragonBattleResetResult.DRAGON_BATTLES_DISABLED;

        if (!island.isEndEnabled())
            return DragonBattleResetResult.WORLD_NOT_UNLOCKED;

        if (!island.wasSchematicGenerated(World.Environment.THE_END))
            return DragonBattleResetResult.WORLD_NOT_GENERATED;

        stopEnderDragonBattle(island);

        Location islandCenter = island.getCenter(World.Environment.THE_END);

        plugin.getNMSDragonFight().startDragonBattle(island, plugin.getSettings().getWorlds().getEnd()
                .getPortalOffset().applyToLocation(islandCenter));

        return DragonBattleResetResult.SUCCESS;
    }

}
