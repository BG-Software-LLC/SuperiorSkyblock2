package com.bgsoftware.superiorskyblock.core.collections.creator;

import com.bgsoftware.superiorskyblock.core.collections.view.Char2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.collections.view.CharIterator;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2IntMapView;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.collections.view.IntIterator;
import com.bgsoftware.superiorskyblock.core.collections.view.Long2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.collections.view.LongIterator;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.Iterator;
import java.util.Map;
import java.util.OptionalInt;

public class FastUtilCollectionsCreator implements CollectionsCreator {

    public static final FastUtilCollectionsCreator INSTANCE = new FastUtilCollectionsCreator();

    private FastUtilCollectionsCreator() {

    }

    @Override
    public <V> Int2ObjectMapView<V> createInt2ObjectLinkedHashMap() {
        return new Int2ObjectFastUtilMapView<>(new Int2ObjectLinkedOpenHashMap<>());
    }

    @Override
    public <V> Int2ObjectMapView<V> createInt2ObjectArrayMap() {
        return new Int2ObjectFastUtilMapView<>(new Int2ObjectArrayMap<>());
    }

    @Override
    public Int2IntMapView createInt2IntHashMap() {
        return new Int2IntFastUtilMapView(new Int2IntOpenHashMap());
    }

    @Override
    public Int2IntMapView createInt2IntArrayMap() {
        return new Int2IntFastUtilMapView(new Int2IntArrayMap());
    }

    @Override
    public <V> Long2ObjectMapView<V> createLong2ObjectHashMap() {
        return new Long2ObjectFastUtilMapView<>(new Long2ObjectOpenHashMap<>());
    }

    @Override
    public <V> Long2ObjectMapView<V> createLong2ObjectLinkedHashMap() {
        return new Long2ObjectFastUtilMapView<>(new Long2ObjectLinkedOpenHashMap<>());
    }

    @Override
    public <V> Long2ObjectMapView<V> createLong2ObjectArrayMap() {
        return new Long2ObjectFastUtilMapView<>(new Long2ObjectArrayMap<>());
    }

    @Override
    public <V> Char2ObjectMapView<V> createChar2ObjectArrayMap() {
        return new Char2ObjectFastUtilMapView<>(new Char2ObjectArrayMap<>());
    }

    private static class Int2ObjectFastUtilMapView<V> implements Int2ObjectMapView<V> {

        private final Int2ObjectMap<V> delegate;

        Int2ObjectFastUtilMapView(Int2ObjectMap<V> delegate) {
            this.delegate = delegate;
        }

        @Override
        public V put(int key, V value) {
            return this.delegate.put(key, value);
        }

        @Override
        public V get(int key) {
            return this.delegate.get(key);
        }

        @Override
        public V remove(int key) {
            return this.delegate.remove(key);
        }

        @Override
        public int size() {
            return this.delegate.size();
        }

        @Override
        public void clear() {
            this.delegate.clear();
        }

        @Override
        public Iterator<Entry<V>> entryIterator() {
            return new EntryIteratorWrapper<>(this.delegate.int2ObjectEntrySet().iterator());
        }

        @Override
        public Iterator<V> valueIterator() {
            return this.delegate.values().iterator();
        }

        @Override
        public IntIterator keyIterator() {
            return new IntIteratorWrapper(this.delegate.keySet().iterator());
        }

        private static class EntryIteratorWrapper<V> implements Iterator<Int2ObjectMapView.Entry<V>> {

            private final Iterator<Int2ObjectMap.Entry<V>> handle;

            EntryIteratorWrapper(Iterator<Int2ObjectMap.Entry<V>> handle) {
                this.handle = handle;
            }

            @Override
            public boolean hasNext() {
                return this.handle.hasNext();
            }

            @Override
            public Int2ObjectMapView.Entry<V> next() {
                return new EntryWrapper(this.handle.next());
            }

            @Override
            public void remove() {
                this.handle.remove();
            }

            private class EntryWrapper implements Int2ObjectMapView.Entry<V> {

                private final Int2ObjectMap.Entry<V> handle;

                EntryWrapper(Int2ObjectMap.Entry<V> handle) {
                    this.handle = handle;
                }

                @Override
                public int getKey() {
                    return this.handle.getIntKey();
                }

