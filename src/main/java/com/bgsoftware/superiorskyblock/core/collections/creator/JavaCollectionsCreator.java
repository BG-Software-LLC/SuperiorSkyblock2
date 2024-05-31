package com.bgsoftware.superiorskyblock.core.collections.creator;

import com.bgsoftware.superiorskyblock.core.collections.ArrayMap;

import java.util.HashMap;
import java.util.Map;

public class JavaCollectionsCreator implements CollectionsCreator {

    public static final JavaCollectionsCreator INSTANCE = new JavaCollectionsCreator();

    private JavaCollectionsCreator() {

    }

    @Override
    public <V> Map<Integer, V> createInt2ObjectHashMap() {
        return new HashMap<>();
    }

    @Override
    public <V> Map<Integer, V> createInt2ObjectArrayMap() {
        return new ArrayMap<>();
    }

    @Override
    public Map<Integer, Integer> createInt2IntHashMap() {
        return new HashMap<>();
    }

    @Override
    public Map<Integer, Integer> createInt2IntArrayMap() {
        return new ArrayMap<>();
    }

    @Override
    public <V> Map<Long, V> createLong2ObjectHashMap() {
        return new HashMap<>();
    }

    @Override
    public <V> Map<Long, V> createLong2ObjectArrayMap() {
        return new ArrayMap<>();
    }

    @Override
    public <V> Map<Character, V> createChar2ObjectArrayMap() {
        return new ArrayMap<>();
    }

}
