package com.bgsoftware.superiorskyblock.core.collections;

import com.bgsoftware.superiorskyblock.core.collections.creator.CollectionsCreator;
import com.bgsoftware.superiorskyblock.core.collections.creator.JavaCollectionsCreator;

import java.util.Map;

public class CollectionsFactory {

    private static CollectionsCreator creator = JavaCollectionsCreator.INSTANCE;

    private CollectionsFactory() {

    }

    public static void setCreator(CollectionsCreator creator) {
        CollectionsFactory.creator = creator;
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

}