                @Override
                public V getValue() {
                    return this.handle.getValue();
                }

                @Override
                public V setValue(V newValue) {
                    return this.handle.setValue(newValue);
                }
            }

        }

    }

    private static class Int2IntFastUtilMapView implements Int2IntMapView {

        private final Int2IntMap delegate;

        Int2IntFastUtilMapView(Int2IntMap delegate) {
            this.delegate = delegate;
        }

        @Override
        public OptionalInt put(int key, int value) {
            int old = this.delegate.put(key, value);
            return old == this.delegate.defaultReturnValue() ? OptionalInt.empty() : OptionalInt.of(old);
        }

        @Override
        public OptionalInt get(int key) {
            int old = this.delegate.get(key);
            return old == this.delegate.defaultReturnValue() ? OptionalInt.empty() : OptionalInt.of(old);
        }

        @Override
        public OptionalInt remove(int key) {
            int old = this.delegate.remove(key);
            return old == this.delegate.defaultReturnValue() ? OptionalInt.empty() : OptionalInt.of(old);
        }

        @Override
        public int size() {
            return this.delegate.size();
        }

        @Override
        public void clear() {
            this.delegate.clear();
        }

        @Override
        public Iterator<Entry> entryIterator() {
            return new EntryIteratorWrapper(this.delegate.int2IntEntrySet().iterator());
        }

        @Override
        public IntIterator valueIterator() {
            return new IntIteratorWrapper(this.delegate.values().iterator());
        }

        @Override
        public IntIterator keyIterator() {
            return new IntIteratorWrapper(this.delegate.keySet().iterator());
        }

        @Override
        public Map<Integer, Integer> asMap() {
            return this.delegate;
        }

        private static class EntryIteratorWrapper implements Iterator<Int2IntMapView.Entry> {

            private final Iterator<Int2IntMap.Entry> handle;

            EntryIteratorWrapper(Iterator<Int2IntMap.Entry> handle) {
                this.handle = handle;
            }

            @Override
            public boolean hasNext() {
                return this.handle.hasNext();
            }

            @Override
            public Int2IntMapView.Entry next() {
                return new EntryWrapper(this.handle.next());
            }

            @Override
            public void remove() {
                this.handle.remove();
            }

            private static class EntryWrapper implements Int2IntMapView.Entry {

                private final Int2IntMap.Entry handle;

                EntryWrapper(Int2IntMap.Entry handle) {
                    this.handle = handle;
                }

                @Override
                public int getKey() {
                    return this.handle.getIntKey();
                }

                @Override
                public int getValue() {
                    return this.handle.getIntValue();
                }

                @Override
                public int setValue(int newValue) {
                    return this.handle.setValue(newValue);
                }
            }

        }

    }

    private static class Long2ObjectFastUtilMapView<V> implements Long2ObjectMapView<V> {

        private final Long2ObjectMap<V> delegate;

        Long2ObjectFastUtilMapView(Long2ObjectMap<V> delegate) {
            this.delegate = delegate;
        }

        @Override
        public V put(long key, V value) {
            return this.delegate.put(key, value);
        }

        @Override
        public V get(long key) {
            return this.delegate.get(key);
        }

        @Override
        public V remove(long key) {
            return this.delegate.remove(key);
        }

        @Override
        public int size() {
            return this.delegate.size();
        }

        @Override
        public void clear() {
            this.delegate.clear();
        }

        @Override
        public Iterator<Entry<V>> entryIterator() {
            return new EntryIteratorWrapper<>(this.delegate.long2ObjectEntrySet().iterator());
        }

        @Override
        public Iterator<V> valueIterator() {
            return this.delegate.values().iterator();
        }

        @Override
        public LongIterator keyIterator() {
            return new LongIteratorWrapper(this.delegate.keySet().iterator());
        }

        private static class EntryIteratorWrapper<V> implements Iterator<Long2ObjectMapView.Entry<V>> {

            private final Iterator<Long2ObjectMap.Entry<V>> handle;

            EntryIteratorWrapper(Iterator<Long2ObjectMap.Entry<V>> handle) {
                this.handle = handle;
            }

            @Override
            public boolean hasNext() {
                return this.handle.hasNext();
            }

            @Override
            public Long2ObjectMapView.Entry<V> next() {
                return new EntryWrapper(this.handle.next());
            }

