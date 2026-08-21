package com.bgsoftware.superiorskyblock.core.collections;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Identified;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.collections.view.IntIterator;
import com.google.common.collect.Iterators;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class IdMap<K extends Identified, V> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Int2ObjectMapView<V> ids = CollectionsFactory.createInt2ObjectLinkedHashMap();

    public static <V> IdMap<SuperiorPlayer, V> newPlayersMap() {
        return new SuperiorPlayerIdMap<>();
    }

    public static <V> IdMap<Island, V> newIslandsMap() {
        return new IslandIdMap<>();
    }

    public int size() {
        return this.ids.size();
    }

    public void clear() {
        this.ids.clear();
    }

    @Nullable
    public V put(K key, V value) {
        return this.ids.put(key.getId(), value);
    }

    public void putAll(IdMap<K, ? super V> other) {
        if (other.size() == 0)
            return;

        Iterator iterator = other.ids.entryIterator();
        while (iterator.hasNext()) {
            Int2ObjectMapView.Entry<V> entry = (Int2ObjectMapView.Entry<V>) iterator.next();
            this.ids.put(entry.getKey(), entry.getValue());
        }
    }

    @Nullable
    public V remove(K key) {
        return this.ids.remove(key.getId());
    }

    @Nullable
    public V get(K key) {
        return this.ids.get(key.getId());
    }

    public V getOrDefault(K key, V defaultValue) {
        V value = get(key);
        return value == null ? defaultValue : value;
    }

    public Iterator<K> keyIterator() {
        return this.size() == 0 ? Iterators.emptyIterator() : new KeyIteratorImpl();
    }

    public Iterator<V> valueIterator() {
        return this.size() == 0 ? Iterators.emptyIterator() : this.ids.valueIterator();
    }

    public Iterator<Map.Entry<K, V>> entryIterator() {
        return this.size() == 0 ? Iterators.emptyIterator() : new EntryIteratorImpl();
    }

    public V computeIfAbsent(K key, Function<K, V> function) {
        V value = get(key);
        if (value != null)
            return value;

        value = function.apply(key);
        put(key, value);
        return value;
    }

    public Map<K, V> asMapView() {
        if (size() == 0)
            return Collections.emptyMap();

        IdMap<K, V> copy = newInstanceInternal();
        copy.putAll(this);
        return new MapViewImpl<>(copy);
    }

    @Override
    public boolean equals(Object o) {
        return o == this || this.ids.equals(o);
    }

    @Override
    public int hashCode() {
        return this.ids.hashCode();
    }

    protected abstract IdMap<K, V> newInstanceInternal();

    protected abstract K getKeyFromId(int id);

    private class KeyIteratorImpl implements Iterator<K> {

        private final IntIterator delegate = ids.keyIterator();

        @Override
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        @Override
        public K next() {
            return getKeyFromId(this.delegate.next());
        }

        @Override
        public void remove() {
            this.delegate.remove();
        }

    }

    private class EntryIteratorImpl implements Iterator<Map.Entry<K, V>> {

        private final Iterator<Int2ObjectMapView.Entry<V>> delegate = ids.entryIterator();

        @Override
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        @Override
        public Map.Entry<K, V> next() {
            return new EntryImpl(this.delegate.next());
        }

        @Override
        public void remove() {
            this.delegate.remove();
        }

        private class EntryImpl implements Map.Entry<K, V> {

            private final Int2ObjectMapView.Entry<V> handle;

            EntryImpl(Int2ObjectMapView.Entry<V> handle) {
                this.handle = handle;
            }

            @Override
            public K getKey() {
                return getKeyFromId(this.handle.getKey());
            }

            @Override
            public V getValue() {
                return this.handle.getValue();
            }

            @Override
            public V setValue(V v) {
                return this.handle.setValue(v);
            }
        }

    }

    private static class MapViewImpl<K extends Identified, V> extends AbstractMap<K, V> {

        private final IdMap<K, V> handle;

        private KeySet keySet;
        private EntrySet entrySet;
        private Values valuesCollection;

        MapViewImpl(IdMap<K, V> handle) {
            this.handle = handle;
        }

        @Override
        public int size() {
            return this.handle.size();
        }

        @Override
        public boolean isEmpty() {
            return this.handle.size() == 0;
        }

        @Override
        public boolean containsKey(Object o) {
            return this.handle.get((K) o) != null;
        }

        @Override
        public boolean containsValue(Object o) {
            return Iterators.find(this.handle.valueIterator(), e -> e.equals(o)) != null;
        }

        @Override
        public V get(Object o) {
            return this.handle.get((K) o);
        }

        @Override
        public V put(K k, V v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> map) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull Set<K> keySet() {
            return this.keySet == null ? (this.keySet = new KeySet()) : this.keySet;
        }

        @Override
        public @NotNull Set<Entry<K, V>> entrySet() {
            return this.entrySet == null ? (this.entrySet = new EntrySet()) : this.entrySet;
        }

        @Override
        public @NotNull Collection<V> values() {
            return this.valuesCollection == null ? (this.valuesCollection = new Values()) : this.valuesCollection;
        }

        @Override
        public boolean equals(Object o) {
            return this == o || this.handle.equals(o);
        }

        @Override
        public int hashCode() {
            return this.handle.hashCode();
        }

        @Override
        public V getOrDefault(Object o, V v) {
            V value = get(o);
            return value == null ? v : value;
        }

        @Override
        public void replaceAll(BiFunction<? super K, ? super V, ? extends V> biFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @Nullable V putIfAbsent(K k, V v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object var1, Object var2) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean replace(K var1, V var2, V var3) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V replace(K var1, V var2) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V computeIfAbsent(K var1, Function<? super K, ? extends V> var2) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V computeIfPresent(K var1, BiFunction<? super K, ? super V, ? extends V> var2) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V compute(K var1, BiFunction<? super K, ? super V, ? extends V> var2) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V merge(K var1, V var2, BiFunction<? super V, ? super V, ? extends V> var3) {
            throw new UnsupportedOperationException();
        }

        private class KeySet extends AbstractSet<K> {

            @Override
            public int size() {
                return MapViewImpl.this.size();
            }

            @Override
            public boolean isEmpty() {
                return MapViewImpl.this.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return MapViewImpl.this.containsKey(o);
            }

            @NotNull
            @Override
            public Iterator<K> iterator() {
                return MapViewImpl.this.handle.keyIterator();
            }

            @NotNull
            @Override
            public Object[] toArray() {
                Object[] arr = new Object[size()];
                int i = 0;
                Iterator<K> iterator = MapViewImpl.this.handle.keyIterator();
                while (iterator.hasNext())
                    arr[i++] = iterator.next();
                return arr;
            }

            @NotNull
            @Override
            public <T> T[] toArray(@NotNull T[] ts) {
                Object[] arr = ts.length >= size() ? ts : Arrays.copyOf(ts, size());
                int i = 0;
                Iterator<K> iterator = MapViewImpl.this.handle.keyIterator();
                while (iterator.hasNext())
                    arr[i++] = iterator.next();
                return (T[]) arr;
            }

            @Override
            public void clear() {
                MapViewImpl.this.clear();
            }
        }

        private class EntrySet extends AbstractSet<Entry<K, V>> {

            @Override
            public int size() {
                return MapViewImpl.this.size();
            }

            @Override
            public boolean isEmpty() {
                return MapViewImpl.this.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return MapViewImpl.this.containsKey(o);
            }

            @NotNull
            @Override
            public Iterator<Entry<K, V>> iterator() {
                return MapViewImpl.this.handle.entryIterator();
            }

            @Override
            public void clear() {
                MapViewImpl.this.clear();
            }

        }

        private class Values extends AbstractCollection<V> {

            @Override
            public int size() {
                return MapViewImpl.this.size();
            }

            @Override
            public boolean isEmpty() {
                return MapViewImpl.this.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return MapViewImpl.this.containsValue(o);
            }

            @NotNull
            @Override
            public Iterator<V> iterator() {
                return MapViewImpl.this.handle.valueIterator();
            }

            @NotNull
            @Override
            public Object[] toArray() {
                Object[] arr = new Object[size()];
                int i = 0;
                Iterator<V> iterator = MapViewImpl.this.handle.valueIterator();
                while (iterator.hasNext())
                    arr[i++] = iterator.next();
                return arr;
            }

            @NotNull
            @Override
            public <T> T[] toArray(@NotNull T[] ts) {
                Object[] arr = ts.length >= size() ? ts : Arrays.copyOf(ts, size());
                int i = 0;
                Iterator<V> iterator = MapViewImpl.this.handle.valueIterator();
                while (iterator.hasNext())
                    arr[i++] = iterator.next();
                return (T[]) arr;
            }

            @Override
            public void clear() {
                MapViewImpl.this.clear();
            }
        }

    }

    private static class SuperiorPlayerIdMap<V> extends IdMap<SuperiorPlayer, V> {

        @Override
        protected IdMap<SuperiorPlayer, V> newInstanceInternal() {
            return new SuperiorPlayerIdMap<>();
        }

        @Override
        protected SuperiorPlayer getKeyFromId(int id) {
            return plugin.getPlayers().getPlayersContainer().getSuperiorPlayer(id);
        }
    }

    private static class IslandIdMap<V> extends IdMap<Island, V> {

        @Override
        protected IdMap<Island, V> newInstanceInternal() {
            return new IslandIdMap<>();
        }

        @Override
        protected Island getKeyFromId(int id) {
            return plugin.getGrid().getIslandsContainer().getIslandById(id);
        }
    }

}
