package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordService;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;

public class EntityTrackingListener extends AbstractGameEventListener {

    private final LazyReference<WorldRecordService> worldRecordService = new LazyReference<WorldRecordService>() {
        @Override
        protected WorldRecordService create() {
            return plugin.getServices().getService(WorldRecordService.class);
        }
    };

    public EntityTrackingListener(SuperiorSkyblockPlugin plugin) {
        super(plugin);
        this.registerListeners();
    }

    private void onEntitySpawn(GameEvent<GameEventArgs.EntitySpawnEvent> e) {
        this.worldRecordService.get().recordEntitySpawn(e.getArgs().entity);
    }

    private void onEntityDeath(GameEvent<GameEventArgs.EntityDeathEvent> e) {
        worldRecordService.get().recordEntityDespawn(e.getArgs().entity);
    }

    /* INTERNAL */

    private void registerListeners() {
        registerCallback(GameEventType.ENTITY_SPAWN_EVENT, GameEventPriority.MONITOR, this::onEntitySpawn);
        registerCallback(GameEventType.ENTITY_DEATH_EVENT, GameEventPriority.MONITOR, this::onEntityDeath);
    }

}
