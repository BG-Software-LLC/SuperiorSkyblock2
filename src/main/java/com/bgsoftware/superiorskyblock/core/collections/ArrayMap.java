package com.bgsoftware.superiorskyblock.core.collections;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

public class ArrayMap<K, V> implements Map<K, V> {

    private static final Object[] EMPTY_ARRAY = new Object[0];

    private static final int DEFAULT_CAPACITY = 4;

    private KeySet keySet;
    private Values valuesCollection;
    private EntrySet entrySet;

    private Object[] keys = EMPTY_ARRAY;
    private Object[] values = EMPTY_ARRAY;
    private int size = 0;
    private int capacity = 0;

    private int findKeyPos(Object key) {
        for (int i = 0; i < this.size; ++i) {
            if (Objects.equals(keys[i], key))
                return i;
        }

        return -1;
    }

    private void ensureCapacity() {
        if (this.capacity == 0) {
            this.capacity = DEFAULT_CAPACITY;
            this.keys = new Object[DEFAULT_CAPACITY];
            this.values = new Object[DEFAULT_CAPACITY];
        } else if (this.size >= this.capacity) {
            this.capacity *= 2;
            this.keys = Arrays.copyOf(this.keys, this.capacity);
            this.values = Arrays.copyOf(this.values, this.capacity);
        }
    }

    @Override
    public V get(Object key) {
        int pos = findKeyPos(key);
        return pos < 0 ? null : (V) this.values[pos];
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void clear() {
        this.values = EMPTY_ARRAY;
        this.keys = EMPTY_ARRAY;
        this.size = 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return findKeyPos(key) >= 0;
    }

    @Override
    public boolean containsValue(Object value) {
        for (Object curr : this.values) {
            if (Objects.equals(value, curr))
                return true;
        }

        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public V put(Object key, Object value) {
        int pos = findKeyPos(key);
        if (pos >= 0) {
            Object old = this.values[pos];
            this.values[pos] = value;
            return (V) old;
        }

        ensureCapacity();

        this.keys[this.size] = key;
        this.values[this.size] = value;
        ++this.size;

        return null;
    }

    @Override
    public V remove(Object key) {
        int pos = findKeyPos(key);
        if (pos < 0) {
            return null;
        }

        return remove(pos);
    }

    private V remove(int pos) {
        Object oldValue = this.values[pos];
        int tail = this.size - pos - 1;
        System.arraycopy(this.keys, pos + 1, this.keys, pos, tail);
        System.arraycopy(this.values, pos + 1, this.values, pos, tail);
        --this.size;
        this.values[this.size] = null;
        return (V) oldValue;
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        for (Entry<? extends K, ? extends V> curr : map.entrySet()) {
            put(curr.getKey(), curr.getValue());
        }
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return this.keySet == null ? (this.keySet = new KeySet()) : this.keySet;
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return this.valuesCollection == null ? (this.valuesCollection = new Values()) : this.valuesCollection;
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.entrySet == null ? (this.entrySet = new EntrySet()) : this.entrySet;
    }

    private class KeySet extends AbstractSet<K> {

        @Override
        public int size() {
            return ArrayMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return ArrayMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return ArrayMap.this.containsKey(o);
        }

        @NotNull
        @Override
        public Iterator<K> iterator() {
            return new KeySetItr();
        }

        @NotNull
        @Override
        public Object[] toArray() {
            return Arrays.copyOf(ArrayMap.this.keys, size());
        }

        @NotNull
        @Override
        public <T> T[] toArray(@NotNull T[] ts) {
            Object[] arr = ts.length >= size() ? ts : Arrays.copyOf(ts, size());
            System.arraycopy(ArrayMap.this.keys, 0, arr, 0, size());
            return (T[]) arr;
        }

        @Override
        public void clear() {
            ArrayMap.this.clear();
        }
    }

    private class KeySetItr extends Itr<K> {

        @Override
        protected K getNext() {
            return (K) ArrayMap.this.keys[this.lastRet];
        }

    }

    private class Values extends AbstractCollection<V> {

        @Override
        public int size() {
            return ArrayMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return ArrayMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return ArrayMap.this.containsValue(o);
        }

        @NotNull
        @Override
        public Iterator<V> iterator() {
            return new ValuesItr();
        }

        @NotNull
        @Override
        public Object[] toArray() {
            return Arrays.copyOf(ArrayMap.this.values, size());
        }

        @NotNull
        @Override
        public <T> T[] toArray(@NotNull T[] ts) {
            Object[] arr = ts.length >= size() ? ts : Arrays.copyOf(ts, size());
            System.arraycopy(ArrayMap.this.values, 0, arr, 0, size());
            return (T[]) arr;
        }

        @Override
        public void clear() {
            ArrayMap.this.clear();
        }
    }

    private class ValuesItr extends Itr<V> {

        @Override
        protected V getNext() {
            return (V) ArrayMap.this.values[this.lastRet];
        }

    }

    private class EntrySet extends AbstractSet<Entry<K, V>> {

        @Override
        public int size() {
            return ArrayMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return ArrayMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return ArrayMap.this.containsKey(o);
        }

        @NotNull
        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new EntrySetItr();
        }

        @Override
        public void clear() {
            ArrayMap.this.clear();
        }

    }

    private class EntrySetItr extends Itr<Entry<K, V>> {

        @Override
        protected Entry<K, V> getNext() {
            return new EntryImpl(this.lastRet);
        }

    }

    private class EntryImpl implements Entry<K, V> {

        private final int pos;

        EntryImpl(int pos) {
            this.pos = pos;
        }

        @Override
        public K getKey() {
            return (K) ArrayMap.this.keys[this.pos];
        }

        @Override
        public V getValue() {
            return (V) ArrayMap.this.values[this.pos];
        }

        @Override
        public V setValue(V v) {
            Object old = ArrayMap.this.values[this.pos];
            ArrayMap.this.values[this.pos] = v;
            return (V) old;
        }
    }

    private abstract class Itr<T> implements Iterator<T> {

        protected int pos = 0;
        protected int lastRet = -1;

        @Override
        public final boolean hasNext() {
            return this.pos < size();
        }

        @Override
        public final T next() {
            if (!hasNext())
                throw new NoSuchElementException();
            this.lastRet = this.pos++;
            return getNext();
        }

        protected abstract T getNext();

        @Override
        public final void remove() {
            if (this.lastRet < 0)
                throw new IllegalStateException();
            ArrayMap.this.remove(this.lastRet);
            this.pos = this.lastRet;
            this.lastRet = -1;
        }
    }

}
