package com.bgsoftware.superiorskyblock.api.island;

import com.bgsoftware.superiorskyblock.api.objects.Enumerable;
import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class IslandPrivilege implements Enumerable {

    private static final Map<String, IslandPrivilege> islandPrivileges = new HashMap<>();
    private static int ordinalCounter = 0;

    private final String name;
    private final int ordinal;

    private IslandPrivilege(String name) {
        this.name = name.toUpperCase(Locale.ENGLISH);
        this.ordinal = ordinalCounter++;
    }

    @Override
    public int ordinal() {
        return this.ordinal;
    }

    /**
     * Get all the island privileges.
     */
    public static Collection<IslandPrivilege> values() {
        return islandPrivileges.values();
    }

    /**
     * Get an island privilege by it's name.
     *
     * @param name The name to check.
     */
    public static IslandPrivilege getByName(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");

        IslandPrivilege islandPrivilege = islandPrivileges.get(name.toUpperCase(Locale.ENGLISH));

        Preconditions.checkNotNull(islandPrivilege, "Couldn't find an IslandPrivilege with the name " + name + ".");

        return islandPrivilege;
    }

    /**
     * Register a new island privilege.
     *
     * @param name The name for the island privilege.
     */
    public static void register(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");

        name = name.toUpperCase(Locale.ENGLISH);

        Preconditions.checkState(!islandPrivileges.containsKey(name), "IslandPrivilege with the name " + name + " already exists.");

        islandPrivileges.put(name, new IslandPrivilege(name));
    }

    /**
     * Get the name of the island privilege.
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "IslandPrivilege{name=" + name + "}";
    }

}
