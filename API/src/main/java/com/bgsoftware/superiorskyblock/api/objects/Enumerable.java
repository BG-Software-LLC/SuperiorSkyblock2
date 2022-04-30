package com.bgsoftware.superiorskyblock.api.objects;

/**
 * This class is used in internal data structures for a more optimized way of doing data lookups.
 * This is mainly used for lookups for {@link com.bgsoftware.superiorskyblock.api.island.IslandPrivilege} and
 * {@link com.bgsoftware.superiorskyblock.api.island.IslandFlag}.
 */
public interface Enumerable {

    /**
     * The ordinal of this enumeration constant.
     */
    int ordinal();

}
