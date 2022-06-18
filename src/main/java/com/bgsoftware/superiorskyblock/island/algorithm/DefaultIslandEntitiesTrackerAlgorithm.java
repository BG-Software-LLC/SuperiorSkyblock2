package com.bgsoftware.superiorskyblock.island.algorithm;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandEntitiesTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.core.key.KeyMapImpl;
import com.bgsoftware.superiorskyblock.core.collections.CompletableFutureList;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import com.google.common.base.Preconditions;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DefaultIslandEntitiesTrackerAlgorithm implements IslandEntitiesTrackerAlgorithm {

    private static final long CALCULATE_DELAY = TimeUnit.MINUTES.toMillis(5);

    private final KeyMap<Integer> entityCounts = KeyMapImpl.createConcurrentHashMap();

    private final Island island;

    private volatile boolean beingRecalculated = false;
    private volatile long lastCalculateTime = 0L;

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

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastCalculateTime <= CALCULATE_DELAY)
            return;

        this.beingRecalculated = true;
        this.lastCalculateTime = currentTime;

        clearEntityCounts();

        KeyMap<Integer> recalculatedEntityCounts = KeyMapImpl.createConcurrentHashMap();
        CompletableFutureList<Chunk> chunks = new CompletableFutureList<>();

        for (World.Environment environment : World.Environment.values()) {
            try {
                World world = island.getCenter(environment).getWorld();
                chunks.addAll(IslandUtils.getAllChunksAsync(island, world, true, true, ChunkLoadReason.ENTITIES_RECALCULATE, chunk -> {
                    for (Entity entity : chunk.getEntities()) {
                        if (BukkitEntities.canBypassEntityLimit(entity))
                            continue;

                        Key key = BukkitEntities.getLimitEntityType(entity);

                        if (!canTrackEntity(key))
                            continue;

                        int currentEntityAmount = recalculatedEntityCounts.getOrDefault(key, 0);
                        recalculatedEntityCounts.put(key, currentEntityAmount + 1);
                    }
                }));
            } catch (Exception ignored) {
            }
        }

        BukkitExecutor.async(() -> {
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
        return island.getEntityLimit(key) != -1;
    }

}
