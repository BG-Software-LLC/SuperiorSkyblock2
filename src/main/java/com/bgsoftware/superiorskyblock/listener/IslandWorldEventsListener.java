package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.service.hologram.HologramsService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import org.bukkit.Location;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.function.Predicate;

public class IslandWorldEventsListener extends AbstractGameEventListener {

    // Shared by both callbacks below, which fire constantly.
    private static final Predicate<SuperiorPlayer> ONLINE_AND_NOT_AFK =
            superiorPlayer -> superiorPlayer.isOnline() && !superiorPlayer.isAFK();

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

        registerCallback(GameEventType.ENTITY_SPAWN_EVENT, GameEventPriority.MONITOR, this::onEnderDragonSpawn);
    }

    private void onBlockRedstone(GameEvent<GameEventArgs.BlockRedstoneEvent> e) {
        Island island = e.getArgs().islandResolver.get();
        if (island == null || island.isSpawn())
            return;

        if ((plugin.getSettings().isDisableRedstoneOffline() && !island.isCurrentlyActive()) ||
                (plugin.getSettings().getAFKIntegrations().isDisableRedstone() &&
                        !island.anyPlayerInsideMatches(ONLINE_AND_NOT_AFK))) {
            if (Log.isDebugged(Debug.DISABLE_REDSTONE)) {
                try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                    Log.debug(Debug.DISABLE_REDSTONE, island.getOwner().getName(), e.getArgs().block.getLocation(wrapper.getHandle()));
                }
            }

            e.setCancelled();
        }
    }

    private void onEntitySpawn(GameEvent<GameEventArgs.EntitySpawnEvent> e) {
        Entity entity = e.getArgs().entity;

        if (hologramsService.get().isHologram(entity))
            return;

        Island island = e.getArgs().islandResolver.get();
        if (island == null || island.isSpawn() || island.anyPlayerInsideMatches(ONLINE_AND_NOT_AFK))
            return;

        e.setCancelled();
    }

    private void onEnderDragonSpawn(GameEvent<GameEventArgs.EntitySpawnEvent> e) {
        // Dragon fights add their dragons with the default reason. Older versions add them at the vanilla spawn
        // position before moving them to their island, so they must not be matched with the island there.
        if (!(e.getArgs().entity instanceof EnderDragon) || e.getArgs().spawnReason == CreatureSpawnEvent.SpawnReason.DEFAULT)
            return;

        EnderDragon enderDragon = (EnderDragon) e.getArgs().entity;

        Island island = e.getArgs().islandResolver.get();
        if (island != null)
            plugin.getNMSDragonFight().replaceEnderDragon(island, enderDragon);
    }

}
