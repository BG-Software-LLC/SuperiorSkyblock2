package com.bgsoftware.superiorskyblock.api.island;

import com.bgsoftware.superiorskyblock.api.objects.Enumerable;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class IslandFlag implements Enumerable {

    private static final Map<String, IslandFlag> islandFlags = new HashMap<>();
    private static final List<Consumer<IslandFlag>> registrationListeners = new ArrayList<>();
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

        IslandFlag islandFlag = new IslandFlag(name);
        islandFlags.put(name, islandFlag);
        new ArrayList<>(registrationListeners).forEach(listener -> listener.accept(islandFlag));
    }

    /**
     * Listen for island flag registrations.
     *
     * @param listener The listener to register.
     */
    public static void addRegistrationListener(Consumer<IslandFlag> listener) {
        Preconditions.checkNotNull(listener, "listener parameter cannot be null.");
        registrationListeners.add(listener);
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
