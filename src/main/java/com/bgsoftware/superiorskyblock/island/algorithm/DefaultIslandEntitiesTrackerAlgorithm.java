package com.bgsoftware.superiorskyblock.island.algorithm;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChunkFlags;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandEntitiesTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.Counter;
import com.bgsoftware.superiorskyblock.core.collections.CompletableFutureList;
import com.bgsoftware.superiorskyblock.core.database.bridge.IslandsDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.KeyMaps;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.google.common.base.Preconditions;
import org.bukkit.World;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DefaultIslandEntitiesTrackerAlgorithm implements IslandEntitiesTrackerAlgorithm {

    private static final long CALCULATE_DELAY = TimeUnit.MINUTES.toMillis(5);

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final KeyMap<Integer> entityCounts = KeyMaps.createConcurrentHashMap(KeyIndicator.ENTITY_TYPE);

    private final Island island;

    private volatile boolean beingRecalculated = false;
    private volatile long lastCalculateTime = 0L;

    public DefaultIslandEntitiesTrackerAlgorithm(Island island) {
        this.island = island;
    }

    @Override
    public boolean trackEntity(Key key, int amount) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");

        Log.debug(Debug.ENTITY_SPAWN, island.getOwner().getName(), key, amount);

        if (amount <= 0) {
            Log.debugResult(Debug.ENTITY_SPAWN, "Return", "Negative Amount");
            return false;
        }

        if (!canTrackEntity(key)) {
            Log.debugResult(Debug.ENTITY_SPAWN, "Return", "Cannot Track Entity");
            return false;
        }

        int currentAmount = entityCounts.getOrDefault(key, 0);
        entityCounts.put(key, currentAmount + amount);

        Log.debugResult(Debug.ENTITY_SPAWN, "Return", "Success");

        return true;
    }

    @Override
    public boolean untrackEntity(Key key, int amount) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");

        Log.debug(Debug.ENTITY_DESPAWN, island.getOwner().getName(), key, amount);

        if (amount <= 0) {
            Log.debugResult(Debug.ENTITY_DESPAWN, "Return", "Negative Amount");
            return false;
        }

        if (!canTrackEntity(key)) {
            Log.debugResult(Debug.ENTITY_DESPAWN, "Return", "Cannot Untrack Entity");
            return false;
        }

        int currentAmount = entityCounts.getOrDefault(key, -1);

        if (currentAmount != -1) {
            if (currentAmount > amount) {
                entityCounts.put(key, currentAmount - amount);
            } else {
                entityCounts.remove(key);
            }
        }

        Log.debugResult(Debug.ENTITY_DESPAWN, "Return", "Success");

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
        if (beingRecalculated || !canRecalculateEntityCounts())
            return;

        this.beingRecalculated = true;

        try {
            this.lastCalculateTime = System.currentTimeMillis();

            clearEntityCounts();

            KeyMap<Counter> recalculatedEntityCounts = KeyMaps.createConcurrentHashMap(KeyIndicator.ENTITY_TYPE);

            CompletableFutureList<KeyMap<Counter>> chunkEntities = new CompletableFutureList<>(-1);

            IslandUtils.getChunkCoords(island, IslandChunkFlags.ONLY_PROTECTED | IslandChunkFlags.NO_EMPTY_CHUNKS)
                    .forEach(((worldInfo, worldChunks) -> {
                        // Load the world.
                        World world = plugin.getProviders().getWorldsProvider().getIslandsWorld(island, worldInfo.getDimension());
                        if (world != null)
                            chunkEntities.add(plugin.getNMSChunks().calculateChunkEntities(worldChunks));
                    }));

            BukkitExecutor.async(() -> {
                try {
                    chunkEntities.forEachCompleted(entities -> {
                        entities.forEach((entity, count) -> {
                            if (canTrackEntity(entity))
                                recalculatedEntityCounts.computeIfAbsent(entity, i -> new Counter(0)).inc(count.get());
                        });
                    }, error -> {
                        error.printStackTrace();
                        beingRecalculated = false;
                    });

                    if (!beingRecalculated)
                        return;

                    if (!recalculatedEntityCounts.isEmpty()) {
                        recalculatedEntityCounts.forEach((entity, count) ->
                                this.entityCounts.put(entity, count.get()));
                    }
                } finally {
                    IslandsDatabaseBridge.saveEntityCounts(this.island);
                    beingRecalculated = false;
                }
            });
        } catch (Exception error) {
            IslandsDatabaseBridge.saveEntityCounts(this.island);
            beingRecalculated = false;
            throw error;
        }
    }

    @Override
    public boolean canRecalculateEntityCounts() {
        long currentTime = System.currentTimeMillis();
        return currentTime - lastCalculateTime > CALCULATE_DELAY;
    }

    private boolean canTrackEntity(Key key) {
        return island.getEntityLimit(key) != -1 || key.toString().contains("MINECART");
    }

}
