package com.bgsoftware.superiorskyblock.api.island;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class IslandFlag {

    private static final Map<String, IslandFlag> islandFlags = new HashMap<>();

    private final String name;

    private IslandFlag(String name){
        this.name = name.toUpperCase();
    }

    /**
     * Get the name of the island flag.
     */
    public String getName() {
        return name;
    }

    /**
     * Get all the island flags.
     */
    public static Collection<IslandFlag> values(){
        return islandFlags.values();
    }

    /**
     * Get an island flag by it's name.
     * @param name The name to check.
     */
    public static IslandFlag getByName(String name){
        Preconditions.checkNotNull(name, "name parameter cannot be null.");

        IslandFlag islandFlag = islandFlags.get(name.toUpperCase());

        Preconditions.checkNotNull(islandFlag, "Couldn't find an IslandFlag with the name " + name + ".");

        return islandFlag;
    }

    @Override
    public String toString() {
        return "IslandFlag{name=" + name + "}";
    }

    /**
     * Register a new island flag.
     * @param name The name for the island flag.
     */
    public static void register(String name){
        Preconditions.checkNotNull(name, "name parameter cannot be null.");

        name = name.toUpperCase();

        Preconditions.checkState(!islandFlags.containsKey(name), "IslandFlag with the name " + name + " already exists.");

        islandFlags.put(name, new IslandFlag(name));
    }

}
