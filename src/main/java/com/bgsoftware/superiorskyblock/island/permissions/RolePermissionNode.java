package com.bgsoftware.superiorskyblock.island.permissions;

import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.google.common.base.Preconditions;

public class RolePermissionNode extends PermissionNodeAbstract {

    private final SPlayerRole playerRole;
    private final RolePermissionNode previousNode;

    public RolePermissionNode(PlayerRole playerRole, PermissionNodeAbstract previousNode){
        this(playerRole, previousNode, "");
    }

    public RolePermissionNode(PlayerRole playerRole, PermissionNodeAbstract previousNode, String permissions){
        this.playerRole = (SPlayerRole) playerRole;
        this.previousNode = (RolePermissionNode) previousNode;
        Executor.sync(() -> setPermissions(permissions, playerRole != null), 1L);
    }

    private RolePermissionNode(Registry<IslandPrivilege, PrivilegeStatus> privileges, SPlayerRole playerRole, RolePermissionNode previousNode){
        super(privileges);

        this.playerRole = playerRole;
        this.previousNode = previousNode != null ? (RolePermissionNode) previousNode.clone() : null;
    }

    @Override
    public boolean hasPermission(IslandPrivilege islandPrivilege) {
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege parameter cannot be null.");

        PrivilegeStatus status = getStatus(islandPrivilege);

        if(status != PrivilegeStatus.DEFAULT){
            return status == PrivilegeStatus.ENABLED;
        }

        status = previousNode == null ? PrivilegeStatus.DEFAULT : previousNode.getStatus(islandPrivilege);

        if(status != PrivilegeStatus.DEFAULT){
            return status == PrivilegeStatus.ENABLED;
        }

        return playerRole != null && playerRole.getDefaultPermissions().hasPermission(islandPrivilege);
    }

    public PrivilegeStatus getStatus(IslandPrivilege permission){
        return privileges.get(permission, PrivilegeStatus.DEFAULT);
    }

    @Override
    public void setPermission(IslandPrivilege islandPrivilege, boolean value) {
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege parameter cannot be null.");
        setPermission(islandPrivilege, value, true);
    }

    public void setPermission(IslandPrivilege permission, boolean value, boolean keepDisable) {
        if(!value && !keepDisable){
            privileges.remove(permission);
        }
        else {
            super.setPermission(permission, value);
        }

        if (previousNode != null)
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
    public PermissionNodeAbstract clone() {
        return new RolePermissionNode(privileges, playerRole, previousNode);
    }

    @Override
    public String toString() {
        return "RolePermissionNode" + privileges;
    }

    public static class EmptyRolePermissionNode extends RolePermissionNode{

        public static final EmptyRolePermissionNode INSTANCE;

        static {
            INSTANCE = new EmptyRolePermissionNode();
        }

        EmptyRolePermissionNode(){
            super(null, null);
        }

        @Override
        public boolean hasPermission(IslandPrivilege permission) {
            return false;
        }

        @Override
        public void setPermission(IslandPrivilege permission, boolean value) {

        }

    }

}
