package com.bgsoftware.superiorskyblock.api.island;

public interface PermissionNode extends Cloneable {

    /**
     * Check whether or not the node has a permission.
     * @param islandPrivilege The privilege to check.
     */
    boolean hasPermission(IslandPrivilege islandPrivilege);

    /**
     * Set whether or not the node should have a permission.
     * @param islandPrivilege The privilege to set.
     * @param value The value to set.
     */
    void setPermission(IslandPrivilege islandPrivilege, boolean value);

}
