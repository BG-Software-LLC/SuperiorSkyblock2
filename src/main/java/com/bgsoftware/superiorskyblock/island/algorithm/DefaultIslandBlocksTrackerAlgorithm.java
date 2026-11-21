package com.bgsoftware.superiorskyblock.island.algorithm;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandBlocksTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyCategory;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateMap;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.key.BaseKey;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.category.KeyCategoryId;
import com.bgsoftware.superiorskyblock.core.key.map.KeyMaps;
import com.bgsoftware.superiorskyblock.core.key.MaterialKeySource;
import com.bgsoftware.superiorskyblock.core.key.types.MaterialKey;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.threads.Synchronized;
import com.bgsoftware.superiorskyblock.core.values.BlockValue;
import com.bgsoftware.superiorskyblock.island.upgrade.IslandUpgradeConstants;
import com.google.common.base.Preconditions;

import com.bgsoftware.common.annotations.Nullable;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultIslandBlocksTrackerAlgorithm implements IslandBlocksTrackerAlgorithm {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final KeyMap<BigInteger> blockCounts = KeyMaps.createConcurrentHashMap(KeyIndicator.MATERIAL);
    private final KeyMap<BigInteger> individualCounts = KeyMaps.createConcurrentHashMap(KeyIndicator.MATERIAL);
    private final Synchronized<EnumerateMap<KeyCategoryId, BigInteger>> categoryCounts =
            Synchronized.of(new EnumerateMap<>(KeyCategoryId.values()));
    // The mapper the category counts were built with. When it changes (reload), the counts are rebuilt.
    @Nullable
    private volatile SettingsManager.CategoriesMapper countedMapper = null;

    private final Island island;
    private boolean loadingDataMode = false;

    public DefaultIslandBlocksTrackerAlgorithm(Island island) {
        this.island = island;
    }

    @Override
    public boolean trackBlock(Key key, BigInteger amount) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkNotNull(amount, "amount parameter cannot be null.");

        if (amount.compareTo(BigInteger.ZERO) == 0)
            return false;

        if (!ServerVersion.isLegacy() && key instanceof MaterialKey &&
                ((MaterialKey) key).getMaterialKeySource() == MaterialKeySource.ITEM)
            key = ((MaterialKey) key).toGlobalKey();

        BlockValue blockValue = plugin.getBlockValues().getBlockValue(key);
        boolean increaseAmount = blockValue != BlockValue.ZERO;

        boolean hasBlockLimit = island.getBlockLimit(key) != IslandUpgradeConstants.NO_LIMIT_VALUE;
        boolean valuesMenu = plugin.getBlockValues().isValuesMenu(key);

        if (increaseAmount || hasBlockLimit || valuesMenu || hasBlockCategoryLimit(key)) {
            Log.debug(Debug.BLOCK_PLACE, island.getOwner().getName(), key, amount);

            addIndividualCounts(key, amount);
            addCounts(key, amount);

            return true;
        }

        return false;
    }

    @Override
    public boolean untrackBlock(Key key, BigInteger amount) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkNotNull(amount, "amount parameter cannot be null.");

        if (amount.compareTo(BigInteger.ZERO) == 0)
            return false;

        if (!ServerVersion.isLegacy() && key instanceof MaterialKey &&
                ((MaterialKey) key).getMaterialKeySource() == MaterialKeySource.ITEM)
            key = ((MaterialKey) key).toGlobalKey();

        BlockValue blockValue = plugin.getBlockValues().getBlockValue(key);

        boolean decreaseAmount = blockValue != BlockValue.ZERO;

        boolean hasBlockLimit = island.getBlockLimit(key) != IslandUpgradeConstants.NO_LIMIT_VALUE;
        boolean valuesMenu = plugin.getBlockValues().isValuesMenu(key);

        if (decreaseAmount || hasBlockLimit || valuesMenu || hasBlockCategoryLimit(key)) {
            Log.debug(Debug.BLOCK_BREAK, island.getOwner().getName(), key, amount);

            removeIndividualCounts(key, amount);

            Key valueKey = plugin.getBlockValues().getBlockKey(key);
            removeCounts(valueKey, amount);

            if (loadingDataMode)
                return true;

            Key limitKey = island.getBlockLimitKey(valueKey);
            Key globalKey = ((BaseKey<?>) valueKey).toGlobalKey();
            boolean limitCount = false;

            if (!limitKey.equals(valueKey)) {
                removeCounts(limitKey, amount);
                limitCount = true;
            }

            if (!globalKey.equals(valueKey) && (!limitCount || !globalKey.equals(limitKey))) {
                blockValue = plugin.getBlockValues().getBlockValue(globalKey);
                if (blockValue != BlockValue.ZERO)
                    removeCounts(globalKey, amount);
            }

            return true;
        }

        return false;
    }

    @Override
    public BigInteger getBlockCount(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        return blockCounts.getOrDefault(key, BigInteger.ZERO);
    }

    @Override
    public BigInteger getExactBlockCount(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        return blockCounts.getRaw(key, BigInteger.ZERO);
    }

    @Override
    public Map<Key, BigInteger> getBlockCounts() {
        return Collections.unmodifiableMap(this.blockCounts);
    }

    @Override
    public Map<Key, BigInteger> getIndividualBlockCounts() {
        return Collections.unmodifiableMap(this.individualCounts);
    }

    @Override
    public BigInteger getBlockCategoryCount(KeyCategory blockCategory) {
        Preconditions.checkNotNull(blockCategory, "blockCategory parameter cannot be null.");

        KeyCategoryId id = KeyCategoryId.of(blockCategory);
        if (id == null)
            return BigInteger.ZERO;

        // Fast path: counts are up to date, so read under the read lock. On a mismatch (or a race with a
        // mapping change) fall back to the write path, which rebuilds the counts.
        BigInteger result = this.categoryCounts.readAndGet(counts ->
                plugin.getSettings().getBlockCategoriesMapping() == this.countedMapper ? counts.getOrDefault(id, BigInteger.ZERO) : null);
        if (result != null)
            return result;

        return this.categoryCounts.writeAndGet(counts -> {
            ensureCategoryCounts(counts);
            return counts.getOrDefault(id, BigInteger.ZERO);
        });
    }

    @Override
    public Map<KeyCategory, BigInteger> getBlockCategoryCounts() {
        return this.categoryCounts.writeAndGet(counts -> {
            ensureCategoryCounts(counts);

            if (counts.isEmpty())
                return Collections.<KeyCategory, BigInteger>emptyMap();

            Map<KeyCategory, BigInteger> result = new LinkedHashMap<>();
            for (KeyCategory category : plugin.getSettings().getBlockCategoriesMapping().getCategories()) {
                KeyCategoryId id = KeyCategoryId.of(category);
                if (id == null)
                    continue;
                BigInteger count = counts.getOrDefault(id, BigInteger.ZERO);
                if (count.signum() > 0)
                    result.put(category, count);
            }

            return Collections.unmodifiableMap(result);
        });
    }

    @Override
    public void clearBlockCounts() {
        this.blockCounts.clear();
        this.categoryCounts.write(counts -> {
            this.individualCounts.clear();
            counts.clear();
        });
    }

    @Override
    public void setLoadingDataMode(boolean loadingDataMode) {
        this.loadingDataMode = loadingDataMode;
    }

    private void addCounts(Key key, BigInteger amount) {
        Key valueKey = plugin.getBlockValues().getBlockKey(key);

        Log.debug(Debug.BLOCK_COUNT_INCREASE, island.getOwner().getName(), key, amount);

        BigInteger currentAmount = blockCounts.getRaw(valueKey, BigInteger.ZERO);
        blockCounts.put(valueKey, currentAmount.add(amount));

        if (loadingDataMode)
            return;

        Key limitKey = island.getBlockLimitKey(valueKey);
        Key globalKey = ((BaseKey<?>) valueKey).toGlobalKey();
        boolean limitCount = false;

        if (!limitKey.equals(valueKey)) {
            Log.debugResult(Debug.BLOCK_COUNT_INCREASE, "Limit Key", limitKey);
            currentAmount = blockCounts.getRaw(limitKey, BigInteger.ZERO);
            blockCounts.put(limitKey, currentAmount.add(amount));
            limitCount = true;
        }

        if (!globalKey.equals(valueKey) && (!limitCount || !globalKey.equals(limitKey))) {
            BlockValue blockValue = plugin.getBlockValues().getBlockValue(globalKey);
            if (blockValue != BlockValue.ZERO) {
                Log.debugResult(Debug.BLOCK_COUNT_INCREASE, "Global Key", globalKey);
                currentAmount = blockCounts.getRaw(globalKey, BigInteger.ZERO);
                blockCounts.put(globalKey, currentAmount.add(amount));
            }
        }
    }

    private void removeCounts(Key key, BigInteger amount) {
        Log.debug(Debug.BLOCK_COUNT_DECREASE, island.getOwner().getName(), key, amount);
        BigInteger currentAmount = blockCounts.getRaw(key, BigInteger.ZERO);
        if (currentAmount.compareTo(amount) <= 0)
            blockCounts.remove(key);
        else
            blockCounts.put(key, currentAmount.subtract(amount));
    }

    private boolean hasBlockCategoryLimit(Key key) {
        for (KeyCategory category : plugin.getSettings().getBlockCategoriesMapping().getCategories(key)) {
            if (island.getBlockCategoryLimit(category) != IslandUpgradeConstants.NO_LIMIT_VALUE)
                return true;
        }

        return false;
    }

    private void addIndividualCounts(Key key, BigInteger amount) {
        this.categoryCounts.write(counts -> {
            this.individualCounts.put(key, this.individualCounts.getRaw(key, BigInteger.ZERO).add(amount));
            SettingsManager.CategoriesMapper mapper = plugin.getSettings().getBlockCategoriesMapping();
            if (mapper == this.countedMapper)
                addToCategories(counts, mapper, key, amount);
        });
    }

    private void removeIndividualCounts(Key key, BigInteger amount) {
        this.categoryCounts.write(counts -> {
            BigInteger current = this.individualCounts.getRaw(key, BigInteger.ZERO);
            if (current.signum() <= 0)
                return;

            BigInteger removed = current.min(amount);

            if (current.compareTo(amount) <= 0)
                this.individualCounts.remove(key);
            else
                this.individualCounts.put(key, current.subtract(amount));

            SettingsManager.CategoriesMapper mapper = plugin.getSettings().getBlockCategoriesMapping();
            if (mapper == this.countedMapper)
                addToCategories(counts, mapper, key, removed.negate());
        });
    }

    private static void addToCategories(EnumerateMap<KeyCategoryId, BigInteger> counts,
                                        SettingsManager.CategoriesMapper mapper, Key key, BigInteger delta) {
        for (KeyCategory category : mapper.getCategories(key)) {
            KeyCategoryId id = KeyCategoryId.of(category);
            if (id == null)
                continue;

            BigInteger value = counts.getOrDefault(id, BigInteger.ZERO).add(delta);
            if (value.signum() <= 0) {
                // EnumerateMap#remove corrupts the size when the slot is empty.
                if (counts.containsKey(id))
                    counts.remove(id);
            } else {
                counts.put(id, value);
            }
        }
    }

    // Call only inside categoryCounts.write / writeAndGet.
    private void ensureCategoryCounts(EnumerateMap<KeyCategoryId, BigInteger> counts) {
        SettingsManager.CategoriesMapper mapper = plugin.getSettings().getBlockCategoriesMapping();
        if (mapper == this.countedMapper)
            return;

        counts.clear();
        this.individualCounts.forEach((key, count) -> addToCategories(counts, mapper, key, count));
        this.countedMapper = mapper;
    }

}
