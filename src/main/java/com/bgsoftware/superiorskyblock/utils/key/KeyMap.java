package com.bgsoftware.superiorskyblock.utils.key;

import com.bgsoftware.superiorskyblock.api.key.Key;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class KeyMap<V> extends AbstractMap<Key, V> implements Map<Key, V> {

    private Map<String, V> map;

    public KeyMap(){
        this.map = new HashMap<>();
    }

    public KeyMap(KeyMap<V> other){
        this.map = new HashMap<>(other.map);
    }

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
        return get(o) != null;
    }

    public V put(String key, V value) {
        return put(Key.of(key), value);
    }

    @Override
    public V put(Key key, V value) {
        return map.put(key.toString(), value);
    }

    public Key getKey(Key key){
        String keyStr = key.toString();
        if(keyStr.contains(":") && map.containsKey(keyStr.split(":")[0]))
            keyStr = keyStr.split(":")[0];
        else if(keyStr.contains(";") && map.containsKey(keyStr.split(";")[0]))
            keyStr = keyStr.split(";")[0];
        return Key.of(keyStr);
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

    public V removeRaw(Key key){
        return map.remove(key.toString());
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

        return map.get(o.toString());
    }

    public V getRaw(Key key, V defaultValue){
        return map.getOrDefault(key.toString(), defaultValue);
    }

    @Override
    public String toString() {
        return map.toString();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return key instanceof Key ? containsKey(key) ? get(key) : defaultValue : map.getOrDefault(key, defaultValue);
    }

    @Override
    public void clear() {
        map.clear();
    }

    public Map<Key, V> asKeyMap(){
        Map<Key, V> map = new HashMap<>();
        this.map.forEach((key, value) -> map.put(Key.of(key), value));
        return map;
    }

}
