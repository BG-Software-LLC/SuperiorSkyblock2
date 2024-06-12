package com.bgsoftware.superiorskyblock.core.collections;

import com.bgsoftware.superiorskyblock.core.collections.creator.CollectionsCreator;
import com.bgsoftware.superiorskyblock.core.collections.creator.FastUtilCollectionsCreator;
import com.bgsoftware.superiorskyblock.core.collections.creator.JavaCollectionsCreator;
import com.bgsoftware.superiorskyblock.core.collections.view.Char2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2IntMapView;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.collections.view.Long2ObjectMapView;

public class CollectionsFactory {

    private static final CollectionsCreator creator = findSuitableCollectionsCreator();

    private CollectionsFactory() {

    }

    public static <V> Int2ObjectMapView<V> createInt2ObjectLinkedHashMap() {
        return creator.createInt2ObjectLinkedHashMap();
    }

    public static <V> Int2ObjectMapView<V> createInt2ObjectArrayMap() {
        return creator.createInt2ObjectArrayMap();
    }

    public static Int2IntMapView createInt2IntArrayMap() {
        return creator.createInt2IntArrayMap();
    }

    public static Int2IntMapView createInt2IntHashMap() {
        return creator.createInt2IntHashMap();
    }

    public static <V> Long2ObjectMapView<V> createLong2ObjectHashMap() {
        return creator.createLong2ObjectHashMap();
    }

    public static <V> Long2ObjectMapView<V> createLong2ObjectLinkedHashMap() {
        return creator.createLong2ObjectLinkedHashMap();
    }

    public static <V> Long2ObjectMapView<V> createLong2ObjectArrayMap() {
        return creator.createLong2ObjectArrayMap();
    }

    public static <V> Char2ObjectMapView<V> createChar2ObjectArrayMap() {
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
