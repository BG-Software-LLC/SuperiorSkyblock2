package com.bgsoftware.superiorskyblock.api.island;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.objects.Enumerable;
import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class SortingType implements Comparator<Island>, Enumerable {

    private static final Map<String, SortingType> sortingTypes = new HashMap<>();
    private static int ordinalCounter = 0;

    private static final Comparator<Island> ISLAND_NAMES_COMPARATOR = (o1, o2) -> {
        String firstName = o1.getStrippedName().isEmpty() ? o1.getOwner() == null ? "null" : o1.getOwner().getName() : o1.getStrippedName();
        String secondName = o2.getStrippedName().isEmpty() ? o2.getOwner() == null ? "null" : o2.getOwner().getName() : o2.getStrippedName();
        return firstName.compareTo(secondName);
    };

    private final String name;
    @Nullable
    private final Function<Island, Number> valueFunction;
    private final Comparator<Island> comparator;
    private final int ordinal;

    private SortingType(String name, @Nullable Function<Island, Number> valueFunction, Comparator<Island> comparator, boolean handleEqualsIslands) {
        this.name = name;
        this.valueFunction = valueFunction;
        this.comparator = !handleEqualsIslands ? comparator : (o1, o2) -> {
            int compare = comparator.compare(o1, o2);
            return compare == 0 ? ISLAND_NAMES_COMPARATOR.compare(o1, o2) : compare;
        };
        this.ordinal = ordinalCounter++;
    }

    @Override
    public int ordinal() {
        return this.ordinal;
    }

    /**
     * Get all the sorting types.
     */
    public static Collection<SortingType> values() {
        return sortingTypes.values();
    }

    /**
     * Get a sorting type by it's name.
     *
     * @param name The name to check.
     */
    public static SortingType getByName(String name) {
        return sortingTypes.get(name);
    }

    /**
     * Register a new sorting type.
     *
     * @param name       The name for the sorting type.
     * @param comparator The comparator for sorting the islands.
     */
    public static void register(String name, Comparator<Island> comparator) {
        register(name, comparator, true);
    }

    /**
     * Register a new sorting type.
     *
     * @param name                The name for the sorting type.
     * @param comparator          The comparator for sorting the islands.
     * @param handleEqualsIslands Should the plugin handle equals islands?
     *                            If that's false, you should handle it on your own.
     */
    public static void register(String name, Comparator<Island> comparator, boolean handleEqualsIslands) {
        register(name, null, comparator, handleEqualsIslands);
    }

    /**
     * Register a new sorting type.
     *
     * @param name          The name for the sorting type.
     * @param valueFunction The value function for sorting type.
     * @param comparator    The comparator for sorting the islands.
     */
    public static void register(String name, @Nullable Function<Island, Number> valueFunction, Comparator<Island> comparator) {
        register(name, valueFunction, comparator, true);
    }

    /**
     * Register a new sorting type.
     *
     * @param name                The name for the sorting type.
     * @param valueFunction       The value function for sorting type.
     * @param comparator          The comparator for sorting the islands.
     * @param handleEqualsIslands Should the plugin handle equals islands?
     *                            If that's false, you should handle it on your own.
     */
    public static void register(String name, @Nullable Function<Island, Number> valueFunction, Comparator<Island> comparator, boolean handleEqualsIslands) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        Preconditions.checkNotNull(comparator, "comparator parameter cannot be null.");
        Preconditions.checkState(!sortingTypes.containsKey(name), "SortingType with the name " + name + " already exists.");

        SortingType sortingType = new SortingType(name, valueFunction, comparator, handleEqualsIslands);
        sortingTypes.put(name, sortingType);
        SuperiorSkyblockAPI.getGrid().registerSortingType(sortingType);
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
     * Get the value of the sorting type for specific island.
     */
    public Optional<Number> getValue(Island island) {
        return valueFunction == null ? Optional.empty() : Optional.ofNullable(valueFunction.apply(island));
    }

    @Override
    public int compare(Island o1, Island o2) {
        return comparator.compare(o1, o2);
    }

    @Override
    public String toString() {
        return "SortingType{name=" + name + "}";
    }

}
