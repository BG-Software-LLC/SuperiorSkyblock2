package com.bgsoftware.superiorskyblock.island.privilege;

import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateMap;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.google.common.base.Preconditions;

public class RolePrivilegeNode extends PrivilegeNodeAbstract {

    private final SPlayerRole playerRole;
    private final RolePrivilegeNode previousNode;

    public RolePrivilegeNode(PlayerRole playerRole, PrivilegeNodeAbstract previousNode) {
        this(playerRole, previousNode, "");
    }

    public RolePrivilegeNode(PlayerRole playerRole, PrivilegeNodeAbstract previousNode, String permissions) {
        this.playerRole = (SPlayerRole) playerRole;
        this.previousNode = (RolePrivilegeNode) previousNode;
        BukkitExecutor.sync(() -> setPermissions(permissions, playerRole != null), 1L);
    }

    private RolePrivilegeNode(EnumerateMap<IslandPrivilege, PrivilegeStatus> privileges,
                              SPlayerRole playerRole, RolePrivilegeNode previousNode) {
        super(privileges);
        this.playerRole = playerRole;
        this.previousNode = previousNode != null ? (RolePrivilegeNode) previousNode.clone() : null;
    }

    @Override
    public boolean hasPermission(IslandPrivilege islandPrivilege) {
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege parameter cannot be null.");

        PrivilegeStatus status = getStatus(islandPrivilege);

        if (status != PrivilegeStatus.DEFAULT) {
            return status == PrivilegeStatus.ENABLED;
        }

        status = previousNode == null ? PrivilegeStatus.DEFAULT : previousNode.getStatus(islandPrivilege);

        if (status != PrivilegeStatus.DEFAULT) {
            return status == PrivilegeStatus.ENABLED;
        }

        return playerRole != null && playerRole.getDefaultPermissions().hasPermission(islandPrivilege);
    }

    @Override
    public void setPermission(IslandPrivilege islandPrivilege, boolean value) {
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege parameter cannot be null.");
        setPermission(islandPrivilege, value, true);
    }

    @Override
    public PrivilegeNodeAbstract clone() {
        return new RolePrivilegeNode(privileges, playerRole, previousNode);
    }

    @Override
    protected boolean isDefault(IslandPrivilege islandPrivilege) {
        if (playerRole != null) {
            return playerRole.getDefaultPermissions().isDefault(islandPrivilege);
        }

        if (previousNode != null && previousNode.isDefault(islandPrivilege))
            return true;

        return privileges.containsKey(islandPrivilege);
    }

    public PrivilegeStatus getStatus(IslandPrivilege permission) {
        return privileges.getOrDefault(permission, PrivilegeStatus.DEFAULT);
    }

    public void setPermission(IslandPrivilege permission, boolean value, boolean keepDisable) {
        if (!value && !keepDisable) {
            privileges.remove(permission);
        } else {
            super.setPermission(permission, value);
        }

        if (previousNode != null)
            previousNode.setPermission(permission, false, false);
    }

    @Override
    public String toString() {
        return "RolePermissionNode" + privileges;
    }

    public static class EmptyRolePermissionNode extends RolePrivilegeNode {

        public static final EmptyRolePermissionNode INSTANCE;

        static {
            INSTANCE = new EmptyRolePermissionNode();
        }

        EmptyRolePermissionNode() {
            super(null, null);
        }

        @Override
        public boolean hasPermission(IslandPrivilege permission) {
            return false;
        }

        @Override
        public void setPermission(IslandPrivilege permission, boolean value) {
            // Do nothing.
        }

    }

}
