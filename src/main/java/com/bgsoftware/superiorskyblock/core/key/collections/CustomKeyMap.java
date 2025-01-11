package com.bgsoftware.superiorskyblock.core.key.collections;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.key.BaseKey;
import com.bgsoftware.superiorskyblock.core.key.types.LazyKey;
import com.google.common.base.Preconditions;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class CustomKeyMap<V> extends AbstractMap<Key, V> implements KeyMap<V> {

    private final LazyReference<Map<Key, V>> innerMap;

    protected CustomKeyMap(KeyMapStrategy strategy) {
        this.innerMap = new LazyReference<Map<Key, V>>() {
            @Override
            protected Map<Key, V> create() {
                return strategy.create(false);
            }
        };
    }

    @Override
    public int size() {
        return this.innerMap.getIfPresent().map(Map::size).orElse(0);
    }

    @Override
    public boolean containsKey(Object key) {
        return this.get(key) != null;
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

        V value = this.innerMap.getIfPresent().map(m -> m.get(key)).orElse(null);

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

        return this.innerMap.get().put(key, value);
    }

    @Override
    public V remove(Object key) {
        return size() == 0 ? null : removeNoSizeCheck(key);
    }

    private V removeNoSizeCheck(Object key) {
        if (key instanceof LazyKey) {
            return removeNoSizeCheck(((LazyKey<?>) key).getBaseKey());
        }

        return this.innerMap.getIfPresent().map(m -> m.remove(key)).orElse(null);
    }

    @Override
    public void clear() {
        this.innerMap.getIfPresent().ifPresent(Map::clear);
    }

    @NotNull
    @Override
    public Set<Entry<Key, V>> entrySet() {
        return this.innerMap.getIfPresent().map(Map::entrySet).orElse(Collections.emptySet());
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

        Map<Key, V> map = this.innerMap.getIfPresent().orElse(null);

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

        return this.innerMap.getIfPresent().map(m ->
                        m.entrySet().removeIf(entry -> predicate.test(entry.getKey())))
                .orElse(false);
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
        V value = get(key);
        return value == null ? defaultValue : value;
    }

    @Override
    public Map<Key, V> asMap() {
        return this;
    }

}
