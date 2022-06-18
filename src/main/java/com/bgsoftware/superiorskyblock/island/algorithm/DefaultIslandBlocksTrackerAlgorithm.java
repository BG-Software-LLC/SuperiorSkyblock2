package com.bgsoftware.superiorskyblock.island.algorithm;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandBlocksTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import com.bgsoftware.superiorskyblock.core.key.KeyMapImpl;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;

public class DefaultIslandBlocksTrackerAlgorithm implements IslandBlocksTrackerAlgorithm {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final KeyMap<BigInteger> blockCounts = KeyMapImpl.createConcurrentHashMap();

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

        BigDecimal blockValue = plugin.getBlockValues().getBlockWorth(key);
        BigDecimal blockLevel = plugin.getBlockValues().getBlockLevel(key);

        boolean increaseAmount = false;

        if (blockValue.compareTo(BigDecimal.ZERO) != 0) {
            increaseAmount = true;
        } else if (blockLevel.compareTo(BigDecimal.ZERO) != 0) {
            increaseAmount = true;
        }

        boolean hasBlockLimit = island.getBlockLimit(key) != -1;
        boolean valuesMenu = plugin.getBlockValues().isValuesMenu(key);

        if (increaseAmount || hasBlockLimit || valuesMenu) {
            PluginDebugger.debug("Action: Block Place, Island: " + island.getOwner().getName() +
                    ", Block: " + key + ", Amount: " + amount);

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

        BigDecimal blockValue = plugin.getBlockValues().getBlockWorth(key);
        BigDecimal blockLevel = plugin.getBlockValues().getBlockLevel(key);

        boolean decreaseAmount = false;

        if (blockValue.compareTo(BigDecimal.ZERO) != 0) {
            decreaseAmount = true;
        }

        if (blockLevel.compareTo(BigDecimal.ZERO) != 0) {
            decreaseAmount = true;
        }

        boolean hasBlockLimit = island.getBlockLimit(key) != -1;
        boolean valuesMenu = plugin.getBlockValues().isValuesMenu(key);

        if (decreaseAmount || hasBlockLimit || valuesMenu) {
            PluginDebugger.debug("Action: Block Break, Island: " + island.getOwner().getName() + ", Block: " + key);

            Key valueKey = plugin.getBlockValues().getBlockKey(key);
            removeCounts(valueKey, amount);

            Key limitKey = island.getBlockLimitKey(valueKey);
            Key globalKey = KeyImpl.of(valueKey.getGlobalKey());
            boolean limitCount = false;

            if (!limitKey.equals(valueKey)) {
                removeCounts(limitKey, amount);
                limitCount = true;
            }

            if (!globalKey.equals(valueKey) && (!limitCount || !globalKey.equals(limitKey)) &&
                    (plugin.getBlockValues().getBlockWorth(globalKey).doubleValue() != 0 ||
                            plugin.getBlockValues().getBlockLevel(globalKey).doubleValue() != 0)) {
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
    public void clearBlockCounts() {
        this.blockCounts.clear();
    }

    @Override
    public void setLoadingDataMode(boolean loadingDataMode) {
        this.loadingDataMode = loadingDataMode;
    }

    private void addCounts(Key key, BigInteger amount) {
        Key valueKey = plugin.getBlockValues().getBlockKey(key);

        PluginDebugger.debug("Action: Count Increase, Block: " + valueKey + ", Amount: " + amount);

        BigInteger currentAmount = blockCounts.getRaw(valueKey, BigInteger.ZERO);
        blockCounts.put(valueKey, currentAmount.add(amount));

        if (loadingDataMode)
            return;

        Key limitKey = island.getBlockLimitKey(valueKey);
        Key globalKey = KeyImpl.of(valueKey.getGlobalKey());
        boolean limitCount = false;

        if (!limitKey.equals(valueKey)) {
            PluginDebugger.debug("Action: Count Increase, Block: " + limitKey + ", Amount: " + amount + " - Limit Key");
            currentAmount = blockCounts.getRaw(limitKey, BigInteger.ZERO);
            blockCounts.put(limitKey, currentAmount.add(amount));
            limitCount = true;
        }

        if (!globalKey.equals(valueKey) && (!limitCount || !globalKey.equals(limitKey)) &&
                (plugin.getBlockValues().getBlockWorth(globalKey).doubleValue() != 0 ||
                        plugin.getBlockValues().getBlockLevel(globalKey).doubleValue() != 0)) {
            PluginDebugger.debug("Action: Count Increase, Block: " + globalKey + ", Amount: " + amount + " - Global Key");
            currentAmount = blockCounts.getRaw(globalKey, BigInteger.ZERO);
            blockCounts.put(globalKey, currentAmount.add(amount));
        }
    }

    private void removeCounts(Key key, BigInteger amount) {
        PluginDebugger.debug("Action: Count Decrease, Block: " + key + ", Amount: " + amount);
        BigInteger currentAmount = blockCounts.getRaw(key, BigInteger.ZERO);
        if (currentAmount.compareTo(amount) <= 0)
            blockCounts.remove(key);
        else
            blockCounts.put(key, currentAmount.subtract(amount));
    }

}
