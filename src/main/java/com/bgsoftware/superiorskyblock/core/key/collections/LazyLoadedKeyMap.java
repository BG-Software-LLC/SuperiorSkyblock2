package com.bgsoftware.superiorskyblock.core.key.collections;

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
import java.util.Set;
import java.util.function.Predicate;

public class LazyLoadedKeyMap<V> extends AbstractMap<Key, V> implements KeyMap<V> {

    private final KeyMapStrategy strategy;
    @Nullable
    private KeyMap<V> delegate;

    public LazyLoadedKeyMap(KeyMapStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public int size() {
        return this.delegate == null ? 0 : this.delegate.size();
    }

    @Override
    public boolean containsKey(Object o) {
        return this.delegate != null && this.delegate.containsKey(o);
    }

    @Override
    public V get(Object obj) {
        return this.delegate == null ? null : this.delegate.get(obj);
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
        } else if (key instanceof MaterialKey) {
            this.delegate = new MaterialKeyMap<>(this.strategy);
        } else {
            throw new IllegalArgumentException("Cannot insert key of type " + key.getClass());
        }

        return this.delegate.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return this.delegate == null ? null : this.delegate.remove(key);
    }

    @Override
    public void clear() {
        if (this.delegate != null)
            this.delegate.clear();
    }

    @NotNull
    @Override
    public Set<Entry<Key, V>> entrySet() {
        return this.delegate == null ? Collections.emptySet() : this.delegate.entrySet();
    }

    @Override
    public String toString() {
        return this.delegate == null ? "LazyLoadedKeyMap{}" : this.delegate.toString();
    }

    @Nullable
    @Override
    public Key getKey(Key original) {
        return this.delegate == null ? null : this.delegate.getKey(original);
    }

    @Override
    public Key getKey(Key original, @Nullable Key def) {
        return this.delegate == null ? def : this.delegate.getKey(original, def);
    }


    @Override
    public boolean removeIf(Predicate<Key> predicate) {
        return this.delegate != null && this.delegate.removeIf(predicate);
    }

    @Override
    public V getRaw(Key key, V def) {
        return this.delegate == null ? def : this.delegate.getRaw(key, def);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return this.delegate == null ? defaultValue : this.delegate.getOrDefault(key, defaultValue);
    }

    @Override
    public Map<Key, V> asMap() {
        return this.delegate == null ? this : this.delegate.asMap();
    }

}
