package com.bgsoftware.superiorskyblock.island.algorithm;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChunkFlags;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandEntitiesTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyCategory;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.CalculatedChunk;
import com.bgsoftware.superiorskyblock.core.Counter;
import com.bgsoftware.superiorskyblock.core.collections.CompletableFutureList;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateMap;
import com.bgsoftware.superiorskyblock.core.database.bridge.IslandsDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.category.KeyCategoryId;
import com.bgsoftware.superiorskyblock.core.key.map.KeyMaps;
import com.bgsoftware.superiorskyblock.core.key.types.EntityTypeKey;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.core.threads.Synchronized;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.upgrade.IslandUpgradeConstants;
import com.google.common.base.Preconditions;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DefaultIslandEntitiesTrackerAlgorithm implements IslandEntitiesTrackerAlgorithm {

    private static final Set<EntityType> TRACKABLE_ENTITIES = initializeTrackableEntities();
    private static final long CALCULATE_DELAY = TimeUnit.MINUTES.toMillis(5);

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final KeyMap<Integer> entityCounts = KeyMaps.createConcurrentHashMap(KeyIndicator.ENTITY_TYPE);
    private final Synchronized<EnumerateMap<KeyCategoryId, Integer>> categoryCounts =
            Synchronized.of(new EnumerateMap<>(KeyCategoryId.values()));
    // The mapper the category counts were built with. When it changes (reload), the counts are rebuilt.
    @Nullable
    private volatile SettingsManager.CategoriesMapper countedMapper = null;

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

        this.categoryCounts.write(counts -> {
            this.entityCounts.put(key, this.entityCounts.getOrDefault(key, 0) + amount);

            SettingsManager.CategoriesMapper mapper = plugin.getSettings().getEntityCategoriesMapping();
            if (mapper == this.countedMapper)
                addToCategories(counts, mapper, key, amount);
        });

        Log.debugResult(Debug.ENTITY_SPAWN, "Return", "Success");

        return true;
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

        this.categoryCounts.write(counts -> {
            int currentAmount = this.entityCounts.getOrDefault(key, -1);
            if (currentAmount == -1)
                return;

            int removed = Math.min(currentAmount, amount);

            if (currentAmount > amount)
                this.entityCounts.put(key, currentAmount - amount);
            else
                this.entityCounts.remove(key);

            SettingsManager.CategoriesMapper mapper = plugin.getSettings().getEntityCategoriesMapping();
            if (mapper == this.countedMapper)
                addToCategories(counts, mapper, key, -removed);
        });

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
    public int getEntityCategoryCount(KeyCategory entityCategory) {
        Preconditions.checkNotNull(entityCategory, "entityCategory parameter cannot be null.");

        KeyCategoryId id = KeyCategoryId.of(entityCategory);
        if (id == null)
            return 0;

        // Fast path: counts are up to date, so read under the read lock. On a mismatch (or a race with a
        // mapping change) fall back to the write path, which rebuilds the counts.
        Integer result = this.categoryCounts.readAndGet(counts ->
                plugin.getSettings().getEntityCategoriesMapping() == this.countedMapper ? counts.getOrDefault(id, 0) : null);
        if (result != null)
            return result;

        return this.categoryCounts.writeAndGet(counts -> {
            ensureCategoryCounts(counts);
            return counts.getOrDefault(id, 0);
        });
    }

    @Override
    public Map<KeyCategory, Integer> getEntityCategoryCounts() {
        return this.categoryCounts.writeAndGet(counts -> {
            ensureCategoryCounts(counts);

            if (counts.isEmpty())
                return Collections.<KeyCategory, Integer>emptyMap();

            Map<KeyCategory, Integer> result = new LinkedHashMap<>();
            for (KeyCategory category : plugin.getSettings().getEntityCategoriesMapping().getCategories()) {
                KeyCategoryId id = KeyCategoryId.of(category);
                if (id == null)
                    continue;
                int count = counts.getOrDefault(id, 0);
                if (count > 0)
                    result.put(category, count);
            }

            return Collections.unmodifiableMap(result);
        });
    }

    @Override
    public void clearEntityCounts() {
        this.categoryCounts.write(counts -> {
            this.entityCounts.clear();
            counts.clear();
            // Force a rebuild on next access.
            this.countedMapper = null;
        });
    }

    @Override
    public void recalculateEntityCounts() {
        if (beingRecalculated || !canRecalculateEntityCounts())
            return;

        this.beingRecalculated = true;

        Log.debug(Debug.CHUNK_CALCULATION_ENTITIES, island.getOwner().getName());

        try {
            this.lastCalculateTime = System.currentTimeMillis();

            CompletableFutureList<List<CalculatedChunk.Entities>> chunkEntities = new CompletableFutureList<>(
                    plugin.getSettings().getRecalcTaskTimeout());

            IslandUtils.getChunkCoords(island, IslandChunkFlags.ONLY_PROTECTED | IslandChunkFlags.NO_EMPTY_CHUNKS)
                    .forEach(((worldInfo, worldChunks) -> {
                        // Load the world.
                        World world = plugin.getProviders().getWorldsProvider().getIslandsWorld(island, worldInfo.getDimension());
                        if (world != null)
                            chunkEntities.add(plugin.getNMSChunks().calculateChunkEntities(worldChunks));
                    }));

            BukkitExecutor.async(() -> {
                try {
                    KeyMap<Counter> recalculatedEntityCounts = KeyMaps.createConcurrentHashMap(KeyIndicator.ENTITY_TYPE);

                    boolean completed = chunkEntities.forEachCompleted(worldCalculatedChunk -> worldCalculatedChunk.forEach(calculatedChunk -> {
                        Log.debugResult(Debug.CHUNK_CALCULATION_ENTITIES, "Chunk Finished", calculatedChunk.getPosition());
                        calculatedChunk.getEntityCounts().forEach((entity, count) -> {
                            if (canTrackEntity(entity))
                                recalculatedEntityCounts.computeIfAbsent(entity, i -> new Counter(0)).inc(count.get());
                        });
                    }), error -> {
                        error.printStackTrace();
                        beingRecalculated = false;
                    });

                    // The current counts are kept when not all chunks were counted in time.
                    if (!completed || !beingRecalculated)
                        return;

                    clearEntityCounts();

                    if (!recalculatedEntityCounts.isEmpty()) {
                        this.categoryCounts.write(counts -> {
                            recalculatedEntityCounts.forEach((entity, count) -> {
                                Log.debug(Debug.ENTITY_SPAWN, island.getOwner().getName(), entity, count.get());
                                this.entityCounts.put(entity, count.get());
                            });
                            // Rebuilt from the exact counts on next access.
                            counts.clear();
                            this.countedMapper = null;
                        });
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
        if (island.getEntityLimit(key) != IslandUpgradeConstants.NO_LIMIT_VALUE) {
            return true;
        }

        if (hasEntityCategoryLimit(key))
            return true;

        if (key instanceof EntityTypeKey) {
            return TRACKABLE_ENTITIES.contains(((EntityTypeKey) key).getEntityType());
        } else {
            return key.toString().contains("MINECART");
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

    private boolean hasEntityCategoryLimit(Key key) {
        for (KeyCategory category : plugin.getSettings().getEntityCategoriesMapping().getCategories(key)) {
            if (island.getEntityCategoryLimit(category) != IslandUpgradeConstants.NO_LIMIT_VALUE)
                return true;
        }

        return false;
    }

    private static void addToCategories(EnumerateMap<KeyCategoryId, Integer> counts,
                                        SettingsManager.CategoriesMapper mapper, Key key, int delta) {
        for (KeyCategory category : mapper.getCategories(key)) {
            KeyCategoryId id = KeyCategoryId.of(category);
            if (id == null)
                continue;

            int value = counts.getOrDefault(id, 0) + delta;
            if (value <= 0) {
                // EnumerateMap#remove corrupts the size when the slot is empty.
                if (counts.containsKey(id))
                    counts.remove(id);
            } else {
                counts.put(id, value);
            }
        }
    }

    // Call only inside categoryCounts.write / writeAndGet.
    private void ensureCategoryCounts(EnumerateMap<KeyCategoryId, Integer> counts) {
        SettingsManager.CategoriesMapper mapper = plugin.getSettings().getEntityCategoriesMapping();
        if (mapper == this.countedMapper)
            return;

        counts.clear();
        this.entityCounts.forEach((key, count) -> addToCategories(counts, mapper, key, count));
        this.countedMapper = mapper;
    }

}
