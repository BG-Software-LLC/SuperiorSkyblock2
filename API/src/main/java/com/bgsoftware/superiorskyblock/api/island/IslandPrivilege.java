package com.bgsoftware.superiorskyblock.api.island;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class IslandPrivilege {

    private static final Map<String, IslandPrivilege> islandPrivileges = new HashMap<>();

    private final String name;

    private IslandPrivilege(String name){
        this.name = name.toUpperCase();
    }

    /**
     * Get the name of the island privilege.
     */
    public String getName() {
        return name;
    }

    /**
     * Get all the island privileges.
     */
    public static Collection<IslandPrivilege> values(){
        return islandPrivileges.values();
    }

    /**
     * Get an island privilege by it's name.
     * @param name The name to check.
     */
    public static IslandPrivilege getByName(String name){
        Preconditions.checkNotNull(name, "name parameter cannot be null.");

        IslandPrivilege islandPrivilege = islandPrivileges.get(name.toUpperCase());

        Preconditions.checkNotNull(islandPrivilege, "Couldn't find an IslandPrivilege with the name " + name + ".");

        return islandPrivilege;
    }

    @Override
    public String toString() {
        return "IslandPrivilege{name=" + name + "}";
    }

    /**
     * Register a new island privilege.
     * @param name The name for the island privilege.
     */
    public static void register(String name){
        Preconditions.checkNotNull(name, "name parameter cannot be null.");

        name = name.toUpperCase();

        Preconditions.checkState(!islandPrivileges.containsKey(name), "IslandPrivilege with the name " + name + " already exists.");

        islandPrivileges.put(name, new IslandPrivilege(name));
    }

}
