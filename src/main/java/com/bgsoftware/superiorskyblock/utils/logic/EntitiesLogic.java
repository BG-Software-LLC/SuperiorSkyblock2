package com.bgsoftware.superiorskyblock.utils.logic;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeEntityLimits;
import com.bgsoftware.superiorskyblock.utils.entities.EntityUtils;
import org.bukkit.entity.Entity;

public final class EntitiesLogic {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private EntitiesLogic() {

    }

    public static void handleSpawn(Entity entity) {
        if (!BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeEntityLimits.class))
            return;

        Island island = plugin.getGrid().getIslandAt(entity.getLocation());

        if (island == null)
            return;

        if (!EntityUtils.canHaveLimit(entity.getType()))
            return;

        island.getEntitiesTracker().trackEntity(Key.of(entity), 1);
    }

    public static void handleDespawn(Entity entity) {
        if (!BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeEntityLimits.class))
            return;

        Island island = plugin.getGrid().getIslandAt(entity.getLocation());

        if (island == null)
            return;

        if (!EntityUtils.canHaveLimit(entity.getType()))
            return;

        island.getEntitiesTracker().untrackEntity(Key.of(entity), 1);
    }

}
