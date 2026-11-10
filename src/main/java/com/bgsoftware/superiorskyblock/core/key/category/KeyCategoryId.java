package com.bgsoftware.superiorskyblock.core.key.category;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.objects.Enumerable;
import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class KeyCategoryId implements Enumerable {

    private static final Map<String, KeyCategoryId> cache = new HashMap<>();
    private static int ordinalCounter = 0;

    private final String name;
    private final int ordinal;

    private KeyCategoryId(String name) {
        this.name = name.toUpperCase(Locale.ENGLISH);
        Preconditions.checkState(cache.computeIfAbsent(this.name, n -> this) == this);
        this.ordinal = ordinalCounter++;
    }

    public static KeyCategoryId register(String name) {
        return new KeyCategoryId(name);
    }

    @Nullable
    public static KeyCategoryId getByName(String name) {
        return cache.get(name);
    }

    public static Collection<KeyCategoryId> values() {
        return cache.values();
    }

    public String getName() {
        return name;
    }

    @Override
    public int ordinal() {
        return this.ordinal;
    }

}
