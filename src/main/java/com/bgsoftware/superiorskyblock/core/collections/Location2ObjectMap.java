package com.bgsoftware.superiorskyblock.core.collections;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2ObjectMapView;
import com.google.common.base.Preconditions;
import org.bukkit.Location;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

public class Location2ObjectMap<V> extends AbstractMap<Location, V> {

    private static final boolean IS_TALL_WORLD = ServerVersion.isAtLeast(ServerVersion.v1_18);

    @Nullable
    private KeySet keySet;
    @Nullable
    private Values values;
    @Nullable
    private EntrySet entrySet;

    private final Chunk2ObjectMap<ChunkMap<V>> backendMap = new Chunk2ObjectMap<>();
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
        return key instanceof Location ? get((Location) key) : null;
    }

    @Nullable
    private V get(Location location) {
        String worldName = LazyWorldLocation.getWorldName(location);
        if (worldName == null)
            return null;

        int blockX = location.getBlockX();
        int blockZ = location.getBlockZ();

        long chunkPair = computeChunkPair(blockX >> 4, blockZ >> 4);

        ChunkMap<V> chunkMap = this.backendMap.get(worldName, chunkPair);
        if (chunkMap == null)
            return null;

        return chunkMap.get(blockX & 0xF, location.getBlockY(), blockZ & 0xF);
    }

    @Nullable
    @Override
    public V put(Location location, V value) {
        String worldName = LazyWorldLocation.getWorldName(location);
        Preconditions.checkArgument(worldName != null, "cannot insert location with null world");

        int blockX = location.getBlockX();
        int blockZ = location.getBlockZ();

        long chunkPair = computeChunkPair(blockX >> 4, blockZ >> 4);

        ChunkMap<V> chunkMap = this.backendMap.computeIfAbsent(worldName, chunkPair, ChunkMap::new);

        V oldValue = chunkMap.put(blockX & 0xF, location.getBlockY(), blockZ & 0xF, value);

        if (oldValue == null)
            ++this.size;

        return oldValue;
    }

    @Override
    public V remove(Object key) {
        return key instanceof Location ? remove((Location) key) : null;
    }

    @Nullable
    public V remove(Location location) {
        String worldName = LazyWorldLocation.getWorldName(location);
        if (worldName == null)
            return null;

        int blockX = location.getBlockX();
        int blockZ = location.getBlockZ();

        long chunkPair = computeChunkPair(blockX >> 4, blockZ >> 4);

        ChunkMap<V> chunkMap = this.backendMap.get(worldName, chunkPair);
        if (chunkMap == null)
            return null;

        V oldValue = onRemove(chunkMap.remove(blockX & 0xF, location.getBlockY(), blockZ & 0xF));
        if (oldValue == null)
            return null;

        if (chunkMap.isEmpty()) {
            this.backendMap.remove(worldName, chunkPair);
        }

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
    public Set<Location> keySet() {
        return this.keySet == null ? (this.keySet = new KeySet()) : this.keySet;
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return this.values == null ? (this.values = new Values()) : this.values;
    }

    public void forEach(ChunkPosition chunkPosition, Consumer<V> consumer) {
        ChunkMap<V> chunkMap = this.backendMap.get(chunkPosition.getWorldName(), chunkPosition.asPair());
        if (chunkMap == null)
            return;

        Iterator<V> iterator = chunkMap.backendData.valueIterator();
        while (iterator.hasNext())
            consumer.accept(iterator.next());
    }

    public void removeAll(ChunkPosition chunkPosition, Consumer<V> predicate) {
        ChunkMap<V> chunkMap = this.backendMap.get(chunkPosition.getWorldName(), chunkPosition.asPair());
        if (chunkMap == null)
            return;

        Iterator<V> iterator = chunkMap.backendData.valueIterator();
        while (iterator.hasNext())
            predicate.accept(iterator.next());

        chunkMap.backendData.clear();
    }

    @NotNull
    @Override
    public Set<Entry<Location, V>> entrySet() {
        return this.entrySet == null ? (this.entrySet = new EntrySet()) : this.entrySet;
    }

    private static long computeChunkPair(int chunkX, int chunkZ) {
        return ((chunkZ & 0xFFFFFFFFL) << 32) | (chunkX & 0xFFFFFFFFL);
    }

    private static int computeBlockIndex(int relativeX, int blockY, int relativeZ) {
        Preconditions.checkArgument(relativeX >= 0 && relativeX <= 0xF, "invalid relativeX: " + relativeX);
        Preconditions.checkArgument(relativeZ >= 0 && relativeZ <= 0xF, "invalid blockZ: " + relativeZ);

        int worldMinHeight = IS_TALL_WORLD ? 0 : -64;

        short relativeY = (short) (blockY - worldMinHeight);
        return (relativeY << 8) | (relativeX << 4) | relativeZ;
    }

    private static Location computeLocationFromBlockAndChunk(String worldName, long chunkPair, int blockIdx) {
        int chunkX = Chunk2ObjectMap.getChunkXFromPair(chunkPair);
        int chunkZ = Chunk2ObjectMap.getChunkZFromPair(chunkPair);

        int blockX = (chunkX << 4) + getRelativeBlockXFromIndex(blockIdx);
        int blockY = (IS_TALL_WORLD ? 0 : -64) + getRelativeBlockYFromIndex(blockIdx);
        int blockZ = (chunkZ << 4) + getRelativeBlockZFromIndex(blockIdx);

        return new LazyWorldLocation(worldName, blockX, blockY, blockZ, 0, 0);
    }

    private static int getRelativeBlockXFromIndex(int blockIdx) {
        return (blockIdx >> 4) & 0xF;
    }

    private static int getRelativeBlockYFromIndex(int blockIdx) {
        return blockIdx >> 8;
    }

    private static int getRelativeBlockZFromIndex(int blockIdx) {
        return blockIdx & 0xF;
    }

    private static class ChunkMap<V> {

        private final Int2ObjectMapView<V> backendData = CollectionsFactory.createInt2ObjectLinkedHashMap();

        @Nullable
        public V get(int relativeX, int blockY, int relativeZ) {
            int blockIdx = computeBlockIndex(relativeX, blockY, relativeZ);
            return get(blockIdx);
        }

        @Nullable
        public V get(int blockIdx) {
            return this.backendData.get(blockIdx);
        }

        @Nullable
        public V put(int relativeX, int blockY, int relativeZ, V value) {
            int blockIdx = computeBlockIndex(relativeX, blockY, relativeZ);
            return put(blockIdx, value);
        }

        @Nullable
        public V put(int blockIdx, V value) {
            return this.backendData.put(blockIdx, value);
        }

        @Nullable
        public V remove(int relativeX, int blockY, int relativeZ) {
            int blockIdx = computeBlockIndex(relativeX, blockY, relativeZ);
            return this.remove(blockIdx);
        }

        @Nullable
        public V remove(int blockIdx) {
            return this.backendData.remove(blockIdx);
        }

        public boolean isEmpty() {
            return this.backendData.isEmpty();
        }

    }

    private class KeySet extends AbstractSet<Location> {

        @Override
        public int size() {
            return Location2ObjectMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return Location2ObjectMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return Location2ObjectMap.this.containsKey(o);
        }

        @NotNull
        @Override
        public Iterator<Location> iterator() {
            return new KeySetItr();
        }

        @Override
        public void clear() {
            Location2ObjectMap.this.clear();
        }

        private class KeySetItr extends Itr<Location> {

            @Override
            protected Location getNext() {
                return computeLocationFromBlockAndChunk(this.currWorld, this.currChunk, this.currBlockIdx);
            }

        }

    }

    private class Values extends AbstractCollection<V> {

        @Override
        public int size() {
            return Location2ObjectMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return Location2ObjectMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return Location2ObjectMap.this.containsValue(o);
        }

        @NotNull
        @Override
        public Iterator<V> iterator() {
            return new ValuesItr();
        }

        @Override
        public void clear() {
            Location2ObjectMap.this.clear();
        }

        private class ValuesItr extends Itr<V> {

            @Override
            protected V getNext() {
                return this.currValue;
            }

        }

    }

    private class EntrySet extends AbstractSet<Entry<Location, V>> {

        @Override
        public int size() {
            return Location2ObjectMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return Location2ObjectMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            Location key = null;

            if (o instanceof Entry) {
                o = ((Entry<?, ?>) o).getKey();
            }

            if (o instanceof Location)
                key = (Location) o;

            return key != null && Location2ObjectMap.this.containsKey(o);
        }

        @NotNull
        @Override
        public Iterator<Entry<Location, V>> iterator() {
            return new EntrySetItr();
        }

        @Override
        public void clear() {
            Location2ObjectMap.this.clear();
        }

        private class EntrySetItr extends Itr<Entry<Location, V>> {

            @Override
            protected Entry<Location, V> getNext() {
                return new EntryImpl(this.currWorld, this.currChunk, this.currBlockIdx, this.currValue);
            }

            private class EntryImpl implements Entry<Location, V> {

                private final String worldName;
                private final long chunkPair;
                private final int blockIdx;

                private Location cachedLocation;
                private V cachedValue;


                EntryImpl(String worldName, long chunkPair, int blockIdx, V value) {
                    this.worldName = worldName;
                    this.chunkPair = chunkPair;
                    this.blockIdx = blockIdx;
                    this.cachedValue = value;
                }

                @Override
                public Location getKey() {
                    return this.cachedLocation == null ?
                            (this.cachedLocation = computeLocationFromBlockAndChunk(this.worldName, this.chunkPair, this.blockIdx)) :
                            this.cachedLocation;
                }

                @Override
                public V getValue() {
                    if (this.cachedValue == null) {
                        this.cachedValue = Location2ObjectMap.this.backendMap
                                .get(this.worldName, this.chunkPair)
                                .get(this.blockIdx);
                    }

                    return this.cachedValue;
                }

                @Override
                public V setValue(V v) {
                    V oldValue = this.cachedValue;
                    this.cachedValue = v;

                    Location2ObjectMap.this.backendMap
                            .get(this.worldName, this.chunkPair)
                            .put(this.blockIdx, this.cachedValue);

                    return oldValue;
                }
            }

        }

    }

    private abstract class Itr<T> implements Iterator<T> {

        protected final Iterator<Entry<ChunkPosition, ChunkMap<V>>> worldsIterator;
        protected Iterator<Int2ObjectMapView.Entry<V>> currChunkIterator = Collections.emptyIterator();
        protected String currWorld;
        protected long currChunk;
        protected ChunkMap<V> currChunkMap;
        protected int currBlockIdx;
        protected V currValue;

        protected Itr() {
            this.worldsIterator = Location2ObjectMap.this.backendMap.entrySet().iterator();
        }

        @Override
        public final boolean hasNext() {
            return this.currChunkIterator.hasNext() || this.worldsIterator.hasNext();
        }

        @Override
        public final T next() {
            if (!this.currChunkIterator.hasNext()) {
                Entry<ChunkPosition, ChunkMap<V>> nextChunk = this.worldsIterator.next();
                this.currWorld = nextChunk.getKey().getWorldName();
                this.currChunk = nextChunk.getKey().asPair();
                this.currChunkMap = nextChunk.getValue();
                this.currChunkIterator = this.currChunkMap.backendData.entryIterator();
            }

            Int2ObjectMapView.Entry<V> nextBlock = this.currChunkIterator.next();
            this.currBlockIdx = nextBlock.getKey();
            this.currValue = nextBlock.getValue();

            return getNext();
        }

        protected abstract T getNext();

        @Override
        public final void remove() {
            this.currChunkIterator.remove();
            Location2ObjectMap.this.onRemove(this.currValue);

            if (this.currChunkMap.isEmpty())
                this.worldsIterator.remove();
        }
    }

}
