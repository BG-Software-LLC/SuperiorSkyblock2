package com.bgsoftware.superiorskyblock.utils.registry;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public abstract class Registry<K, V> implements Iterable<V> {

    protected static final Set<Registry<?, ?>> loadedRegisteries = Collections.newSetFromMap(new WeakHashMap<>());

    private final Map<K ,V> registry;

    protected Registry(){
        this(Maps.newHashMap());
    }

    protected Registry(Map<K, V> map){
        this.registry = map;
        loadedRegisteries.add(this);
    }

    protected Registry(Registry<K, V> other){
        this.registry = Maps.newHashMap(other.registry);
        loadedRegisteries.add(this);
    }

    public V get(K key){
        synchronized (registry){
            return registry.get(key);
        }
    }

    public V get(K key, V def){
        synchronized (registry){
            return registry.getOrDefault(key, def);
        }
    }

    public V add(K key, V value){
        synchronized (registry){
            return registry.put(key, value);
        }
    }

    public V remove(K key){
        synchronized (registry){
            return registry.remove(key);
        }
    }

    public boolean containsKey(K key){
        synchronized (registry){
            return registry.containsKey(key);
        }
    }

    public Collection<V> values(){
        synchronized (registry){
            return registry.values();
        }
    }

    public Collection<K> keys(){
        synchronized (registry){
            return registry.keySet();
        }
    }

    public Collection<Map.Entry<K, V>> entries(){
        synchronized (registry){
            return registry.entrySet();
        }
    }

    public void clear(){
        synchronized (registry){
            registry.clear();
        }
    }

    public void delete(){
        clear();
        loadedRegisteries.remove(this);
    }

    @Override
    protected void finalize(){
        delete();
    }

    public Map<K, V> toMap(){
        synchronized (registry) {
            return Maps.newHashMap(registry);
        }
    }

    public int size(){
        return registry.size();
    }

    public boolean isEmpty(){
        return size() == 0;
    }

    @Override
    public Iterator<V> iterator() {
        synchronized (registry){
            return registry.values().iterator();
        }
    }

    @Override
    public String toString() {
        synchronized (registry){
            return registry.toString();
        }
    }

    public static void clearCache(){
        loadedRegisteries.forEach(Registry::clear);
        loadedRegisteries.clear();
    }

    public static <K, V> Registry<K, V> createRegistry(){
        return new Registry<K, V>() {};
    }

    public static <K, V> Registry<K, V> createRegistry(Map<K, V> defaults){
        return new Registry<K, V>(defaults) {};
    }

    public static <K, V> Registry<K, V> createRegistry(Registry<K, V> other){
        return new Registry<K, V>(other) {};
    }

    public static <K, V> Registry<K, V> createLinkedRegistry(){
        return createRegistry(Maps.newLinkedHashMap());
    }

}