            @Override
            public void remove() {
                this.handle.remove();
            }

            private class EntryWrapper implements Long2ObjectMapView.Entry<V> {

                private final Long2ObjectMap.Entry<V> handle;

                EntryWrapper(Long2ObjectMap.Entry<V> handle) {
                    this.handle = handle;
                }

                @Override
                public long getKey() {
                    return this.handle.getLongKey();
                }

                @Override
                public V getValue() {
                    return this.handle.getValue();
                }

                @Override
                public V setValue(V newValue) {
                    return this.handle.setValue(newValue);
                }
            }

        }

    }

    private static class Char2ObjectFastUtilMapView<V> implements Char2ObjectMapView<V> {

        private final Char2ObjectMap<V> delegate;

        Char2ObjectFastUtilMapView(Char2ObjectMap<V> delegate) {
            this.delegate = delegate;
        }

        @Override
        public V put(char key, V value) {
            return this.delegate.put(key, value);
        }

        @Override
        public V get(char key) {
            return this.delegate.get(key);
        }

        @Override
        public V remove(char key) {
            return this.delegate.remove(key);
        }

        @Override
        public int size() {
            return this.delegate.size();
        }

        @Override
        public void clear() {
            this.delegate.clear();
        }

        @Override
        public Iterator<Entry<V>> entryIterator() {
            return new EntryIteratorWrapper<>(this.delegate.char2ObjectEntrySet().iterator());
        }

        @Override
        public Iterator<V> valueIterator() {
            return this.delegate.values().iterator();
        }

        @Override
        public CharIterator keyIterator() {
            return new CharIteratorWrapper(this.delegate.keySet().iterator());
        }

        private static class EntryIteratorWrapper<V> implements Iterator<Char2ObjectMapView.Entry<V>> {

            private final Iterator<Char2ObjectMap.Entry<V>> handle;

            EntryIteratorWrapper(Iterator<Char2ObjectMap.Entry<V>> handle) {
                this.handle = handle;
            }

            @Override
            public boolean hasNext() {
                return this.handle.hasNext();
            }

            @Override
            public Char2ObjectMapView.Entry<V> next() {
                return new EntryWrapper(this.handle.next());
            }

            @Override
            public void remove() {
                this.handle.remove();
            }

            private class EntryWrapper implements Char2ObjectMapView.Entry<V> {

                private final Char2ObjectMap.Entry<V> handle;

                EntryWrapper(Char2ObjectMap.Entry<V> handle) {
                    this.handle = handle;
                }

                @Override
                public char getKey() {
                    return this.handle.getCharKey();
                }

                @Override
                public V getValue() {
                    return this.handle.getValue();
                }

                @Override
                public V setValue(V newValue) {
                    return this.handle.setValue(newValue);
                }
            }

        }

    }

    private static class IntIteratorWrapper implements IntIterator {

        private final it.unimi.dsi.fastutil.ints.IntIterator handle;

        IntIteratorWrapper(it.unimi.dsi.fastutil.ints.IntIterator handle) {
            this.handle = handle;
        }

        @Override
        public boolean hasNext() {
            return this.handle.hasNext();
        }

        @Override
        public int next() {
            return this.handle.nextInt();
        }

        @Override
        public void remove() {
            this.handle.remove();
        }

    }

    private static class LongIteratorWrapper implements LongIterator {

        private final it.unimi.dsi.fastutil.longs.LongIterator handle;

        LongIteratorWrapper(it.unimi.dsi.fastutil.longs.LongIterator handle) {
            this.handle = handle;
        }

        @Override
        public boolean hasNext() {
            return this.handle.hasNext();
        }

        @Override
        public long next() {
            return this.handle.nextLong();
        }

        @Override
        public void remove() {
            this.handle.remove();
        }

    }

    private static class CharIteratorWrapper implements CharIterator {

        private final it.unimi.dsi.fastutil.chars.CharIterator handle;

        CharIteratorWrapper(it.unimi.dsi.fastutil.chars.CharIterator handle) {
            this.handle = handle;
        }

        @Override
        public boolean hasNext() {
            return this.handle.hasNext();
        }

        @Override
        public char next() {
            return this.handle.nextChar();
        }

        @Override
        public void remove() {
            this.handle.remove();
        }

    }


}
