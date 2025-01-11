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
        return runOnMap(KeyMap::size, 0);
    }

    @Override
    public boolean containsKey(Object o) {
        return runOnMap(m -> m.containsKey(o), false);
    }

    @Override
    public V get(Object obj) {
        return runOnMap(m -> m.get(obj), null);
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
        return runOnMap(m -> m.remove(key), null);
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
        return runOnMap(m -> m.getKey(original), null);
    }

    @Override
    public Key getKey(Key original, @Nullable Key def) {
        return runOnMap(m -> m.getKey(original, def), def);
    }


    @Override
    public boolean removeIf(Predicate<Key> predicate) {
        return runOnMap(m -> m.removeIf(predicate), false);
    }

    @Override
    public V getRaw(Key key, V def) {
        return runOnMap(m -> m.getRaw(key, def), def);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return runOnMap(m -> m.getOrDefault(key, defaultValue), defaultValue);
    }

    @Override
    public Map<Key, V> asMap() {
        return runOnMap(KeyMap::asMap, this);
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
