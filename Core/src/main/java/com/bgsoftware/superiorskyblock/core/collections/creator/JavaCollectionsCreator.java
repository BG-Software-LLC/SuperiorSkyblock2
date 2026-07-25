package com.bgsoftware.superiorskyblock.core.collections.creator;

import com.bgsoftware.superiorskyblock.core.collections.ArrayMap;
import com.bgsoftware.superiorskyblock.core.collections.view.Char2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.collections.view.CharIterator;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2IntMapView;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.collections.view.IntIterator;
import com.bgsoftware.superiorskyblock.core.collections.view.Long2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.collections.view.LongIterator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;

public class JavaCollectionsCreator implements CollectionsCreator {

    public static final JavaCollectionsCreator INSTANCE = new JavaCollectionsCreator();

    private JavaCollectionsCreator() {

    }

    @Override
    public <V> Int2ObjectMapView<V> createInt2ObjectLinkedHashMap() {
        return new Int2ObjectJavaMapView<>(new LinkedHashMap<>());
    }

    @Override
    public <V> Int2ObjectMapView<V> createInt2ObjectArrayMap() {
        return new Int2ObjectJavaMapView<>(new ArrayMap<>());
    }

    @Override
    public Int2IntMapView createInt2IntHashMap() {
        return new Int2IntJavaMapView(new HashMap<>());
    }

    @Override
    public Int2IntMapView createInt2IntArrayMap() {
        return new Int2IntJavaMapView(new ArrayMap<>());
    }

    @Override
    public <V> Long2ObjectMapView<V> createLong2ObjectHashMap() {
        return new Long2ObjectJavaMapView<>(new HashMap<>());
    }

    @Override
    public <V> Long2ObjectMapView<V> createLong2ObjectLinkedHashMap() {
        return new Long2ObjectJavaMapView<>(new LinkedHashMap<>());
    }

    @Override
    public <V> Long2ObjectMapView<V> createLong2ObjectArrayMap() {
        return new Long2ObjectJavaMapView<>(new ArrayMap<>());
    }

    @Override
    public <V> Char2ObjectMapView<V> createChar2ObjectArrayMap() {
        return new Char2ObjectJavaMapView<>(new ArrayMap<>());
    }

    private static class Int2ObjectJavaMapView<V> implements Int2ObjectMapView<V> {

        private final Map<Integer, V> delegate;

        Int2ObjectJavaMapView(Map<Integer, V> delegate) {
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
            return new EntryIteratorWrapper<>(this.delegate.entrySet().iterator());
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

            private final Iterator<Map.Entry<Integer, V>> handle;

            EntryIteratorWrapper(Iterator<Map.Entry<Integer, V>> handle) {
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

                private final Map.Entry<Integer, V> handle;

                EntryWrapper(Map.Entry<Integer, V> handle) {
                    this.handle = handle;
                }

                @Override
                public int getKey() {
                    return this.handle.getKey();
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

    private static class Int2IntJavaMapView implements Int2IntMapView {

        private final Map<Integer, Integer> delegate;

        Int2IntJavaMapView(Map<Integer, Integer> delegate) {
            this.delegate = delegate;
        }

        @Override
        public OptionalInt put(int key, int value) {
            Integer old = this.delegate.put(key, value);
            return old == null ? OptionalInt.empty() : OptionalInt.of(old);
        }

        @Override
        public OptionalInt get(int key) {
            Integer old = this.delegate.get(key);
            return old == null ? OptionalInt.empty() : OptionalInt.of(old);
        }

        @Override
        public OptionalInt remove(int key) {
            Integer old = this.delegate.remove(key);
            return old == null ? OptionalInt.empty() : OptionalInt.of(old);
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
            return new EntryIteratorWrapper(this.delegate.entrySet().iterator());
        }

        @Override
        public IntIterator valueIterator() {
            return new IntIteratorWrapper(this.delegate.values().iterator());
        }

        @Override
        public IntIterator keyIterator() {
            return new IntIteratorWrapper(this.delegate.values().iterator());
        }

        @Override
        public Map<Integer, Integer> asMap() {
            return this.delegate;
        }

        private static class EntryIteratorWrapper implements Iterator<Int2IntMapView.Entry> {

            private final Iterator<Map.Entry<Integer, Integer>> handle;

            EntryIteratorWrapper(Iterator<Map.Entry<Integer, Integer>> handle) {
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

                private final Map.Entry<Integer, Integer> handle;

                EntryWrapper(Map.Entry<Integer, Integer> handle) {
                    this.handle = handle;
                }

                @Override
                public int getKey() {
                    return this.handle.getKey();
                }

                @Override
                public int getValue() {
                    return this.handle.getValue();
                }

                @Override
                public int setValue(int newValue) {
                    return this.handle.setValue(newValue);
                }
            }

        }


    }

    private static class Long2ObjectJavaMapView<V> implements Long2ObjectMapView<V> {

        private final Map<Long, V> delegate;

        Long2ObjectJavaMapView(Map<Long, V> delegate) {
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
            return new EntryIteratorWrapper<>(this.delegate.entrySet().iterator());
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

            private final Iterator<Map.Entry<Long, V>> handle;

            EntryIteratorWrapper(Iterator<Map.Entry<Long, V>> handle) {
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

                private final Map.Entry<Long, V> handle;

                EntryWrapper(Map.Entry<Long, V> handle) {
                    this.handle = handle;
                }

                @Override
                public long getKey() {
                    return this.handle.getKey();
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

    private static class Char2ObjectJavaMapView<V> implements Char2ObjectMapView<V> {

        private final Map<Character, V> delegate;

        Char2ObjectJavaMapView(Map<Character, V> delegate) {
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
            return new EntryIteratorWrapper<>(this.delegate.entrySet().iterator());
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

            private final Iterator<Map.Entry<Character, V>> handle;

            EntryIteratorWrapper(Iterator<Map.Entry<Character, V>> handle) {
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

                private final Map.Entry<Character, V> handle;

                EntryWrapper(Map.Entry<Character, V> handle) {
                    this.handle = handle;
                }

                @Override
                public char getKey() {
                    return this.handle.getKey();
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

        private final Iterator<Integer> handle;

        IntIteratorWrapper(Iterator<Integer> handle) {
            this.handle = handle;
        }

        @Override
        public boolean hasNext() {
            return this.handle.hasNext();
        }

        @Override
        public int next() {
            return this.handle.next();
        }

        @Override
        public void remove() {
            this.handle.remove();
        }

    }

    private static class LongIteratorWrapper implements LongIterator {

        private final Iterator<Long> handle;

        LongIteratorWrapper(Iterator<Long> handle) {
            this.handle = handle;
        }

        @Override
        public boolean hasNext() {
            return this.handle.hasNext();
        }

        @Override
        public long next() {
            return this.handle.next();
        }

        @Override
        public void remove() {
            this.handle.remove();
        }

    }

    private static class CharIteratorWrapper implements CharIterator {

        private final Iterator<Character> handle;

        CharIteratorWrapper(Iterator<Character> handle) {
            this.handle = handle;
        }

        @Override
        public boolean hasNext() {
            return this.handle.hasNext();
        }

        @Override
        public char next() {
            return this.handle.next();
        }

        @Override
        public void remove() {
            this.handle.remove();
        }

    }

}
