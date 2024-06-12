package com.bgsoftware.superiorskyblock.core.values.container;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.KeyMaps;

import java.math.BigDecimal;
import java.util.function.BiConsumer;

public abstract class BlockValuesContainer {

    private final KeyMap<BigDecimal> valuesMap = KeyMaps.createHashMap(KeyIndicator.MATERIAL);

    public abstract void loadDefaultValues(SuperiorSkyblockPlugin plugin);

    public void setBlockValue(Key key, BigDecimal value) {
        valuesMap.put(getBlockValueKey(key), value);
    }

    public BigDecimal getBlockValue(Key key) {
        return valuesMap.get(key);
    }

    public boolean hasBlockValue(Key key) {
        return valuesMap.containsKey(key);
    }

    public boolean containsKeyRaw(Key key) {
        return valuesMap.getKey(key) == key;
    }

    public boolean containsKey(Key key) {
        return valuesMap.containsKey(key);
    }

    public Key getBlockValueKey(Key key) {
        return valuesMap.getKey(key, key);
    }

    public void forEach(BiConsumer<Key, BigDecimal> consumer) {
        valuesMap.forEach(consumer);
    }

    public void clear() {
        valuesMap.clear();
    }

}
