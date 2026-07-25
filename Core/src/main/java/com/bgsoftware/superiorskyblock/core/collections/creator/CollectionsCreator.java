package com.bgsoftware.superiorskyblock.core.collections.creator;

import com.bgsoftware.superiorskyblock.core.collections.view.Char2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2IntMapView;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.collections.view.Long2ObjectMapView;

public interface CollectionsCreator {

    <V> Int2ObjectMapView<V> createInt2ObjectLinkedHashMap();

    <V> Int2ObjectMapView<V> createInt2ObjectArrayMap();

    Int2IntMapView createInt2IntHashMap();

    Int2IntMapView createInt2IntArrayMap();

    <V> Long2ObjectMapView<V> createLong2ObjectHashMap();

    <V> Long2ObjectMapView<V> createLong2ObjectLinkedHashMap();

    <V> Long2ObjectMapView<V> createLong2ObjectArrayMap();

    <V> Char2ObjectMapView<V> createChar2ObjectArrayMap();

}
