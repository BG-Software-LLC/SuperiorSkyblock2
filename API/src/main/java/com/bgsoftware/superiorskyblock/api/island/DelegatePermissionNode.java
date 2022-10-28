package com.bgsoftware.superiorskyblock.api.island;

import java.util.Map;

public class DelegatePermissionNode implements PermissionNode {

    protected final PermissionNode handle;

    protected DelegatePermissionNode(PermissionNode handle) {
        this.handle = handle;
    }

    @Override
    public boolean hasPermission(IslandPrivilege islandPrivilege) {
        return this.handle.hasPermission(islandPrivilege);
    }

    @Override
    public void setPermission(IslandPrivilege islandPrivilege, boolean value) {
        this.handle.setPermission(islandPrivilege, value);
    }

    @Override
    public Map<IslandPrivilege, Boolean> getCustomPermissions() {
        return this.handle.getCustomPermissions();
    }

}
