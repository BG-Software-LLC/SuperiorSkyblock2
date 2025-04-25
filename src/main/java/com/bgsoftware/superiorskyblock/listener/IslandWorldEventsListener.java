package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.service.hologram.HologramsService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class IslandWorldEventsListener extends AbstractGameEventListener {

    private final LazyReference<HologramsService> hologramsService = new LazyReference<HologramsService>() {
        @Override
        protected HologramsService create() {
            return plugin.getServices().getService(HologramsService.class);
        }
    };

    public IslandWorldEventsListener(SuperiorSkyblockPlugin plugin) {
        super(plugin);

        if (plugin.getSettings().isDisableRedstoneOffline() || plugin.getSettings().getAFKIntegrations().isDisableRedstone())
            registerCallback(GameEventType.BLOCK_REDSTONE_EVENT, GameEventPriority.HIGHEST, this::onBlockRedstone);

        if (plugin.getSettings().getAFKIntegrations().isDisableSpawning())
            registerCallback(GameEventType.ENTITY_SPAWN_EVENT, GameEventPriority.HIGHEST, this::onEntitySpawn);
    }

    private void onBlockRedstone(GameEvent<GameEventArgs.BlockRedstoneEvent> e) {
        Island island;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            island = plugin.getGrid().getIslandAt(e.getArgs().block.getLocation(wrapper.getHandle()));
        }

        if (island == null || island.isSpawn())
            return;

        if ((plugin.getSettings().isDisableRedstoneOffline() && !island.isCurrentlyActive()) ||
                (plugin.getSettings().getAFKIntegrations().isDisableRedstone() &&
                        island.getAllPlayersInside().stream().allMatch(SuperiorPlayer::isAFK))) {
            e.setCancelled();
        }
    }

    private void onEntitySpawn(GameEvent<GameEventArgs.EntitySpawnEvent> e) {
        Entity entity = e.getArgs().entity;

        if (hologramsService.get().isHologram(entity))
            return;

        Island island;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            island = plugin.getGrid().getIslandAt(entity.getLocation(wrapper.getHandle()));
        }

        if (island == null || island.isSpawn() || !island.getAllPlayersInside().stream().allMatch(SuperiorPlayer::isAFK))
            return;

        e.setCancelled();
    }

}
