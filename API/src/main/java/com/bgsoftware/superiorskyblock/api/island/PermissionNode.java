package com.bgsoftware.superiorskyblock.api.island;

public interface PermissionNode extends Cloneable {

    /**
     * Check whether or not the node has a permission.
     * @param permission The permission to check.
     *
     * @deprecated See hasPermission(IslandPrivilege)
     */
    @Deprecated
    boolean hasPermission(IslandPermission permission);

    /**
     * Set whether or not the node should have a permission.
     * @param permission The permission to set.
     * @param value The value to set.
     *
     * @deprecated See setPermission(IslandPrivilege, Boolean)
     */
    @Deprecated
    void setPermission(IslandPermission permission, boolean value);

    /**
     * Check whether or not the node has a permission.
     * @param permission The permission to check.
     */
    boolean hasPermission(IslandPrivilege permission);

    /**
     * Set whether or not the node should have a permission.
     * @param permission The permission to set.
     * @param value The value to set.
     */
    void setPermission(IslandPrivilege permission, boolean value);

}
