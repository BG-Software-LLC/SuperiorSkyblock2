package com.bgsoftware.superiorskyblock.utils;

import java.util.Map;

public final class Pair<K, V> {

    private K key;
    private V value;

    public Pair(Map.Entry<K, V> entry){
        this(entry.getKey(), entry.getValue());
    }

    public Pair(K key, V value){
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "{" + key + "=" + value + "}";
    }
}
