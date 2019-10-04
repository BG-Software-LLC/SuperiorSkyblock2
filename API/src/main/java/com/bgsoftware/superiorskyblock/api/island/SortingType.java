package com.bgsoftware.superiorskyblock.api.island;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SortingType {

    private static final Map<String, SortingType> sortingTypes = new HashMap<>();

    private String name;
    private Comparator<UUID> comparator;

    private SortingType(String name, Comparator<UUID> comparator){
        this.name = name;
        this.comparator = comparator;
    }

    public String getName() {
        return name;
    }

    public Comparator<UUID> getComparator() {
        return comparator;
    }

    public static Collection<SortingType> values(){
        return sortingTypes.values();
    }

    public static SortingType getByName(String name){
        return sortingTypes.get(name);
    }

    @Override
    public String toString() {
        return "SortingType{name=" + name + "}";
    }

    public static void register(String name, Comparator<UUID> comparator){
        if(sortingTypes.containsKey(name))
            throw new IllegalStateException("SortingType with the name " + name + " already exists.");

        sortingTypes.put(name, new SortingType(name, comparator));
    }

}
