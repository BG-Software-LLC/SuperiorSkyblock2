package com.bgsoftware.superiorskyblock.utils.key;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class KeyMap<V> extends AbstractMap<Key, V> implements Map<Key, V> {

    private final Registry<String, V> registry;

    public KeyMap(){
        this.registry = Registry.createRegistry();
    }

    public KeyMap(KeyMap<V> other){
        this.registry = Registry.createRegistry(other.registry);
    }

    @Override
    public Set<Entry<Key, V>> entrySet() {
        return asKeyMap().entrySet();
    }

    @Override
    public int size() {
        return registry.size();
    }

    @Override
    public boolean containsKey(Object o) {
        return get(o) != null;
    }

    public V put(String key, V value) {
        return put(Key.of(key), value);
    }

    @Override
    public V put(Key key, V value) {
        return registry.add(key.toString(), value);
    }

    public Key getKey(Key key){
        V returnValue = registry.get(key.getGlobalKey());
        return returnValue == null && !key.getSubKey().isEmpty() ? Key.of(key.getGlobalKey()) : key;
    }

    @Override
    public V remove(Object key) {
        return registry.remove(key + "");
    }

    public void removeRaw(Key key){
        remove(key);
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
            V returnValue = registry.get(obj.toString());
            return returnValue == null && !((Key) obj).getSubKey().isEmpty() ? registry.get(((Key) obj).getGlobalKey()) : returnValue;
        }

        return null;
    }

    public V getRaw(Key key, V defaultValue){
        V returnValue = registry.get(key.toString());
        return returnValue == null ? defaultValue : returnValue;
    }

    @Override
    public String toString() {
        return registry.toString();
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

    public Map<Key, V> asKeyMap(){
        return registry.toMap().entrySet().stream().collect(Collectors.toMap(entry -> Key.of(entry.getKey()), Entry::getValue));
    }

}
