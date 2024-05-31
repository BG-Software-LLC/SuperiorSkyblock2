package com.bgsoftware.superiorskyblock.core.collections.creator;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

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

    @Override
    public <V> Map<Long, V> createLong2ObjectHashMap() {
        return new Long2ObjectOpenHashMap<>();
    }

    @Override
    public <V> Map<Long, V> createLong2ObjectArrayMap() {
        return new Long2ObjectArrayMap<>();
    }

    @Override
    public <V> Map<Character, V> createChar2ObjectArrayMap() {
        return new Char2ObjectArrayMap<>();
    }

}
