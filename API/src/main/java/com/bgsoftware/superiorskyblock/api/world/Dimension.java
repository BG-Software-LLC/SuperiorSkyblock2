package com.bgsoftware.superiorskyblock.api.world;

import com.bgsoftware.superiorskyblock.api.objects.Enumerable;
import com.google.common.base.Preconditions;
import org.bukkit.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Dimension implements Enumerable {

    private static final Map<String, Dimension> dimensions = new HashMap<>();
    private static int ordinalCounter = 0;

    private final String name;
    private final World.Environment environment;
    private final int ordinal;

    private Dimension(String name, World.Environment environment) {
        this.name = name.toUpperCase(Locale.ENGLISH);
        this.environment = environment;
        this.ordinal = ordinalCounter++;
    }

    @Override
    public int ordinal() {
        return this.ordinal;
    }

    /**
     * Get the environment of the world.
     */
    public World.Environment getEnvironment() {
        return environment;
    }

    /**
     * Get all the dimensions..
     */
    public static Collection<Dimension> values() {
        return dimensions.values();
    }

    /**
     * Get a dimension by its name.
     *
     * @param name The name to check.
     */
    public static Dimension getByName(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");

        Dimension islandPrivilege = dimensions.get(name.toUpperCase(Locale.ENGLISH));

        Preconditions.checkNotNull(islandPrivilege, "Couldn't find a Dimension with the name " + name + ".");

        return islandPrivilege;
    }

    /**
     * Register a new dimension.
     *
     * @param name        The name for the dimension.
     * @param environment The environment of the world this dimension refers to.
     */
    public static void register(String name, World.Environment environment) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");

        name = name.toUpperCase(Locale.ENGLISH);

        Preconditions.checkState(!dimensions.containsKey(name), "Dimension with the name " + name + " already exists.");

        dimensions.put(name, new Dimension(name, environment));
    }

    /**
     * Get the name of the dimension.
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Dimension{name=" + name + ",environment=" + environment + "}";
    }

}
