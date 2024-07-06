package com.bgsoftware.superiorskyblock.core.values.container;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.KeyMaps;
import com.bgsoftware.superiorskyblock.core.values.BlockValue;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

public class BlockValuesContainer {

    private final KeyMap<BlockValue> valuesMap = KeyMaps.createHashMap(KeyIndicator.MATERIAL);

    public void setBlockValue(Key key, BlockValue value) {
        valuesMap.put(getBlockValueKey(key), value);
    }

    @Nullable
    public BlockValue getBlockValue(Key key) {
        return valuesMap.get(key);
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

    public void forEach(BiConsumer<Key, BlockValue> consumer) {
        valuesMap.forEach(consumer);
    }

    public void clear() {
        valuesMap.clear();
    }

}
