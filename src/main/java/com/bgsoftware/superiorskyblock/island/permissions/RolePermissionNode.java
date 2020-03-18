package com.bgsoftware.superiorskyblock.island.permissions;

import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;

public class RolePermissionNode extends PermissionNodeAbstract {

    private final SPlayerRole playerRole;
    private final RolePermissionNode previousNode;

    public RolePermissionNode(PlayerRole playerRole, PermissionNodeAbstract previousNode){
        this(playerRole, previousNode, "");
    }

    public RolePermissionNode(PlayerRole playerRole, PermissionNodeAbstract previousNode, String permissions){
        this.playerRole = (SPlayerRole) playerRole;
        this.previousNode = (RolePermissionNode) previousNode;
        setPermissions(permissions, playerRole != null);
    }

    private RolePermissionNode(Registry<IslandPrivilege, PrivilegeStatus> privileges, SPlayerRole playerRole, RolePermissionNode previousNode){
        super(privileges);

        this.playerRole = playerRole;
        this.previousNode = previousNode != null ? (RolePermissionNode) previousNode.clone() : null;
    }

    @Override
    public boolean hasPermission(IslandPrivilege permission) {
        return super.hasPermission(permission) || (previousNode != null && previousNode.hasPermission(permission));
    }

    public boolean hasPermissionRaw(IslandPrivilege permission) {
        return privileges.get(permission, PrivilegeStatus.DISABLED) == PrivilegeStatus.ENABLED;
    }

    @Override
    public void setPermission(IslandPrivilege permission, boolean value) {
        setPermission(permission, value, true);
    }

    public void setPermission(IslandPrivilege permission, boolean value, boolean keepDisable) {
        if(!value && !keepDisable){
            privileges.remove(permission);
        }
        else {
            super.setPermission(permission, value);
        }

        if (value && previousNode != null)
            previousNode.setPermission(permission, false, false);
    }

    @Override
    protected boolean isDefault(IslandPrivilege islandPrivilege) {
        if(playerRole != null){
            return playerRole.getDefaultPermissions().isDefault(islandPrivilege);
        }

        if(previousNode != null && previousNode.isDefault(islandPrivilege))
            return true;

        return privileges.containsKey(islandPrivilege);
    }

    @Override
    protected PrivilegeStatus getStatus(IslandPrivilege islandPrivilege) {
        return privileges.get(islandPrivilege, playerRole == null ? PrivilegeStatus.DISABLED : playerRole.getDefaultPermissions().getStatus(islandPrivilege));
    }

    @Override
    public PermissionNodeAbstract clone() {
        return new RolePermissionNode(privileges, playerRole, previousNode);
    }

    public static class EmptyRolePermissionNode extends RolePermissionNode{

        public static EmptyRolePermissionNode INSTANCE;

        EmptyRolePermissionNode(){
            super(null, null);
            INSTANCE = this;
        }

        @Override
        public boolean hasPermission(IslandPrivilege permission) {
            return false;
        }

        @Override
        public void setPermission(IslandPrivilege permission, boolean value) {

        }

        @Override
        protected PrivilegeStatus getStatus(IslandPrivilege islandPrivilege) {
            return PrivilegeStatus.DISABLED;
        }

    }

}
