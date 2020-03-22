package com.bgsoftware.superiorskyblock.utils.registry;

import com.bgsoftware.superiorskyblock.utils.maps.SynchronizedLinkedHashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public abstract class Registry<K, V> implements Iterable<V> {

    protected static final Set<Registry<?, ?>> loadedRegisteries = Collections.newSetFromMap(new WeakHashMap<>());

    private final Map<K ,V> registry;

    protected Registry(){
        this(new ConcurrentHashMap<>());
    }

    protected Registry(Map<K, V> map){
        this.registry = map;
        loadedRegisteries.add(this);
    }

    protected Registry(Registry<K, V> other){
        this();
        this.registry.putAll(other.registry);
    }

    public V get(K key){
        return key == null ? null : registry.get(key);
    }

    public V get(K key, V def){
        return key == null ? def : registry.getOrDefault(key, def);
    }

    public V computeIfAbsent(K key, Function<K, V> mappingFunction){
        return key == null ? null : registry.computeIfAbsent(key, mappingFunction);
    }

    public V add(K key, V value){
        return value == null ? null : registry.put(key, value);
    }

    public V remove(K key){
        return key == null ? null : registry.remove(key);
    }

    public boolean containsKey(K key){
        return key != null && registry.containsKey(key);
    }

    public Collection<V> values(){
        return registry.values();
    }

    public Collection<K> keys(){
        return registry.keySet();
    }

    public Collection<Map.Entry<K, V>> entries(){
        return registry.entrySet();
    }

    public void clear(){
        registry.clear();
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
        return new ConcurrentHashMap<>(registry);
    }

    public int size(){
        return registry.size();
    }

    public boolean isEmpty(){
        return size() == 0;
    }

    @Override
    public Iterator<V> iterator() {
        return registry.values().iterator();
    }

    @Override
    public String toString() {
        return registry.toString();
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
        return createRegistry(new SynchronizedLinkedHashMap<>());
    }

}
