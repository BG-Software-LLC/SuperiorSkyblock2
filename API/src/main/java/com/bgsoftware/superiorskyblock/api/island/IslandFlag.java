package com.bgsoftware.superiorskyblock.api.island;

import com.bgsoftware.superiorskyblock.api.objects.Enumerable;
import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class IslandFlag implements Enumerable {

    private static final Map<String, IslandFlag> islandFlags = new HashMap<>();
    private static int ordinalCounter = 0;

    private final String name;
    private final int ordinal;

    private IslandFlag(String name) {
        this.name = name.toUpperCase(Locale.ENGLISH);
        this.ordinal = ordinalCounter++;
    }

    @Override
    public int ordinal() {
        return this.ordinal;
    }

    /**
     * Get all the island flags.
     */
    public static Collection<IslandFlag> values() {
        return islandFlags.values();
    }

    /**
     * Get an island flag by it's name.
     *
     * @param name The name to check.
     */
    public static IslandFlag getByName(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");

        IslandFlag islandFlag = islandFlags.get(name.toUpperCase(Locale.ENGLISH));

        Preconditions.checkNotNull(islandFlag, "Couldn't find an IslandFlag with the name " + name + ".");

        return islandFlag;
    }

    /**
     * Register a new island flag.
     *
     * @param name The name for the island flag.
     */
    public static void register(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");

        name = name.toUpperCase(Locale.ENGLISH);

        Preconditions.checkState(!islandFlags.containsKey(name), "IslandFlag with the name " + name + " already exists.");

        islandFlags.put(name, new IslandFlag(name));
    }

    /**
     * Get the name of the island flag.
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "IslandFlag{name=" + name + "}";
    }

}
