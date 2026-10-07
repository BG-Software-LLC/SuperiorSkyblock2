package com.bgsoftware.superiorskyblock.island.algorithm;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChunkFlags;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandEntitiesTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.CalculatedChunk;
import com.bgsoftware.superiorskyblock.core.collections.CompletableFutureList;
import com.bgsoftware.superiorskyblock.core.database.bridge.IslandsDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.map.KeyMaps;
import com.bgsoftware.superiorskyblock.core.key.types.EntityTypeKey;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.upgrade.IslandUpgradeConstants;
import com.google.common.base.Preconditions;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DefaultIslandEntitiesTrackerAlgorithm implements IslandEntitiesTrackerAlgorithm {

    private static final Set<EntityType> TRACKABLE_ENTITIES = initializeTrackableEntities();
    private static final long CALCULATE_DELAY = TimeUnit.MINUTES.toMillis(5);
    private static final Map<UUID, EntityReservation> ENTITY_RESERVATIONS = new ConcurrentHashMap<>();

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private KeyMap<Integer> entityCounts = KeyMaps.createConcurrentHashMap(KeyIndicator.ENTITY_TYPE);
    private final Object entityCountsLock = new Object();
    private final KeyMap<Integer> pendingEntityCounts = KeyMaps.createHashMap(KeyIndicator.ENTITY_TYPE);

    private final Island island;

    private volatile boolean beingRecalculated = false;
    private volatile long lastCalculateTime = 0L;

    public DefaultIslandEntitiesTrackerAlgorithm(Island island) {
        this.island = island;
    }

    @Override
    public boolean trackEntity(Key key, int amount) {
        return trackEntity(null, key, amount);
    }

    public boolean trackEntity(UUID entityId, Key key, int amount) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");

        Log.debug(Debug.ENTITY_SPAWN, island.getOwner().getName(), key, amount);

        if (beingRecalculated) {
            Log.debugResult(Debug.ENTITY_SPAWN, "Return", "Recalculating");
            return false;
        }

        if (amount <= 0) {
            Log.debugResult(Debug.ENTITY_SPAWN, "Return", "Negative Amount");
            return false;
        }

        if (!canTrackEntity(key)) {
            Log.debugResult(Debug.ENTITY_SPAWN, "Return", "Cannot Track Entity");
            return false;
        }

        synchronized (this.entityCountsLock) {
            if (this.beingRecalculated)
                return false;
            EntityReservation reservation = entityId == null ? null : ENTITY_RESERVATIONS.get(entityId);
            if (reservation != null && reservation.tracker == this && reservation.committed)
                return false;
            int currentAmount = this.entityCounts.getOrDefault(key, 0);
            this.entityCounts.put(key, currentAmount + amount);
            if (reservation != null && reservation.tracker == this) {
                removePendingReservation(reservation);
                reservation.committed = true;
            }
        }

        Log.debugResult(Debug.ENTITY_SPAWN, "Return", "Success");

        return true;
    }

    public boolean reserveEntity(UUID entityId, Key key, int amount, int limit) {
        Preconditions.checkNotNull(entityId, "entityId parameter cannot be null.");
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkArgument(amount > 0, "amount parameter must be positive.");
        if (limit < 0)
            return true;
        synchronized (this.entityCountsLock) {
            EntityReservation previous = ENTITY_RESERVATIONS.get(entityId);
            if (previous != null)
                return previous.tracker == this;
            if (this.beingRecalculated)
                return false;
            int pendingAmount = this.pendingEntityCounts.getOrDefault(key, 0);
            if ((long) this.entityCounts.getOrDefault(key, 0) + pendingAmount + amount > limit)
                return false;
            EntityReservation reservation = new EntityReservation(this, key, amount);
            if (ENTITY_RESERVATIONS.putIfAbsent(entityId, reservation) != null)
                return false;
            this.pendingEntityCounts.put(key, pendingAmount + amount);
            return true;
        }
    }

    public static void releaseEntityReservation(UUID entityId) {
        EntityReservation reservation = ENTITY_RESERVATIONS.get(entityId);
        if (reservation == null)
            return;
        synchronized (reservation.tracker.entityCountsLock) {
            if (ENTITY_RESERVATIONS.remove(entityId, reservation) && !reservation.committed)
                reservation.tracker.removePendingReservation(reservation);
        }
    }

    private void removePendingReservation(EntityReservation reservation) {
        int remaining = this.pendingEntityCounts.getOrDefault(reservation.key, 0) - reservation.amount;
        if (remaining > 0)
            this.pendingEntityCounts.put(reservation.key, remaining);
        else
            this.pendingEntityCounts.remove(reservation.key);
    }

    @Override
    public boolean untrackEntity(Key key, int amount) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");

        Log.debug(Debug.ENTITY_DESPAWN, island.getOwner().getName(), key, amount);

        if (beingRecalculated) {
            Log.debugResult(Debug.ENTITY_DESPAWN, "Return", "Recalculating");
            return false;
        }

        if (amount <= 0) {
            Log.debugResult(Debug.ENTITY_DESPAWN, "Return", "Negative Amount");
            return false;
        }

        if (!canTrackEntity(key)) {
            Log.debugResult(Debug.ENTITY_DESPAWN, "Return", "Cannot Untrack Entity");
            return false;
        }

        synchronized (this.entityCountsLock) {
            if (this.beingRecalculated)
                return false;
            int currentAmount = this.entityCounts.getOrDefault(key, -1);
            if (currentAmount != -1) {
                if (currentAmount > amount) {
                    this.entityCounts.put(key, currentAmount - amount);
                } else {
                    this.entityCounts.remove(key);
                }
            }
        }

        Log.debugResult(Debug.ENTITY_DESPAWN, "Return", "Success");

        return true;
    }

    @Override
    public int getEntityCount(Key key) {
        synchronized (this.entityCountsLock) {
            return this.entityCounts.getOrDefault(key, 0);
        }
    }

    @Override
    public Map<Key, Integer> getEntitiesCounts() {
        KeyMap<Integer> snapshot = KeyMaps.createHashMap(KeyIndicator.ENTITY_TYPE);
        synchronized (this.entityCountsLock) {
            snapshot.putAll(this.entityCounts);
        }
        return Collections.unmodifiableMap(snapshot);
    }

    @Override
    public void clearEntityCounts() {
        synchronized (this.entityCountsLock) {
            this.entityCounts.clear();
        }
    }

    @Override
    public void recalculateEntityCounts() {
        synchronized (this.entityCountsLock) {
            long currentTime = System.currentTimeMillis();
            if (this.beingRecalculated || !this.pendingEntityCounts.isEmpty() ||
                    currentTime - this.lastCalculateTime <= CALCULATE_DELAY)
                return;
            this.beingRecalculated = true;
            this.lastCalculateTime = currentTime;
        }

        try {
            Log.debug(Debug.CHUNK_CALCULATION_ENTITIES, island.getOwner().getName());
            CompletableFutureList<List<CalculatedChunk.Entities>> chunkEntities = new CompletableFutureList<>(-1);

            IslandUtils.getChunkCoords(island, IslandChunkFlags.ONLY_PROTECTED | IslandChunkFlags.NO_EMPTY_CHUNKS)
                    .forEach(((worldInfo, worldChunks) -> {
                        // Load the world.
                        World world = plugin.getProviders().getWorldsProvider().getIslandsWorld(island, worldInfo.getDimension());
                        if (world != null)
                            chunkEntities.add(plugin.getNMSChunks().calculateChunkEntities(worldChunks));
                    }));

            BukkitExecutor.async(() -> {
                try {
                    KeyMap<Integer> recalculatedEntityCounts = KeyMaps.createHashMap(KeyIndicator.ENTITY_TYPE);
                    AtomicBoolean failed = new AtomicBoolean();

                    chunkEntities.forEachCompleted(worldCalculatedChunk -> worldCalculatedChunk.forEach(calculatedChunk -> {
                        Log.debugResult(Debug.CHUNK_CALCULATION_ENTITIES, "Chunk Finished", calculatedChunk.getPosition());
                        calculatedChunk.getEntityCounts().forEach((entity, count) -> {
                            if (canTrackEntity(entity))
                                recalculatedEntityCounts.put(entity, recalculatedEntityCounts.getRaw(entity, 0) + count.get());
                        });
                    }), error -> {
                        error.printStackTrace();
                        failed.set(true);
                    });

                    if (failed.get())
                        return;

                    recalculatedEntityCounts.forEach((entity, count) ->
                            Log.debug(Debug.ENTITY_SPAWN, island.getOwner().getName(), entity, count));
                    synchronized (this.entityCountsLock) {
                        this.entityCounts = recalculatedEntityCounts;
                    }
                } finally {
                    this.beingRecalculated = false;
                    IslandsDatabaseBridge.saveEntityCounts(this.island);
                }
            });
        } catch (Exception error) {
            this.beingRecalculated = false;
            IslandsDatabaseBridge.saveEntityCounts(this.island);
            throw error;
        }
    }

    @Override
    public boolean canRecalculateEntityCounts() {
        long currentTime = System.currentTimeMillis();
        synchronized (this.entityCountsLock) {
            return !this.beingRecalculated && this.pendingEntityCounts.isEmpty() &&
                    currentTime - this.lastCalculateTime > CALCULATE_DELAY;
        }
    }

    private boolean canTrackEntity(Key key) {
        if (island.getEntityLimit(key) != IslandUpgradeConstants.NO_LIMIT_VALUE)
            return true;

        if (key instanceof EntityTypeKey) {
            return TRACKABLE_ENTITIES.contains(((EntityTypeKey) key).getEntityType());
        } else {
            return key.toString().contains("MINECART");
        }
    }

    private static class EntityReservation {

        private final DefaultIslandEntitiesTrackerAlgorithm tracker;
        private final Key key;
        private final int amount;
        private boolean committed;

        private EntityReservation(DefaultIslandEntitiesTrackerAlgorithm tracker, Key key, int amount) {
            this.tracker = tracker;
            this.key = key;
            this.amount = amount;
        }
    }

    private static Set<EntityType> initializeTrackableEntities() {
        EnumSet<EntityType> trackableEntities = EnumSet.noneOf(EntityType.class);

        for (EntityType entityType : EntityType.values()) {
            if (entityType.name().contains("MINECART")) {
                trackableEntities.add(entityType);
            }
        }

        return trackableEntities.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(trackableEntities);
    }

}
