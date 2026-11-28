package com.bgsoftware.superiorskyblock.core.key.map;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.key.types.EntityTypeKey;
import com.bgsoftware.superiorskyblock.core.key.types.LazyKey;
import com.bgsoftware.superiorskyblock.core.key.types.MaterialKey;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class LazyLoadedKeyMap<V> extends AbstractMap<Key, V> implements KeyMap<V> {

    private final KeyMapStrategy strategy;
    @Nullable
    private KeyMap<V> delegate;
    @Nullable
    private KeyMap<V> pendingCustomKeys;

    public LazyLoadedKeyMap(KeyMapStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public int size() {
        KeyMap<V> map = getCurrentMap();
        return map == null ? 0 : map.size();
    }

    @Override
    public boolean containsKey(Object o) {
        KeyMap<V> map = getCurrentMap();
        return map != null && map.containsKey(o);
    }

    @Override
    public V get(Object obj) {
        KeyMap<V> map = getCurrentMap();
        return map == null ? null : map.get(obj);
    }

    @Override
    public V put(Key key, V value) {
        if (this.delegate != null)
            return this.delegate.put(key, value);

        return putNoDelegate(key, value);
    }

    private V putNoDelegate(Key key, V value) {
        if (key instanceof LazyKey) {
            return putNoDelegate(((LazyKey<?>) key).getBaseKey(), value);
        }

        if (key instanceof EntityTypeKey) {
            this.delegate = new EntityTypeKeyMap<>(this.strategy);
            addPendingCustomKeys();
        } else if (key instanceof MaterialKey) {
            this.delegate = new MaterialKeyMap<>(this.strategy);
            addPendingCustomKeys();
        } else {
            if (this.pendingCustomKeys == null)
                this.pendingCustomKeys = new CustomKeyMap<>(this.strategy);

            return this.pendingCustomKeys.put(key, value);
        }

        return this.delegate.put(key, value);
    }

    @Override
    public V remove(Object key) {
        KeyMap<V> map = getCurrentMap();
        return map == null ? null : map.remove(key);
    }

    @Override
    public void clear() {
        runOnMap(KeyMap::clear);
    }

    @NotNull
    @Override
    public Set<Entry<Key, V>> entrySet() {
        return runOnMap(KeyMap::entrySet, Collections.emptySet());
    }

    @Override
    public String toString() {
        return runOnMap(KeyMap::toString, "LazyLoadedKeyMap{}");
    }

    @Nullable
    @Override
    public Key getKey(Key original) {
        KeyMap<V> map = getCurrentMap();
        return map == null ? null : map.getKey(original);
    }

    @Override
    public Key getKey(Key original, @Nullable Key def) {
        KeyMap<V> map = getCurrentMap();
        return map == null ? def : map.getKey(original, def);
    }


    @Override
    public boolean removeIf(Predicate<Key> predicate) {
        KeyMap<V> map = getCurrentMap();
        return map != null && map.removeIf(predicate);
    }

    @Override
    public V getRaw(Key key, V def) {
        KeyMap<V> map = getCurrentMap();
        return map == null ? def : map.getRaw(key, def);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        KeyMap<V> map = getCurrentMap();
        return map == null ? defaultValue : map.getOrDefault(key, defaultValue);
    }

    @Override
    public V merge(Key key, @NotNull V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(value);
        V oldValue = getRaw(key, null);
        V newValue = (oldValue == null) ? value : remappingFunction.apply(oldValue, value);
        if (newValue == null) {
            remove(key);
        } else {
            put(key, newValue);
        }
        return newValue;
    }

    @Override
    public Map<Key, V> asMap() {
        return runOnMap(KeyMap::asMap, this);
    }

    @Nullable
    private KeyMap<V> getCurrentMap() {
        return this.delegate != null ? this.delegate : this.pendingCustomKeys;
    }

    private <T> T runOnMap(Function<KeyMap<V>, T> function, T def) {
        if (this.delegate != null)
            return function.apply(this.delegate);

        else if (this.pendingCustomKeys != null)
            return function.apply(this.pendingCustomKeys);

        else
            return def;
    }

    private void runOnMap(Consumer<KeyMap<V>> consumer) {
        if (this.delegate != null)
            consumer.accept(this.delegate);

        else if (this.pendingCustomKeys != null)
            consumer.accept(this.pendingCustomKeys);
    }

    private void addPendingCustomKeys() {
        if (this.pendingCustomKeys == null)
            return;

        this.delegate.putAll(this.pendingCustomKeys);
        this.pendingCustomKeys = null;
    }

}
