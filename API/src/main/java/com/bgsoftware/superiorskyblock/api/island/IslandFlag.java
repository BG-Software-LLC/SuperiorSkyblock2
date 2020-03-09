package com.bgsoftware.superiorskyblock.api.island;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class IslandFlag {

    private static final Map<String, IslandFlag> islandFlags = new HashMap<>();

    private String name;

    private IslandFlag(String name){
        this.name = name.toUpperCase();
    }

    /**
     * Get the name of the sorting type.
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
        IslandFlag islandFlag = islandFlags.get(name.toUpperCase());

        if(islandFlag == null)
            throw new IllegalArgumentException("Couldn't find an IslandFlag with the name " + name + ".");

        return islandFlag;
    }

    @Override
    public String toString() {
        return "SortingType{name=" + name + "}";
    }

    /**
     * Register a new sorting type.
     * @param name The name for the sorting type.
     */
    public static void register(String name){
        name = name.toUpperCase();

        if(islandFlags.containsKey(name))
            throw new IllegalStateException("IslandFlag with the name " + name + " already exists.");

        islandFlags.put(name, new IslandFlag(name));
    }

}
