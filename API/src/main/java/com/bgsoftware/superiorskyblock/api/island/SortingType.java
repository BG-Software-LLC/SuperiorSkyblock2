package com.bgsoftware.superiorskyblock.api.island;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public final class SortingType {

    private static final Map<String, SortingType> sortingTypes = new HashMap<>();

    private String name;
    private Comparator<Island> comparator;

    private SortingType(String name, Comparator<Island> comparator){
        this.name = name;
        this.comparator = comparator;
    }

    /**
     * Get the name of the sorting type.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the comparator of the sorting type.
     */
    public Comparator<Island> getComparator() {
        return comparator;
    }

    /**
     * Get all the sorting types.
     */
    public static Collection<SortingType> values(){
        return sortingTypes.values();
    }

    /**
     * Get a sorting type by it's name.
     * @param name The name to check.
     */
    public static SortingType getByName(String name){
        return sortingTypes.get(name);
    }

    @Override
    public String toString() {
        return "SortingType{name=" + name + "}";
    }

    /**
     * Register a new sorting type.
     * @param name The name for the sorting type.
     * @param comparator The comparator for sorting the islands.
     */
    public static void register(String name, Comparator<Island> comparator){
        if(sortingTypes.containsKey(name))
            throw new IllegalStateException("SortingType with the name " + name + " already exists.");

        SortingType sortingType = new SortingType(name, comparator);
        sortingTypes.put(name, sortingType);
        SuperiorSkyblockAPI.getGrid().registerSortingType(sortingType);
    }

}
