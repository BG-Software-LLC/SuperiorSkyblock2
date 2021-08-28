package com.bgsoftware.superiorskyblock.island.permissions;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.google.common.base.Preconditions;

import java.util.Map;

public class PlayerPermissionNode extends PermissionNodeAbstract {

    protected final SuperiorPlayer superiorPlayer;
    protected final Island island;

    public PlayerPermissionNode(SuperiorPlayer superiorPlayer, Island island){
        this.superiorPlayer = superiorPlayer;
        this.island = island;
    }

    public PlayerPermissionNode(SuperiorPlayer superiorPlayer, Island island, String permissions){
        this.superiorPlayer = superiorPlayer;
        this.island = island;
        setPermissions(permissions, false);
    }

    private PlayerPermissionNode(Map<IslandPrivilege, PrivilegeStatus> privileges, SuperiorPlayer superiorPlayer, Island island){
        super(privileges);

        this.superiorPlayer = superiorPlayer;
        this.island = island;
    }

    @Override
    public boolean hasPermission(IslandPrivilege islandPrivilege) {
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege parameter cannot be null.");
        return getStatus(IslandPrivileges.ALL) == PrivilegeStatus.ENABLED || getStatus(islandPrivilege) == PrivilegeStatus.ENABLED;
    }

    @Override
    public PermissionNodeAbstract clone() {
        return new PlayerPermissionNode(privileges, superiorPlayer, island);
    }

    public void loadPrivilege(IslandPrivilege islandPrivilege, byte status){
        privileges.put(islandPrivilege, PrivilegeStatus.of(status));
    }

    protected PrivilegeStatus getStatus(IslandPrivilege islandPrivilege) {
        PrivilegeStatus cachedStatus = privileges.getOrDefault(islandPrivilege, PrivilegeStatus.DEFAULT);

        if(cachedStatus != PrivilegeStatus.DEFAULT)
            return cachedStatus;

        PlayerRole playerRole = island.isMember(superiorPlayer) ? superiorPlayer.getPlayerRole() :
                island.isCoop(superiorPlayer) ? SPlayerRole.coopRole() : SPlayerRole.guestRole();

        return island.hasPermission(playerRole, islandPrivilege) ? PrivilegeStatus.ENABLED : PrivilegeStatus.DISABLED;
    }

    public static class EmptyPlayerPermissionNode extends PlayerPermissionNode{

        public static final EmptyPlayerPermissionNode INSTANCE;

        static {
            INSTANCE = new EmptyPlayerPermissionNode();
        }

        EmptyPlayerPermissionNode(){
            this(null, null);
        }

        EmptyPlayerPermissionNode(SuperiorPlayer superiorPlayer, Island island){
            super(null, superiorPlayer, island);
        }

        @Override
        public boolean hasPermission(IslandPrivilege islandPrivilege) {
            Preconditions.checkNotNull(islandPrivilege, "islandPrivilege parameter cannot be null.");
            return superiorPlayer != null && island != null && super.hasPermission(islandPrivilege);
        }

        @Override
        public void setPermission(IslandPrivilege permission, boolean value) {

        }

        @Override
        protected PrivilegeStatus getStatus(IslandPrivilege islandPrivilege) {
            PlayerRole playerRole = island.isMember(superiorPlayer) ? superiorPlayer.getPlayerRole() : island.isCoop(superiorPlayer) ? SPlayerRole.coopRole() : SPlayerRole.guestRole();

            if(island.hasPermission(playerRole, islandPrivilege))
                return PrivilegeStatus.ENABLED;

            return PrivilegeStatus.DISABLED;
        }
    }

}
