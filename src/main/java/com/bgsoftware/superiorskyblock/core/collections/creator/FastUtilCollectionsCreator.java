package com.bgsoftware.superiorskyblock.core.collections.creator;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Map;

public class FastUtilCollectionsCreator implements CollectionsCreator {

    public static final FastUtilCollectionsCreator INSTANCE = new FastUtilCollectionsCreator();

    private FastUtilCollectionsCreator() {

    }

    @Override
    public <V> Map<Integer, V> createInt2ObjectHashMap() {
        return new Int2ObjectOpenHashMap<>();
    }

    @Override
    public <V> Map<Integer, V> createInt2ObjectArrayMap() {
        return new Int2ObjectArrayMap<>();
    }

    @Override
    public Map<Integer, Integer> createInt2IntHashMap() {
        return new Int2IntOpenHashMap();
    }

    @Override
    public Map<Integer, Integer> createInt2IntArrayMap() {
        return new Int2IntArrayMap();
    }

}
