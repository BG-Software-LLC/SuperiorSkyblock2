package com.bgsoftware.superiorskyblock.utils.registry;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public abstract class Registry<K, V> implements Iterable<V> {

    protected static final Set<Registry<?, ?>> loadedRegisteries = Sets.newHashSet();

    private final Map<K ,V> registry = Maps.newHashMap();

    protected Registry(){
        loadedRegisteries.add(this);
    }

    public synchronized V get(K key){
        return registry.get(key);
    }

    public synchronized void add(K key, V value){
        registry.put(key, value);
    }

    public synchronized V remove(K key){
        return registry.remove(key);
    }

    public synchronized boolean containsKey(K key){
        return registry.containsKey(key);
    }

    public synchronized Collection<V> values(){
        return registry.values();
    }

    public synchronized void clear(){
        registry.clear();
    }

    public int size(){
        return registry.size();
    }

    @Override
    public synchronized Iterator<V> iterator() {
        return registry.values().iterator();
    }

    public static void clearCache(){
        loadedRegisteries.forEach(Registry::clear);
        loadedRegisteries.clear();
    }

}
