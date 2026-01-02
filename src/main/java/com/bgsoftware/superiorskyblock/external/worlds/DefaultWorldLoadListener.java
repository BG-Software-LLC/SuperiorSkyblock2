package com.bgsoftware.superiorskyblock.external.worlds;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.hooks.listener.IWorldLoadListener;
import com.bgsoftware.superiorskyblock.api.hooks.world.WorldLoadFlags;
import com.bgsoftware.superiorskyblock.api.service.dragon.DragonBattleService;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import org.bukkit.World;

public class DefaultWorldLoadListener implements IWorldLoadListener {

    private final LazyReference<DragonBattleService> dragonBattleService = new LazyReference<DragonBattleService>() {
        @Override
        protected DragonBattleService create() {
            return plugin.getServices().getService(DragonBattleService.class);
        }
    };

    private final SuperiorSkyblockPlugin plugin;

    public DefaultWorldLoadListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onWorldLoad(World world, Dimension worldDimension, @WorldLoadFlags int flags) {
        if ((flags & WorldLoadFlags.END_DRAGON_FIGHT) != 0) {
            SettingsManager.Worlds.DimensionConfig dimensionConfig =
                    plugin.getSettings().getWorlds().getDimensionConfig(worldDimension);

            if (worldDimension.getEnvironment() == World.Environment.THE_END &&
                    dimensionConfig instanceof SettingsManager.Worlds.End &&
                    ((SettingsManager.Worlds.End) dimensionConfig).isDragonFight()) {
                dragonBattleService.get().prepareEndWorld(world);
            }
        }
        if ((flags & WorldLoadFlags.REMOVE_ANTI_XRAY) != 0)
            plugin.getNMSWorld().removeAntiXray(world);
        if ((flags & WorldLoadFlags.UPDATE_OCEAN_LEVEL) != 0)
            plugin.getNMSWorld().setOceanLevel(world);
        if ((flags & WorldLoadFlags.LISTEN_BLOCK_CHANGES) != 0)
            plugin.getNMSWorld().listenBlockStateChanges(world);
    }

}
