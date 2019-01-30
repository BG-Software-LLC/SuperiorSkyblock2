package com.bgsoftware.superiorskyblock.api.island;

public interface PermissionNode extends Cloneable {

    boolean hasPermission(IslandPermission permission);

    void setPermission(IslandPermission permission, boolean value);

    String getColoredPermissions();



}
