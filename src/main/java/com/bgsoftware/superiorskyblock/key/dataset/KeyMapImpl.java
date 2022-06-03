package com.bgsoftware.superiorskyblock.key.dataset;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.key.KeyImpl;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class KeyMapImpl<V> extends AbstractMap<Key, V> implements KeyMap<V> {

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final KeyMapImpl EMPTY_MAP = new KeyMapImpl(() -> Collections.emptyMap());

    private final Map<String, V> innerMap;

    private Set<Entry<Key, V>> entrySet;

    public static <V> KeyMapImpl<V> create(Supplier<Map<String, V>> mapCreator) {
        return new KeyMapImpl<>(mapCreator);
    }

    public static <V> KeyMapImpl<V> create(Supplier<Map<String, V>> mapCreator, Map<Key, V> keys) {
        return new KeyMapImpl<>(mapCreator, keys.entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey().toString(),
                Entry::getValue
        )));
    }

    public static <V> KeyMapImpl<V> createHashMap() {
        return create(() -> new HashMap<>());
    }

    public static <V> KeyMapImpl<V> createConcurrentHashMap() {
        return create(() -> new ConcurrentHashMap<>());
    }

    public static <V> KeyMapImpl<V> createConcurrentHashMap(Map<Key, V> keys) {
        return create(() -> new ConcurrentHashMap<>(), keys);
    }

    public static <V> KeyMapImpl<V> createEmptyMap() {
        return (KeyMapImpl<V>) EMPTY_MAP;
    }

    private KeyMapImpl(Supplier<Map<String, V>> mapCreator, Map<String, V> other) {
        this(mapCreator);
        this.innerMap.putAll(other);
    }

    private KeyMapImpl(Supplier<Map<String, V>> mapCreator) {
        this.innerMap = mapCreator.get();
    }

    @Override
    public int size() {
        return innerMap.size();
    }

    @Override
    public boolean containsKey(Object o) {
        return get(o) != null;
    }

    @Override
    public V get(Object obj) {
        if (obj instanceof KeyImpl) {
            V returnValue = innerMap.get(obj.toString());
            return returnValue == null && !((KeyImpl) obj).getSubKey().isEmpty() ? innerMap.get(((KeyImpl) obj).getGlobalKey()) : returnValue;
        }

        return null;
    }

    @Override
    public V put(Key key, V value) {
        return innerMap.put(key.toString(), value);
    }

    @Override
    public V remove(Object key) {
        return innerMap.remove(key + "");
    }

    @Override
    public void clear() {
        innerMap.clear();
    }

    @Override
    @NotNull
    public Set<Entry<Key, V>> entrySet() {
        return entrySet == null ? (entrySet = new EntrySet()) : entrySet;
    }

    @Override
    public String toString() {
        return innerMap.toString();
    }

    @Override
    public Key getKey(Key key) {
        return getKey(key, null);
    }

    @Override
    public Key getKey(Key key, Key def) {
        if (innerMap.containsKey(key.toString()))
            return key;
        else if (innerMap.containsKey(key.getGlobalKey()))
            return KeyImpl.of(key.getGlobalKey(), "");
        else
            return def;
    }

    @Override
    public boolean removeIf(Predicate<Key> predicate) {
        return innerMap.keySet().removeIf(str -> predicate.test(KeyImpl.of(str)));
    }

    @Override
    public V getRaw(Key key, V defaultValue) {
        V returnValue = innerMap.get(key.toString());
        return returnValue == null ? defaultValue : returnValue;
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        V value = get(key);
        return value == null ? defaultValue : value;
    }

    @Override
    public Map<Key, V> asMap() {
        return innerMap.entrySet().stream().collect(Collectors.toMap(entry -> KeyImpl.of(entry.getKey()), Entry::getValue, (v1, v2) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", v1));
        }, HashMap::new));
    }

    private final class EntrySet extends AbstractSet<Entry<Key, V>> {
        public int size() {
            return KeyMapImpl.this.size();
        }

        public void clear() {
            KeyMapImpl.this.clear();
        }

        public @NotNull Iterator<Entry<Key, V>> iterator() {
            return new EntryIterator();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            Object key = e.getKey();
            V mapValue = KeyMapImpl.this.get(key);
            return mapValue != null && mapValue.equals(e.getValue());
        }

        public boolean remove(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                Object key = e.getKey();
                Object value = e.getValue();
                return KeyMapImpl.this.remove(key, value);
            }
            return false;
        }

        public Spliterator<Entry<Key, V>> spliterator() {
            return new EntrySpliterator();
        }

        public void forEach(Consumer<? super Entry<Key, V>> action) {
            iterator().forEachRemaining(action);
        }
    }

    private final class EntryIterator implements Iterator<Entry<Key, V>> {

        final Iterator<Entry<String, V>> innerMapIterator = KeyMapImpl.this.innerMap.entrySet().iterator();

        @Override
        public boolean hasNext() {
            return innerMapIterator.hasNext();
        }

        @Override
        public Entry<Key, V> next() {
            return new KeyEntry(innerMapIterator.next());
        }

        @Override
        public void remove() {
            innerMapIterator.remove();
        }
    }

    private final class EntrySpliterator implements Spliterator<Entry<Key, V>> {

        final Spliterator<Entry<String, V>> spliterator;

        EntrySpliterator() {
            this(KeyMapImpl.this.innerMap.entrySet().spliterator());
        }

        EntrySpliterator(Spliterator<Entry<String, V>> spliterator) {
            this.spliterator = spliterator;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Entry<Key, V>> action) {
            return spliterator.tryAdvance(entry -> action.accept(new KeyEntry(entry)));
        }

        @Override
        public Spliterator<Entry<Key, V>> trySplit() {
            return new EntrySpliterator(spliterator.trySplit());
        }

        @Override
        public long estimateSize() {
            return spliterator.estimateSize();
        }

        @Override
        public int characteristics() {
            return spliterator.characteristics();
        }
    }

    private final class KeyEntry implements Entry<Key, V> {

        private final Entry<String, V> entry;

        KeyEntry(Entry<String, V> entry) {
            this.entry = entry;
        }

        @Override
        public Key getKey() {
            return Key.of(entry.getKey());
        }

        @Override
        public V getValue() {
            return entry.getValue();
        }

        @Override
        public V setValue(V value) {
            return entry.setValue(value);
        }

    }

}
