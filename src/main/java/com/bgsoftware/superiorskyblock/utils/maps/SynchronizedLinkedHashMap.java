package com.bgsoftware.superiorskyblock.utils.maps;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class SynchronizedLinkedHashMap<K, V> implements Map<K, V> {

    private final LinkedHashMap<K, V> m = new LinkedHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public int size() {
        try {
            lock.readLock().lock();
            return m.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        try {
            lock.readLock().lock();
            return m.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean containsValue(Object value) {
        try {
            lock.readLock().lock();
            return m.containsValue(value);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public V get(Object key) {
        try {
            lock.readLock().lock();
            return m.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        try {
            lock.writeLock().lock();
            return m.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public V remove(Object key) {
        try {
            lock.writeLock().lock();
            return m.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        try {
            lock.writeLock().lock();
            this.m.putAll(m);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        try {
            lock.writeLock().lock();
            m.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Set<K> keySet() {
        try {
            lock.readLock().lock();
            return Collections.unmodifiableSet(m.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Collection<V> values() {
        try {
            lock.readLock().lock();
            return Collections.unmodifiableCollection(m.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        try {
            lock.readLock().lock();
            return Collections.unmodifiableSet(m.entrySet());
        } finally {
            lock.readLock().unlock();
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        try {
            lock.readLock().lock();
            return m.equals(o);
        } finally {
            lock.readLock().unlock();
        }
    }

    public int hashCode() {
        try {
            lock.readLock().lock();
            return m.hashCode();
        } finally {
            lock.readLock().unlock();
        }
    }

    public String toString() {
        try {
            lock.readLock().lock();
            return m.toString();
        } finally {
            lock.readLock().unlock();
        }
    }

    // Override default methods in Map
    @Override
    public V getOrDefault(Object k, V defaultValue) {
        try {
            lock.readLock().lock();
            return m.getOrDefault(k, defaultValue);
        } finally {
            lock.readLock().unlock();
        }
    }
    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        try {
            lock.readLock().lock();
            m.forEach(action);
        } finally {
            lock.readLock().unlock();
        }
    }
    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        try {
            lock.writeLock().lock();
            m.replaceAll(function);
        } finally {
            lock.writeLock().unlock();
        }
    }
    @Override
    public V putIfAbsent(K key, V value) {
        try {
            lock.writeLock().lock();
            return m.putIfAbsent(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }
    @Override
    public boolean remove(Object key, Object value) {
        try {
            lock.writeLock().lock();
            return m.remove(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }
    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        try {
            lock.writeLock().lock();
            return m.replace(key, oldValue, newValue);
        } finally {
            lock.writeLock().unlock();
        }
    }
    @Override
    public V replace(K key, V value) {
        try {
            lock.writeLock().lock();
            return m.replace(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }
    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        try {
            lock.writeLock().lock();
            return m.computeIfAbsent(key, mappingFunction);
        } finally {
            lock.writeLock().unlock();
        }
    }
    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        try {
            lock.writeLock().lock();
            return m.computeIfPresent(key, remappingFunction);
        } finally {
            lock.writeLock().unlock();
        }
    }
    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        try {
            lock.writeLock().lock();
            return m.compute(key, remappingFunction);
        } finally {
            lock.writeLock().unlock();
        }
    }
    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        try {
            lock.writeLock().lock();
            return m.merge(key, value, remappingFunction);
        } finally {
            lock.writeLock().unlock();
        }
    }

}
