package com.bgsoftware.superiorskyblock.key.dataset;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.key.KeyImpl;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class KeyMapImpl<V> extends AbstractMap<Key, V> implements KeyMap<V> {

    private final Map<String, V> innerMap;

    public static <V> KeyMapImpl<V> create(Supplier<Map<String, V>> mapCreator) {
        return new KeyMapImpl<>(mapCreator);
    }

    public static <V> KeyMapImpl<V> create(Supplier<Map<String, V>> mapCreator, Map<Key, V> keys) {
        return new KeyMapImpl<>(mapCreator, keys.entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey().toString(),
                Entry::getValue
        )));
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
        return asMap().entrySet();
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
            return Key.of(key.getGlobalKey(), "");
        else
            return def;
    }

    @Override
    public boolean removeIf(Predicate<Key> predicate) {
        return innerMap.keySet().removeIf(str -> predicate.test(Key.of(str)));
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
        return innerMap.entrySet().stream().collect(Collectors.toMap(entry -> Key.of(entry.getKey()), Entry::getValue, (v1, v2) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", v1));
        }, HashMap::new));
    }

}
