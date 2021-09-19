package com.bgsoftware.superiorskyblock.values.container;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMap;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public abstract class BlockValuesContainer {

    private final KeyMap<BigDecimal> valuesMap = new KeyMap<>();

    public abstract void loadDefaultValues(SuperiorSkyblockPlugin plugin);

    public void setBlockValue(com.bgsoftware.superiorskyblock.api.key.Key key, BigDecimal value) {
        setBlockValue(Key.of(key.toString()), value);
    }

    public void setBlockValue(Key key, BigDecimal value) {
        valuesMap.put(getBlockValueKey(key), value);
    }

    public BigDecimal getBlockValue(com.bgsoftware.superiorskyblock.api.key.Key key) {
        return valuesMap.get(key);
    }

    public boolean hasBlockValue(com.bgsoftware.superiorskyblock.api.key.Key key) {
        return valuesMap.containsKey(key);
    }

    public boolean containsKeyRaw(Key key) {
        return valuesMap.getKey(key) == key;
    }

    public boolean containsKey(com.bgsoftware.superiorskyblock.api.key.Key key) {
        return valuesMap.containsKey(key);
    }

    public Key getBlockValueKey(Key key) {
        return valuesMap.getKey(key);
    }

    public Set<Map.Entry<com.bgsoftware.superiorskyblock.api.key.Key, BigDecimal>> getBlockValues() {
        return Collections.unmodifiableSet(valuesMap.entrySet());
    }

}
