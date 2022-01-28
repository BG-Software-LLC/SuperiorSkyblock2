package com.bgsoftware.superiorskyblock.island.algorithms;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandEntitiesTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMap;
import com.bgsoftware.superiorskyblock.structure.CompletableFutureList;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.utils.entities.EntityUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.google.common.base.Preconditions;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.Collections;
import java.util.Map;

public final class DefaultIslandEntitiesTrackerAlgorithm implements IslandEntitiesTrackerAlgorithm {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final KeyMap<Integer> entityCounts = new KeyMap<>();

    private final Island island;

    private volatile boolean beingRecalculated = false;

    public DefaultIslandEntitiesTrackerAlgorithm(Island island) {
        this.island = island;
    }

    @Override
    public boolean trackEntity(Key key, int amount) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");

        if (amount <= 0)
            return false;

        if (!canTrackEntity(key))
            return false;

        PluginDebugger.debug("Action: Entity Spawn, Island: " + island.getOwner().getName() +
                ", Entity: " + key + ", Amount: " + amount);

        int currentAmount = entityCounts.getOrDefault(key, 0);
        entityCounts.put(key, currentAmount + amount);

        return true;
    }

    @Override
    public boolean untrackEntity(Key key, int amount) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");

        if (amount <= 0)
            return false;

        if (!canTrackEntity(key))
            return false;

        PluginDebugger.debug("Action: Entity Despawn, Island: " + island.getOwner().getName() +
                ", Entity: " + key + ", Amount: " + amount);

        int currentAmount = entityCounts.getOrDefault(key, -1);

        if (currentAmount != -1) {
            if (currentAmount > amount) {
                entityCounts.put(key, currentAmount - amount);
            } else {
                entityCounts.remove(key);
            }
        }

        return true;
    }

    @Override
    public int getEntityCount(Key key) {
        return entityCounts.getOrDefault(key, 0);
    }

    @Override
    public Map<Key, Integer> getEntitiesCounts() {
        return Collections.unmodifiableMap(entityCounts);
    }

    @Override
    public void clearEntityCounts() {
        this.entityCounts.clear();
    }

    @Override
    public void recalculateEntityCounts() {
        if (beingRecalculated)
            return;

        beingRecalculated = true;

        clearEntityCounts();

        KeyMap<Integer> recalculatedEntityCounts = new KeyMap<>();
        CompletableFutureList<Chunk> chunks = new CompletableFutureList<>();

        for (World.Environment environment : World.Environment.values()) {
            try {
                chunks.addAll(island.getAllChunksAsync(environment, true, true, chunk -> {
                    for (Entity entity : chunk.getEntities()) {
                        if (EntityUtils.canBypassEntityLimit(entity))
                            continue;

                        Key key = EntityUtils.getLimitEntityType(entity);

                        if (!canTrackEntity(key))
                            continue;

                        int entityAmount = Math.max(1, plugin.getProviders().getEntityProvider().getEntityAmount(entity));

                        int currentEntityAmount = recalculatedEntityCounts.getOrDefault(key, 0);
                        recalculatedEntityCounts.put(key, currentEntityAmount + entityAmount);
                    }
                }));
            } catch (Exception ignored) {
            }
        }

        Executor.async(() -> {
            try {
                //Waiting for all the chunks to load
                chunks.forEachCompleted(chunk -> {
                }, error -> {
                });

                if (!this.entityCounts.isEmpty()) {
                    for (Map.Entry<Key, Integer> entry : this.entityCounts.entrySet()) {
                        Integer currentAmount = recalculatedEntityCounts.remove(entry.getKey());
                        if (currentAmount != null)
                            entry.setValue(entry.getValue() + currentAmount);
                    }
                }

                if (!recalculatedEntityCounts.isEmpty()) {
                    this.entityCounts.putAll(recalculatedEntityCounts);
                }
            } finally {
                beingRecalculated = false;
            }
        });
    }

    private boolean canTrackEntity(Key key) {
        return island.getEntityLimit(key) != IslandUtils.NO_LIMIT.get();
    }

}
