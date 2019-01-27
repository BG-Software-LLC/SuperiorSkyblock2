package com.ome_r.superiorskyblock.utils.key;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class KeyMap<V> extends AbstractMap<Key, V> implements Map<Key, V> {

    private Map<String, V> map;

    public KeyMap(){
        this.map = new HashMap<>();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Set<Entry<Key, V>> entrySet() {
        return asKeyMap().entrySet();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean containsKey(Object o) {
        if(o instanceof Key) {
            String key = o.toString();
            if(map.containsKey(key))
                return true;
            else if(key.contains(":") && map.containsKey(key.split(":")[0]))
                return true;
            else if(key.contains(";") && map.containsKey(key.split(";")[0]))
                return true;
        }
        return super.containsKey(o);
    }

    public V put(String key, V value) {
        return put(Key.of(key), value);
    }

    @Override
    public V put(Key key, V value) {
        return map.put(key.toString(), value);
    }

    @Override
    public V remove(Object key) {
        if(key instanceof Key) {
            String keyStr = key.toString();
            map.remove(keyStr);
            map.remove(keyStr.split(":")[0]);
            map.remove(keyStr.split(";")[0]);
        }
        return map.remove(key);
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
    public V get(Object o) {
        if(map.containsKey("all"))
            return map.get("all");
        if(o instanceof Key) {
            String key = o.toString();
            if(map.containsKey(key))
                return map.get(key);
            else if(key.contains(":") && map.containsKey(key.split(":")[0]))
                return map.get(key.split(":")[0]);
            else if(key.contains(";") && map.containsKey(key.split(";")[0]))
                return map.get(key.split(";")[0]);
        }
        return super.get(o);
    }

    @Override
    public String toString() {
        return map.toString();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return key instanceof Key ? containsKey(key) ? get(key) : defaultValue : super.getOrDefault(key, defaultValue);
    }

    @Override
    public void clear() {
        map.clear();
    }

    private Map<Key, V> asKeyMap(){
        Map<Key, V> map = new HashMap<>();
        this.map.forEach((key, value) -> map.put(Key.of(key), value));
        return map;
    }

}
