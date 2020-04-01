package com.bgsoftware.superiorskyblock.api.island;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public final class SortingType implements Comparator<Island> {

    private static final Map<String, SortingType> sortingTypes = new HashMap<>();

    private static final Comparator<Island> ISLAND_NAMES_COMPARATOR = (o1, o2) -> {
        String firstName = o1.getName().isEmpty() ? o1.getOwner().getName() : o1.getName();
        String secondName = o2.getName().isEmpty() ? o2.getOwner().getName() : o2.getName();
        return firstName.compareTo(secondName);
    };

    private String name;
    private Comparator<Island> comparator;

    private SortingType(String name, Comparator<Island> comparator, boolean handleEqualsIslands){
        this.name = name;
        this.comparator = !handleEqualsIslands ? comparator : (o1, o2) -> {
            int compare = comparator.compare(o1, o2);
            return compare == 0 ? ISLAND_NAMES_COMPARATOR.compare(o1, o2) : compare;
        };
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
    public int compare(Island o1, Island o2) {
        return comparator.compare(o1, o2);
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
        register(name, comparator, true);
    }

    /**
     * Register a new sorting type.
     * @param name The name for the sorting type.
     * @param comparator The comparator for sorting the islands.
     * @param handleEqualsIslands Should the plugin handle equals islands?
     *                            If that's false, you should handle it on your own.
     */
    public static void register(String name, Comparator<Island> comparator, boolean handleEqualsIslands){
        Preconditions.checkState(!sortingTypes.containsKey(name), "SortingType with the name " + name + " already exists.");

        SortingType sortingType = new SortingType(name, comparator, handleEqualsIslands);
        sortingTypes.put(name, sortingType);
        SuperiorSkyblockAPI.getGrid().registerSortingType(sortingType);
    }

}
