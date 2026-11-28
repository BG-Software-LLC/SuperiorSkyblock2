package com.bgsoftware.superiorskyblock.core.key.category;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.key.KeyCategory;
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

    /**
     * Get the id of an existing category, or register a new one if it does not exist.
     * This method is not thread-safe and must only be called from the main thread.
     */
    public static KeyCategoryId getOrRegister(String name) {
        KeyCategoryId id = getByName(name);
        return id != null ? id : register(name);
    }

    @Nullable
    public static KeyCategoryId getByName(String name) {
        return cache.get(name.toUpperCase(Locale.ENGLISH));
    }

    @Nullable
    public static KeyCategoryId of(KeyCategory category) {
        if (category instanceof BaseKeyCategory)
            return ((BaseKeyCategory) category).getId();

        return getByName(category.getName());
    }

    public static Collection<KeyCategoryId> values() {
        return cache.values();
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int ordinal() {
        return this.ordinal;
    }

}
