package com.bgsoftware.superiorskyblock.island.privilege;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateMap;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.google.common.base.Preconditions;

public class PlayerPrivilegeNode extends PrivilegeNodeAbstract {

    protected final SuperiorPlayer superiorPlayer;
    protected Island island;

    public PlayerPrivilegeNode(SuperiorPlayer superiorPlayer, Island island) {
        this.superiorPlayer = superiorPlayer;
        this.island = island;
    }

    public PlayerPrivilegeNode(SuperiorPlayer superiorPlayer, Island island, String permissions) {
        this.superiorPlayer = superiorPlayer;
        this.island = island;
        setPermissions(permissions, false);
    }

    private PlayerPrivilegeNode(@Nullable EnumerateMap<IslandPrivilege, PrivilegeStatus> privileges,
                                SuperiorPlayer superiorPlayer, Island island) {
        super(privileges);
        this.superiorPlayer = superiorPlayer;
        this.island = island;
    }

    public void setIsland(Island island) {
        this.island = island;
    }

    @Override
    public boolean hasPermission(IslandPrivilege islandPrivilege) {
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege parameter cannot be null.");

        PrivilegeStatus allStatus = privileges.getOrDefault(IslandPrivileges.ALL, PrivilegeStatus.DEFAULT);
        if (allStatus == PrivilegeStatus.ENABLED)
            return true;

        PrivilegeStatus privilegeStatus = privileges.getOrDefault(islandPrivilege, PrivilegeStatus.DEFAULT);
        if (privilegeStatus != PrivilegeStatus.DEFAULT && allStatus != PrivilegeStatus.DEFAULT)
            return privilegeStatus == PrivilegeStatus.ENABLED;

        return checkRoleBasedPermission(superiorPlayer, island, islandPrivilege, allStatus, privilegeStatus);
    }

    @Override
    public PrivilegeNodeAbstract clone() {
        return new PlayerPrivilegeNode(privileges, superiorPlayer, island);
    }

    public void loadPrivilege(IslandPrivilege islandPrivilege, byte status) {
        privileges.put(islandPrivilege, PrivilegeStatus.of(status));
    }

    public static boolean checkRoleBasedPermission(SuperiorPlayer superiorPlayer, Island island,
                                                   IslandPrivilege islandPrivilege) {
        return checkRoleBasedPermission(superiorPlayer, island, islandPrivilege, PrivilegeStatus.DEFAULT, PrivilegeStatus.DEFAULT);
    }

    private static boolean checkRoleBasedPermission(SuperiorPlayer superiorPlayer, Island island,
                                                    IslandPrivilege islandPrivilege, PrivilegeStatus allStatus,
                                                    PrivilegeStatus privilegeStatus) {
        PlayerRole playerRole = IslandUtils.getPlayerRole(island, superiorPlayer);

        if (allStatus == PrivilegeStatus.DEFAULT && island.hasPermission(playerRole, IslandPrivileges.ALL))
            return true;

        return privilegeStatus == PrivilegeStatus.DEFAULT ? island.hasPermission(playerRole, islandPrivilege)
                : privilegeStatus == PrivilegeStatus.ENABLED;
    }

    public static class EmptyPlayerPermissionNode extends PlayerPrivilegeNode {

        public static final EmptyPlayerPermissionNode INSTANCE = new EmptyPlayerPermissionNode();

        EmptyPlayerPermissionNode() {
            this(null, null);
        }

        EmptyPlayerPermissionNode(SuperiorPlayer superiorPlayer, Island island) {
            super(null, superiorPlayer, island);
        }

        @Override
        public boolean hasPermission(IslandPrivilege islandPrivilege) {
            return false;
        }

        @Override
        public void setPermission(IslandPrivilege permission, boolean value) {
            // Do nothing.
        }

    }

}
