package com.bgsoftware.superiorskyblock.core.collections;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.core.IslandPosition;
import com.bgsoftware.superiorskyblock.core.collections.view.Long2ObjectMapView;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;

public class IslandPosition2ObjectMap<V> extends AbstractMap<IslandPosition, V> {

    @Nullable
    private KeySet keySet;
    @Nullable
    private Values values;
    @Nullable
    private EntrySet entrySet;

    private final Map<String, Long2ObjectMapView<V>> backendMap = new LinkedHashMap<>();
    private int size = 0;

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return value != null && super.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return key instanceof IslandPosition ? get((IslandPosition) key) : null;
    }

    @Nullable
    public V get(IslandPosition islandPosition) {
        return get(islandPosition.getWorldName(), islandPosition.pack());
    }

    @Nullable
    public V get(String worldName, long packedPos) {
        Long2ObjectMapView<V> worldBackendData = this.backendMap.get(worldName);
        if (worldBackendData == null)
            return null;

        return worldBackendData.get(packedPos);
    }

    public V computeIfAbsent(String worldName, long packedPos, Supplier<V> newValue) {
        Long2ObjectMapView<V> worldBackendData = this.backendMap.computeIfAbsent(worldName, n ->
                CollectionsFactory.createLong2ObjectLinkedHashMap());

        return worldBackendData.computeIfAbsent(packedPos, p -> {
            ++IslandPosition2ObjectMap.this.size;
            return newValue.get();
        });
    }

    @Nullable
    @Override
    public V put(IslandPosition islandPosition, V value) {
        return put(islandPosition.getWorldName(), islandPosition.pack(), value);
    }

    @Nullable
    public V put(String worldName, long packedPos, V value) {
        Long2ObjectMapView<V> worldBackendData = this.backendMap.computeIfAbsent(worldName, n ->
                CollectionsFactory.createLong2ObjectLinkedHashMap());

        V oldValue = worldBackendData.put(packedPos, value);

        if (oldValue == null)
            ++this.size;

        return oldValue;
    }

    @Override
    public V remove(Object key) {
        return key instanceof IslandPosition ? remove((IslandPosition) key) : null;
    }

    @Nullable
    public V remove(IslandPosition islandPosition) {
        return remove(islandPosition.getWorldName(), islandPosition.pack());
    }

    @Nullable
    public V remove(String worldName, long packedPos) {
        Long2ObjectMapView<V> worldBackendData = this.backendMap.get(worldName);
        if (worldBackendData == null)
            return null;

        V oldValue = onRemove(worldBackendData.remove(packedPos));
        if (oldValue == null)
            return null;

        if (worldBackendData.isEmpty())
            IslandPosition2ObjectMap.this.backendMap.remove(worldName);

        return oldValue;
    }

    @Nullable
    private V onRemove(@Nullable V removedValue) {
        if (removedValue != null)
            --this.size;
        return removedValue;
    }

    @Override
    public void clear() {
        this.backendMap.clear();
        this.size = 0;
    }

    @NotNull
    @Override
    public Set<IslandPosition> keySet() {
        return this.keySet == null ? (this.keySet = new KeySet()) : this.keySet;
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return this.values == null ? (this.values = new Values()) : this.values;
    }

    @NotNull
    @Override
    public Set<Entry<IslandPosition, V>> entrySet() {
        return this.entrySet == null ? (this.entrySet = new EntrySet()) : this.entrySet;
    }

    private class KeySet extends AbstractSet<IslandPosition> {

        @Override
        public int size() {
            return IslandPosition2ObjectMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return IslandPosition2ObjectMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return IslandPosition2ObjectMap.this.containsKey(o);
        }

        @NotNull
        @Override
        public Iterator<IslandPosition> iterator() {
            return new KeySetItr();
        }

        @Override
        public void clear() {
            IslandPosition2ObjectMap.this.clear();
        }

        private class KeySetItr extends Itr<IslandPosition> {

            private final MutableIslandPosition mutableIslandPosition = new MutableIslandPosition();

            @Override
            protected IslandPosition getNext() {
                return this.mutableIslandPosition.reset(this.currentWorld, this.currentPosition);
            }
        }

    }

    private class Values extends AbstractCollection<V> {

        @Override
        public int size() {
            return IslandPosition2ObjectMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return IslandPosition2ObjectMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return IslandPosition2ObjectMap.this.containsValue(o);
        }

        @NotNull
        @Override
        public Iterator<V> iterator() {
            return new ValuesItr();
        }

        @Override
        public void clear() {
            IslandPosition2ObjectMap.this.clear();
        }

        private class ValuesItr extends Itr<V> {

            @Override
            protected V getNext() {
                return this.currentValue;
            }

        }

    }

    private class EntrySet extends AbstractSet<Entry<IslandPosition, V>> {

        @Override
        public int size() {
            return IslandPosition2ObjectMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return IslandPosition2ObjectMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            IslandPosition key = null;

            if (o instanceof Entry) {
                o = ((Entry<?, ?>) o).getKey();
            }

            if (o instanceof IslandPosition)
                key = (IslandPosition) o;

            return key != null && IslandPosition2ObjectMap.this.containsKey(o);
        }

        @NotNull
        @Override
        public Iterator<Entry<IslandPosition, V>> iterator() {
            return new EntrySetItr();
        }

        @Override
        public void clear() {
            IslandPosition2ObjectMap.this.clear();
        }

        private class EntrySetItr extends Itr<Entry<IslandPosition, V>> {

            private final MutableIslandPosition mutableIslandPosition = new MutableIslandPosition();
            private final EntryImpl mutableEntry = new EntryImpl();

            @Override
            protected Entry<IslandPosition, V> getNext() {
                return mutableEntry.reset(
                        this.mutableIslandPosition.reset(this.currentWorld, this.currentPosition),
                        this.currentValue);
            }

            private class EntryImpl implements Entry<IslandPosition, V> {

                private IslandPosition islandPosition;
                private V cachedValue;

                EntryImpl() {

                }

                EntryImpl reset(IslandPosition islandPosition, V value) {
                    this.islandPosition = islandPosition;
                    this.cachedValue = value;
                    return this;
                }

                @Override
                public IslandPosition getKey() {
                    return this.islandPosition;
                }

                @Override
                public V getValue() {
                    return this.cachedValue;
                }

                @Override
                public V setValue(V v) {
                    V oldValue = this.cachedValue;
                    this.cachedValue = v;

                    Long2ObjectMapView<V> worldBackendData = IslandPosition2ObjectMap.this.backendMap
                            .get(this.islandPosition.getWorldName());

                    worldBackendData.put(this.islandPosition.pack(), this.cachedValue);

                    return oldValue;
                }
            }

        }

    }

    private abstract class Itr<T> implements Iterator<T> {

        protected final Iterator<Entry<String, Long2ObjectMapView<V>>> worldsIterator;
        protected Iterator<Long2ObjectMapView.Entry<V>> currentWorldIterator = Collections.emptyIterator();
        protected String currentWorld;
        protected long currentPosition;
        protected V currentValue;

        protected Itr() {
            this.worldsIterator = IslandPosition2ObjectMap.this.backendMap.entrySet().iterator();
        }

        @Override
        public final boolean hasNext() {
            return this.currentWorldIterator.hasNext() || this.worldsIterator.hasNext();
        }

        @Override
        public final T next() {
            if (!this.currentWorldIterator.hasNext()) {
                if (!this.worldsIterator.hasNext()) {
                    throw new NoSuchElementException();
                }

                Entry<String, Long2ObjectMapView<V>> nextWorld = this.worldsIterator.next();
                this.currentWorldIterator = nextWorld.getValue().entryIterator();
                this.currentWorld = nextWorld.getKey();
            }

            Long2ObjectMapView.Entry<V> nextPosition = this.currentWorldIterator.next();
            this.currentPosition = nextPosition.getKey();
            this.currentValue = nextPosition.getValue();

            return getNext();
        }

        protected abstract T getNext();

        @Override
        public final void remove() {
            Long2ObjectMapView<V> worldBackendData = IslandPosition2ObjectMap.this.backendMap.get(this.currentWorld);

            this.currentWorldIterator.remove();
            IslandPosition2ObjectMap.this.onRemove(this.currentValue);

            if (worldBackendData.isEmpty())
                this.worldsIterator.remove();
        }
    }

    private static class MutableIslandPosition extends IslandPosition {

        public MutableIslandPosition() {
            super();
        }

        public MutableIslandPosition reset(String worldName, long packedPos) {
            this.worldName = worldName;
            this.x = IslandPosition.getXFromPacked(packedPos);
            this.z = IslandPosition.getZFromPacked(packedPos);
            this.cachedPackedPos = packedPos;
            return this;
        }

    }

}
