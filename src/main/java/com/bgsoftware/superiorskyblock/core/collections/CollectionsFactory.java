package com.bgsoftware.superiorskyblock.core.collections;

import com.bgsoftware.superiorskyblock.core.collections.creator.CollectionsCreator;
import com.bgsoftware.superiorskyblock.core.collections.creator.FastUtilCollectionsCreator;
import com.bgsoftware.superiorskyblock.core.collections.creator.JavaCollectionsCreator;

import java.util.Map;

public class CollectionsFactory {

    private static final CollectionsCreator creator = findSuitableCollectionsCreator();

    private CollectionsFactory() {

    }

    public static <V> Map<Integer, V> createInt2ObjectHashMap() {
        return creator.createInt2ObjectHashMap();
    }

    public static <V> Map<Integer, V> createInt2ObjectArrayMap() {
        return creator.createInt2ObjectArrayMap();
    }

    public static Map<Integer, Integer> createInt2IntArrayMap() {
        return creator.createInt2IntArrayMap();
    }

    public static Map<Integer, Integer> createInt2IntHashMap() {
        return creator.createInt2IntHashMap();
    }

    public static <V> Map<Long, V> createLong2ObjectHashMap() {
        return creator.createLong2ObjectHashMap();
    }

    public static <V> Map<Long, V> createLong2ObjectArrayMap() {
        return creator.createLong2ObjectArrayMap();
    }

    public static <V> Map<Character, V> createChar2ObjectArrayMap() {
        return creator.createChar2ObjectArrayMap();
    }

    private static CollectionsCreator findSuitableCollectionsCreator() {
        try {
            Class.forName("it.unimi.dsi.fastutil.ints.Int2IntArrayMap");
            return FastUtilCollectionsCreator.INSTANCE;
        } catch (ClassNotFoundException ignored) {
        }

        return JavaCollectionsCreator.INSTANCE;
    }

}
