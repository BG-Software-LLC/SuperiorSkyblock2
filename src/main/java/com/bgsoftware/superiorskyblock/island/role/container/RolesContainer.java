package com.bgsoftware.superiorskyblock.island.role.container;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;

import java.util.Collection;
import java.util.List;

public interface RolesContainer {

    @Nullable
    PlayerRole getPlayerRole(int index);

    @Nullable
    PlayerRole getPlayerRoleFromId(int id);

    PlayerRole getPlayerRole(String name);

    List<PlayerRole> getRoles();

    void addPlayerRole(PlayerRole playerRole);

    void clearRoles();

    default void replaceRoles(Collection<PlayerRole> roles) {
        clearRoles();
        roles.forEach(this::addPlayerRole);
    }

}
