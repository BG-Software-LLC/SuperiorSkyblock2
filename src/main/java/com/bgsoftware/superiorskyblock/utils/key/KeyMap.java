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

    private Registry<String, V> registry;

    public KeyMap(){
        this.registry = Registry.createRegistry();
    }

    public KeyMap(KeyMap<V> other){
        this.registry = Registry.createRegistry(other.registry.toMap());
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
        String keyStr = key.toString();
        if(keyStr.contains(":") && registry.containsKey(keyStr.split(":")[0]))
            keyStr = keyStr.split(":")[0];
        else if(keyStr.contains(";") && registry.containsKey(keyStr.split(";")[0]))
            keyStr = keyStr.split(";")[0];
        return Key.of(keyStr);
    }

    @Override
    public V remove(Object key) {
        if(key instanceof Key) {
            String keyStr = key.toString();
            registry.remove(keyStr);
            registry.remove(keyStr.split(":")[0]);
            registry.remove(keyStr.split(";")[0]);
        }
        return registry.remove(key + "");
    }

    public void removeRaw(Key key){
        registry.remove(key.toString());
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
        if(registry.containsKey("all"))
            return registry.get("all");

        if(o instanceof Key) {
            String key = o.toString();
            if(registry.containsKey(key))
                return registry.get(key);
            else if(key.contains(":") && registry.containsKey(key.split(":")[0]))
                return registry.get(key.split(":")[0]);
            else if(key.contains(";") && registry.containsKey(key.split(";")[0]))
                return registry.get(key.split(";")[0]);
        }

        return registry.get(o.toString());
    }

    public V getRaw(Key key, V defaultValue){
        return registry.get(key.toString(), defaultValue);
    }

    @Override
    public String toString() {
        return registry.toString();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return key instanceof Key ? containsKey(key) ? get(key) : defaultValue : registry.get(key + "", defaultValue);
    }

    @Override
    public void clear() {
        registry.clear();
    }

    public Map<Key, V> asKeyMap(){
        return registry.toMap().entrySet().stream().collect(Collectors.toMap(entry -> Key.of(entry.getKey()), Entry::getValue));
    }

}
