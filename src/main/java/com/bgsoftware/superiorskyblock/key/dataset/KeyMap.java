package com.bgsoftware.superiorskyblock.key.dataset;

import com.bgsoftware.superiorskyblock.key.Key;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class KeyMap<V> extends AbstractMap<com.bgsoftware.superiorskyblock.api.key.Key, V> implements Map<com.bgsoftware.superiorskyblock.api.key.Key, V> {

    private final Map<String, V> innerMap = new ConcurrentHashMap<>();

    public KeyMap(){
    }

    public KeyMap(KeyMap<V> other){
        this.innerMap.putAll(other.innerMap);
    }

    @Override
    public Set<Entry<com.bgsoftware.superiorskyblock.api.key.Key, V>> entrySet() {
        return asKeyMap().entrySet();
    }

    @Override
    public int size() {
        return innerMap.size();
    }

    public boolean containsKey(com.bgsoftware.superiorskyblock.api.key.Key key) {
        return containsKey((Object) key);
    }

    @Override
    public boolean containsKey(Object o) {
        return get(o) != null;
    }

    public V put(String key, V value) {
        return put(Key.of(key), value);
    }

    public V put(String globalKey, String subKey, V value) {
        return put(Key.of(globalKey, subKey), value);
    }

    @Override
    public V put(com.bgsoftware.superiorskyblock.api.key.Key key, V value) {
        return innerMap.put(key.toString(), value);
    }

    public Key getKey(Key key){
        return getKey(key, key);
    }

    public Key getKey(Key key, Key def){
        if(innerMap.containsKey(key.toString()))
            return key;
        else if(innerMap.containsKey(key.getGlobalKey()))
            return Key.of(key.getGlobalKey(), "");
        else
            return def;
    }

    @Override
    public V remove(Object key) {
        return innerMap.remove(key + "");
    }

    public boolean removeIf(Predicate<com.bgsoftware.superiorskyblock.api.key.Key> predicate){
        return innerMap.keySet().removeIf(str -> predicate.test(Key.of(str)));
    }

    public V get(ItemStack itemStack) {
        return get(Key.of(itemStack));
    }

    public V get(Material material, short data) {
        return get(Key.of(material, data));
    }

    public V get(String key) {
        return get(Key.of(key));
    }

    @Override
    public V get(Object obj) {
        if(obj instanceof Key){
            V returnValue = innerMap.get(obj.toString());
            return returnValue == null && !((Key) obj).getSubKey().isEmpty() ? innerMap.get(((Key) obj).getGlobalKey()) : returnValue;
        }

        return null;
    }

    public V getRaw(com.bgsoftware.superiorskyblock.api.key.Key key, V defaultValue){
        return getRaw((Key) key, defaultValue);
    }

    public V getRaw(Key key, V defaultValue){
        V returnValue = innerMap.get(key.toString());
        return returnValue == null ? defaultValue : returnValue;
    }

    @Override
    public String toString() {
        return innerMap.toString();
    }

    public V getOrDefault(com.bgsoftware.superiorskyblock.api.key.Key key, V defaultValue) {
        return getOrDefault((Object) key, defaultValue);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        V value = get(key);
        return value == null? defaultValue : value;
    }

    @Override
    public void clear() {
        innerMap.clear();
    }

    public Map<com.bgsoftware.superiorskyblock.api.key.Key, V> asKeyMap(){
        return innerMap.entrySet().stream().collect(Collectors.toMap(entry -> Key.of(entry.getKey()), Entry::getValue));
    }

    public static <T, U> Collector<T, ?, KeyMap<U>> getCollector(Function<? super T, ? extends com.bgsoftware.superiorskyblock.api.key.Key> keyMapper,
                                                                      Function<? super T, ? extends U> valueMapper){
        return Collectors.toMap(keyMapper, valueMapper,
                (u, u2) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); }, KeyMap::new);
    }

}
