package com.bgsoftware.superiorskyblock.island.permissions;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;

public final class PlayerPermissionNode extends PermissionNodeAbstract {

    private final SuperiorPlayer superiorPlayer;
    private final SIsland island;

    public PlayerPermissionNode(SuperiorPlayer superiorPlayer, Island island){
        this(superiorPlayer, island, "");
    }

    public PlayerPermissionNode(SuperiorPlayer superiorPlayer, Island island, String permissions){
        this.superiorPlayer = superiorPlayer;
        this.island = island == null ? null : (SIsland) island;
        setPermissions(permissions, false);
    }

    private PlayerPermissionNode(Registry<IslandPrivilege, PrivilegeStatus> privileges, SuperiorPlayer superiorPlayer, SIsland island){
        super(privileges);

        this.superiorPlayer = superiorPlayer;
        this.island = island;
    }

    @Override
    protected PrivilegeStatus getStatus(IslandPrivilege islandPrivilege) {
        PlayerRole playerRole = island.isMember(superiorPlayer) ? superiorPlayer.getPlayerRole() : island.isCoop(superiorPlayer) ? SPlayerRole.coopRole() : SPlayerRole.guestRole();
        PrivilegeStatus status = island.getPermissionNode(playerRole).getStatus(islandPrivilege);

        if(status == PrivilegeStatus.ENABLED)
            return status;

        return privileges.get(islandPrivilege, PrivilegeStatus.DISABLED);
    }

    @Override
    public PermissionNodeAbstract clone() {
        return new PlayerPermissionNode(privileges, superiorPlayer, island);
    }

}
