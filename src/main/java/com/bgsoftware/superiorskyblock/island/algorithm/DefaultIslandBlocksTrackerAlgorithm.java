package com.bgsoftware.superiorskyblock.island.algorithm;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandBlocksTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.key.BaseKey;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.map.KeyMaps;
import com.bgsoftware.superiorskyblock.core.key.MaterialKeySource;
import com.bgsoftware.superiorskyblock.core.key.types.MaterialKey;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.values.BlockValue;
import com.bgsoftware.superiorskyblock.island.upgrade.IslandUpgradeConstants;
import com.google.common.base.Preconditions;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;

public class DefaultIslandBlocksTrackerAlgorithm implements IslandBlocksTrackerAlgorithm {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final KeyMap<BigInteger> blockCounts = KeyMaps.createConcurrentHashMap(KeyIndicator.MATERIAL);

    private final Island island;
    private volatile boolean loadingDataMode = false;

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

        if (increaseAmount || hasBlockLimit || valuesMenu) {
            Log.debug(Debug.BLOCK_PLACE, island.getOwner().getName(), key, amount);

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

        if (decreaseAmount || hasBlockLimit || valuesMenu) {
            Log.debug(Debug.BLOCK_BREAK, island.getOwner().getName(), key, amount);

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
        synchronized (this.blockCounts) {
            return blockCounts.getOrDefault(key, BigInteger.ZERO);
        }
    }

    @Override
    public BigInteger getExactBlockCount(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        synchronized (this.blockCounts) {
            return blockCounts.getRaw(key, BigInteger.ZERO);
        }
    }

    @Override
    public Map<Key, BigInteger> getBlockCounts() {
        if (!plugin.getTaskScheduler().isFolia())
            return Collections.unmodifiableMap(this.blockCounts);

        KeyMap<BigInteger> snapshot = KeyMaps.createHashMap(KeyIndicator.MATERIAL);
        synchronized (this.blockCounts) {
            snapshot.putAll(this.blockCounts);
        }
        return Collections.unmodifiableMap(snapshot);
    }

    @Override
    public void clearBlockCounts() {
        synchronized (this.blockCounts) {
            this.blockCounts.clear();
        }
    }

    @Override
    public void setLoadingDataMode(boolean loadingDataMode) {
        this.loadingDataMode = loadingDataMode;
    }

    private void addCounts(Key key, BigInteger amount) {
        Key valueKey = plugin.getBlockValues().getBlockKey(key);

        Log.debug(Debug.BLOCK_COUNT_INCREASE, island.getOwner().getName(), key, amount);

        increaseCount(valueKey, amount);

        if (loadingDataMode)
            return;

        Key limitKey = island.getBlockLimitKey(valueKey);
        Key globalKey = ((BaseKey<?>) valueKey).toGlobalKey();
        boolean limitCount = false;

        if (!limitKey.equals(valueKey)) {
            Log.debugResult(Debug.BLOCK_COUNT_INCREASE, "Limit Key", limitKey);
            increaseCount(limitKey, amount);
            limitCount = true;
        }

        if (!globalKey.equals(valueKey) && (!limitCount || !globalKey.equals(limitKey))) {
            BlockValue blockValue = plugin.getBlockValues().getBlockValue(globalKey);
            if (blockValue != BlockValue.ZERO) {
                Log.debugResult(Debug.BLOCK_COUNT_INCREASE, "Global Key", globalKey);
                increaseCount(globalKey, amount);
            }
        }
    }

    private void increaseCount(Key key, BigInteger amount) {
        synchronized (this.blockCounts) {
            BigInteger currentAmount = blockCounts.getRaw(key, BigInteger.ZERO);
            blockCounts.put(key, currentAmount.add(amount));
        }
    }

    private void removeCounts(Key key, BigInteger amount) {
        Log.debug(Debug.BLOCK_COUNT_DECREASE, island.getOwner().getName(), key, amount);
        synchronized (this.blockCounts) {
            BigInteger currentAmount = blockCounts.getRaw(key, BigInteger.ZERO);
            if (currentAmount.compareTo(amount) <= 0)
                blockCounts.remove(key);
            else
                blockCounts.put(key, currentAmount.subtract(amount));
        }
    }

}
