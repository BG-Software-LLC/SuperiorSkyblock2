package com.bgsoftware.superiorskyblock.values.container;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMapImpl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public abstract class BlockValuesContainer {

    private final KeyMap<BigDecimal> valuesMap = KeyMapImpl.createHashMap();

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

    public Set<Map.Entry<Key, BigDecimal>> getBlockValues() {
        return Collections.unmodifiableSet(valuesMap.entrySet());
    }

    public void clear() {
        valuesMap.clear();
    }

}
