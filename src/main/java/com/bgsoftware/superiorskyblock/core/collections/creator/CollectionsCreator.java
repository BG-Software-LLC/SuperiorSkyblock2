package com.bgsoftware.superiorskyblock.core.collections.creator;

import java.util.Map;

public interface CollectionsCreator {

    <V> Map<Integer, V> createInt2ObjectHashMap();

    <V> Map<Integer, V> createInt2ObjectArrayMap();

    Map<Integer, Integer> createInt2IntHashMap();

    Map<Integer, Integer> createInt2IntArrayMap();

    <V> Map<Long, V> createLong2ObjectHashMap();

    <V> Map<Long, V> createLong2ObjectArrayMap();

    <V> Map<Character, V> createChar2ObjectArrayMap();

}
