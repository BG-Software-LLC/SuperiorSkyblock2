package com.bgsoftware.superiorskyblock.utils.key;

import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class KeyMap<V> extends AbstractMap<com.bgsoftware.superiorskyblock.api.key.Key, V> implements Map<com.bgsoftware.superiorskyblock.api.key.Key, V> {

    private final Registry<String, V> registry;

    public KeyMap(){
        this.registry = Registry.createRegistry();
    }

    public KeyMap(KeyMap<V> other){
        this.registry = Registry.createRegistry(other.registry);
    }

    @Override
    public Set<Entry<com.bgsoftware.superiorskyblock.api.key.Key, V>> entrySet() {
        return asKeyMap().entrySet();
    }

    @Override
    public int size() {
        return registry.size();
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
        return registry.add(key.toString(), value);
    }

    public Key getKey(Key key){
        if(registry.containsKey(key.toString()))
            return key;
        else if(registry.containsKey(key.getGlobalKey()))
            return Key.of(key.getGlobalKey(), "");
        else
            return key;
    }

    @Override
    public V remove(Object key) {
        return registry.remove(key + "");
    }

    public boolean removeIf(Predicate<com.bgsoftware.superiorskyblock.api.key.Key> predicate){
        return registry.removeIf(str -> predicate.test(Key.of(str)));
    }

    public V get(ItemStack itemStack) {
        return get(com.bgsoftware.superiorskyblock.utils.key.Key.of(itemStack));
    }

    public V get(Material material, short data) {
        return get(com.bgsoftware.superiorskyblock.utils.key.Key.of(material, data));
    }

    public V get(String key) {
        return get(com.bgsoftware.superiorskyblock.utils.key.Key.of(key));
    }

    @Override
    public V get(Object obj) {
        if(obj instanceof Key){
            V returnValue = registry.get(obj.toString());
            return returnValue == null && !((Key) obj).getSubKey().isEmpty() ? registry.get(((Key) obj).getGlobalKey()) : returnValue;
        }

        return null;
    }

    public V getRaw(com.bgsoftware.superiorskyblock.api.key.Key key, V defaultValue){
        return getRaw((Key) key, defaultValue);
    }

    public V getRaw(Key key, V defaultValue){
        V returnValue = registry.get(key.toString());
        return returnValue == null ? defaultValue : returnValue;
    }

    @Override
    public String toString() {
        return registry.toString();
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
        registry.clear();
    }

    public Map<com.bgsoftware.superiorskyblock.api.key.Key, V> asKeyMap(){
        return registry.toMap().entrySet().stream().collect(Collectors.toMap(entry -> Key.of(entry.getKey()), Entry::getValue));
    }

    public Map<String, V> asMap(){
        return new HashMap<>(registry.toMap());
    }

}
