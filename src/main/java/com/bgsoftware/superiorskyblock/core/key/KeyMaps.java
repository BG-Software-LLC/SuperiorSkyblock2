package com.bgsoftware.superiorskyblock.core.key;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.key.collections.EntityTypeKeyMap;
import com.bgsoftware.superiorskyblock.core.key.collections.LazyLoadedKeyMap;
import com.bgsoftware.superiorskyblock.core.key.collections.MaterialKeyMap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class KeyMaps {

    @SuppressWarnings("rawtypes")
    private static final KeyMap EMPTY_MAP = new EmptyKeyMap();

    private KeyMaps() {

    }

    public static <V> KeyMap<V> createEmptyMap() {
        return (KeyMap<V>) EMPTY_MAP;
    }

    public static <V> KeyMap<V> unmodifiableKeyMap(KeyMap<V> delegate) {
        return delegate == EMPTY_MAP ? createEmptyMap() : new UnmodifiableKeyMap<>(delegate);
    }

    public static <V> KeyMap<V> createHashMap(KeyIndicator keyIndicator) {
        return createMap(keyIndicator, () -> new HashMap());
    }

    public static <V> KeyMap<V> createIdentityHashMap(KeyIndicator keyIndicator) {
        return createMap(keyIndicator, () -> new IdentityHashMap(), true);
    }

    public static <V> KeyMap<V> createConcurrentHashMap(KeyIndicator keyIndicator) {
        return createMap(keyIndicator, () -> new ConcurrentHashMap());
    }

    public static <V> KeyMap<V> createMap(KeyIndicator keyIndicator, Supplier<Map> mapCreator) {
        return createMap(keyIndicator, mapCreator, false);
    }

    private static <V> KeyMap<V> createMap(KeyIndicator keyIndicator, Supplier<Map> mapCreator, boolean identityMap) {
        switch (keyIndicator) {
            case MATERIAL:
                return MaterialKeyMap.createMap(mapCreator);
            case ENTITY_TYPE:
                return EntityTypeKeyMap.createMap(mapCreator, identityMap);
        }

        return LazyLoadedKeyMap.createMap(mapCreator, identityMap);
    }

    public static <V> KeyMap<V> createConcurrentHashMap(KeyIndicator keyIndicator, Map<Key, V> values) {
        KeyMap<V> keyMap = createConcurrentHashMap(keyIndicator);
        keyMap.putAll(values);
        return keyMap;
    }

    private static class EmptyKeyMap<V> implements KeyMap<V> {

        @Nullable
        @Override
        public Key getKey(Key original) {
            return null;
        }

        @Override
        public Key getKey(Key original, @Nullable Key def) {
            return def;
        }

        @Override
        public V getRaw(Key key, V def) {
            return def;
        }

        @Override
        public Map<Key, V> asMap() {
            return this;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            return false;
        }

        @Override
        public V get(Object key) {
            return null;
        }

        @NotNull
        @Override
        public Set<Key> keySet() {
            return Collections.emptySet();
        }

        @NotNull
        @Override
        public Collection<V> values() {
            return Collections.emptyList();
        }

        @NotNull
        @Override
        public Set<Entry<Key, V>> entrySet() {
            return Collections.emptySet();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Map && ((Map<?, ?>) obj).isEmpty();
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public V getOrDefault(Object key, V defaultValue) {
            return defaultValue;
        }

        @Override
        public void forEach(BiConsumer<? super Key, ? super V> action) {
            // Do nothing.
        }

        @Override
        public void replaceAll(BiFunction<? super Key, ? super V, ? extends V> function) {
            // Do nothing.
        }

        @Nullable
        @Override
        public V putIfAbsent(Key key, V value) {
            throw new UnsupportedOperationException("Cannot modify EmptyKeyMap");
        }

        @Override
        public V remove(Object key) {
            throw new UnsupportedOperationException("Cannot modify EmptyKeyMap");
        }

        @Override
        public boolean replace(Key key, V oldValue, V newValue) {
            throw new UnsupportedOperationException("Cannot modify EmptyKeyMap");
        }

        @Nullable
        @Override
        public V replace(Key key, V value) {
            throw new UnsupportedOperationException("Cannot modify EmptyKeyMap");
        }

        @Override
        public V computeIfAbsent(Key key, @NotNull Function<? super Key, ? extends V> mappingFunction) {
            throw new UnsupportedOperationException("Cannot modify EmptyKeyMap");
        }

        @Override
        public V computeIfPresent(Key key, @NotNull BiFunction<? super Key, ? super V, ? extends V> remappingFunction) {
            throw new UnsupportedOperationException("Cannot modify EmptyKeyMap");
        }

        @Override
        public V compute(Key key, @NotNull BiFunction<? super Key, ? super V, ? extends V> remappingFunction) {
            throw new UnsupportedOperationException("Cannot modify EmptyKeyMap");
        }

        @Override
        public V merge(Key key, @NotNull V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            throw new UnsupportedOperationException("Cannot modify EmptyKeyMap");
        }

        @Nullable
        @Override
        public V put(Key key, V value) {
            throw new UnsupportedOperationException("Cannot modify EmptyKeyMap");
        }

        @Override
        public void putAll(@NotNull Map<? extends Key, ? extends V> m) {
            throw new UnsupportedOperationException("Cannot modify EmptyKeyMap");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Cannot modify EmptyKeyMap");
        }

        @Override
        public boolean removeIf(Predicate<Key> predicate) {
            throw new UnsupportedOperationException("Cannot modify EmptyKeyMap");
        }

    }

    private static class UnmodifiableKeyMap<V> implements KeyMap<V> {

        private final KeyMap<V> delegate;

        UnmodifiableKeyMap(KeyMap<V> delegate) {
            this.delegate = delegate;
        }

        @Nullable
        @Override
        public Key getKey(Key original) {
            return this.delegate.getKey(original);
        }

        @Override
        public Key getKey(Key original, @Nullable Key def) {
            return this.delegate.getKey(original, def);
        }

        @Override
        public V getRaw(Key key, V def) {
            return this.delegate.getRaw(key, def);
        }

        @Override
        public Map<Key, V> asMap() {
            return this;
        }

        @Override
        public int size() {
            return this.delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return this.delegate.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return this.delegate.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return this.delegate.containsValue(value);
        }

        @Override
        public V get(Object key) {
            return this.delegate.get(key);
        }

        @NotNull
        @Override
        public Set<Key> keySet() {
            return this.delegate.keySet();
        }

        @NotNull
        @Override
        public Collection<V> values() {
            return this.delegate.values();
        }

        @NotNull
        @Override
        public Set<Entry<Key, V>> entrySet() {
            return this.delegate.entrySet();
        }

        @Override
        public boolean removeIf(Predicate<Key> predicate) {
            throw new UnsupportedOperationException("Cannot modify UnmodifiableKeyMap");
        }

        @Nullable
        @Override
        public V put(Key key, V value) {
            throw new UnsupportedOperationException("Cannot modify UnmodifiableKeyMap");
        }

        @Override
        public V remove(Object key) {
            throw new UnsupportedOperationException("Cannot modify UnmodifiableKeyMap");
        }

        @Override
        public void putAll(@NotNull Map<? extends Key, ? extends V> m) {
            throw new UnsupportedOperationException("Cannot modify UnmodifiableKeyMap");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Cannot modify UnmodifiableKeyMap");
        }

    }

}
