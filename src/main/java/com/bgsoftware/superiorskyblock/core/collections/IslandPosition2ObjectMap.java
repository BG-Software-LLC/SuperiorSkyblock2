package com.bgsoftware.superiorskyblock.core.collections;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.core.collections.view.Long2ObjectMapView;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

public class IslandPosition2ObjectMap<V> {

    @Nullable
    private Values values;

    private final Map<String, Long2ObjectMapView<V>> backendMap = new LinkedHashMap<>();
    private int size = 0;

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size == 0;
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
    public V put(String worldName, long packedPos, V value) {
        Long2ObjectMapView<V> worldBackendData = this.backendMap.computeIfAbsent(worldName, n ->
                CollectionsFactory.createLong2ObjectLinkedHashMap());

        V oldValue = worldBackendData.put(packedPos, value);

        if (oldValue == null)
            ++this.size;

        return oldValue;
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

    public void clear() {
        this.backendMap.clear();
        this.size = 0;
    }

    @NotNull
    public Collection<V> values() {
        return this.values == null ? (this.values = new Values()) : this.values;
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
            throw new UnsupportedOperationException();
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

    private abstract class Itr<T> implements Iterator<T> {

        protected final Iterator<Map.Entry<String, Long2ObjectMapView<V>>> worldsIterator;
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

                Map.Entry<String, Long2ObjectMapView<V>> nextWorld = this.worldsIterator.next();
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

}
