package com.bgsoftware.superiorskyblock.core.key.collections;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.key.BaseKey;
import com.bgsoftware.superiorskyblock.core.key.types.CustomKey;
import com.bgsoftware.superiorskyblock.core.key.types.LazyKey;
import com.bgsoftware.superiorskyblock.core.key.types.MaterialKey;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class MaterialKeyMap<V> extends AbstractMap<Key, V> implements KeyMap<V> {

    private final LazyReference<Map<MaterialKey, V>> innerMap;
    private final LazyReference<Map<CustomKey, V>> customInnerMap;

    public static <V> KeyMap<V> createMap(Supplier<Map> mapCreator) {
        return new MaterialKeyMap<>(mapCreator);
    }

    private MaterialKeyMap(Supplier<Map> mapCreator) {
        this.innerMap = new LazyReference<Map<MaterialKey, V>>() {
            @Override
            protected Map<MaterialKey, V> create() {
                return mapCreator.get();
            }
        };
        this.customInnerMap = new LazyReference<Map<CustomKey, V>>() {
            @Override
            protected Map<CustomKey, V> create() {
                return mapCreator.get();
            }
        };
    }

    @Override
    public int size() {
        int innerMapSize = this.innerMap.getIfPresent().map(Map::size).orElse(0);
        int customInnerMapSize = this.customInnerMap.getIfPresent().map(Map::size).orElse(0);
        return innerMapSize + customInnerMapSize;
    }

    @Override
    public boolean containsKey(Object o) {
        return this.get(o) != null;
    }

    @Override
    public V get(Object obj) {
        if (size() == 0)
            return null;

        if (obj instanceof LazyKey) {
            return get(((LazyKey<?>) obj).getBaseKey());
        }

        return getInternal((Key) obj, true);
    }

    private V getInternal(Key key, boolean tryGlobal) {
        Preconditions.checkArgument(!(key instanceof LazyKey), "Cannot call getInternal on LazyKey directly.");

        V value;

        if (key instanceof MaterialKey) {
            value = this.innerMap.getIfPresent().map(m -> m.get(key)).orElse(null);
        } else if (key instanceof CustomKey) {
            value = this.customInnerMap.getIfPresent().map(m -> m.get(key)).orElse(null);
        } else {
            value = null;
        }

        if (!tryGlobal || value != null)
            return value;

        Key globalKey = ((BaseKey<?>) key).toGlobalKey();

        return globalKey == key ? null : getInternal(globalKey, false);
    }

    @Override
    public V put(Key key, V value) {
        if (key instanceof LazyKey) {
            return put(((LazyKey<?>) key).getBaseKey(), value);
        }

        if (key instanceof MaterialKey) {
            return this.innerMap.get().put((MaterialKey) key, value);
        }

        if (key instanceof CustomKey) {
            return this.customInnerMap.get().put((CustomKey) key, value);
        }

        throw new IllegalArgumentException("obj is not MaterialKey");
    }

    @Override
    public V remove(Object key) {
        if (size() == 0)
            return null;

        if (key instanceof LazyKey) {
            return remove(((LazyKey<?>) key).getBaseKey());
        }

        if (key instanceof MaterialKey) {
            return this.innerMap.getIfPresent().map(m -> m.remove(key)).orElse(null);
        }

        if (key instanceof CustomKey) {
            return this.customInnerMap.getIfPresent().map(m -> m.remove(key)).orElse(null);
        }

        return null;
    }

    @Override
    public void clear() {
        this.innerMap.getIfPresent().ifPresent(Map::clear);
        this.customInnerMap.getIfPresent().ifPresent(Map::clear);
    }

    @NotNull
    @Override
    public Set<Entry<Key, V>> entrySet() {
        if (size() == 0)
            return Collections.emptySet();

        Set innerMapEntrySet = this.innerMap.getIfPresent().map(Map::entrySet).orElse(null);
        Set customInnerMapEntrySet = this.customInnerMap.getIfPresent().map(Map::entrySet).orElse(null);

        if (innerMapEntrySet == null && customInnerMapEntrySet == null)
            return Collections.emptySet();
        if (innerMapEntrySet == null)
            return customInnerMapEntrySet;
        if (customInnerMapEntrySet == null)
            return innerMapEntrySet;

        return Sets.union(innerMapEntrySet, customInnerMapEntrySet);
    }

    @Override
    public String toString() {
        return "MaterialKeyMap" + super.toString();
    }

    @Nullable
    @Override
    public Key getKey(Key original) {
        return this.getKey(original, null);
    }

    @Override
    public Key getKey(Key original, @Nullable Key def) {
        if (size() == 0)
            return def;

        if (original instanceof LazyKey) {
            return getKey(((LazyKey<?>) original).getBaseKey());
        }

        Map map;

        if (original instanceof MaterialKey) {
            map = this.innerMap.getIfPresent().orElse(null);
        } else if (original instanceof CustomKey) {
            map = this.customInnerMap.getIfPresent().orElse(null);
        } else {
            map = null;
        }

        if (map != null) {
            if (map.containsKey(original))
                return original;
            Key globalKey = ((BaseKey<?>) original).toGlobalKey();
            if (globalKey != original && map.containsKey(globalKey))
                return globalKey;
        }

        return def;
    }

    @Override
    public boolean removeIf(Predicate<Key> predicate) {
        if (isEmpty())
            return false;

        boolean removed = false;

        {
            Map<MaterialKey, V> innerMap = this.innerMap.getIfPresent().orElse(null);
            if (innerMap != null)
                removed |= innerMap.entrySet().removeIf(entry -> predicate.test(entry.getKey()));
        }

        {
            Map<CustomKey, V> customInnerMap = this.customInnerMap.getIfPresent().orElse(null);
            if (customInnerMap != null)
                removed |= customInnerMap.entrySet().removeIf(entry -> predicate.test(entry.getKey()));
        }

        return removed;
    }

    @Override
    public V getRaw(Key key, V def) {
        if (size() == 0)
            return def;

        return getRawNoSizeCheck(key, def);
    }

    private V getRawNoSizeCheck(Key key, V def) {
        if (key instanceof LazyKey) {
            return getRawNoSizeCheck(((LazyKey<?>) key).getBaseKey(), def);
        }

        return Optional.ofNullable(getInternal(key, false)).orElse(def);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return Optional.ofNullable(get(key)).orElse(defaultValue);
    }

    @Override
    public Map<Key, V> asMap() {
        return this;
    }

}
