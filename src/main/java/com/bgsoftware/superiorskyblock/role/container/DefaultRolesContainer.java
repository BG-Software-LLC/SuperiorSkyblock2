package com.bgsoftware.superiorskyblock.role.container;

import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class DefaultRolesContainer implements RolesContainer {

    private final Map<Integer, PlayerRole> rolesByWeight = new HashMap<>();
    private final Map<Integer, PlayerRole> rolesById = new HashMap<>();
    private final Map<String, PlayerRole> rolesByName = new HashMap<>();

    @Nullable
    @Override
    public PlayerRole getPlayerRole(int index) {
        return rolesByWeight.get(index);
    }

    @Nullable
    @Override
    public PlayerRole getPlayerRoleFromId(int id) {
        return rolesById.get(id);
    }

    @Override
    public PlayerRole getPlayerRole(String name) {
        PlayerRole playerRole = rolesByName.get(name.toUpperCase());

        Preconditions.checkArgument(playerRole != null, "Invalid role name: " + name);

        return playerRole;
    }

    @Override
    public List<PlayerRole> getRoles() {
        return rolesById.keySet().stream().sorted().map(rolesById::get).collect(Collectors.toList());
    }

    @Override
    public void addPlayerRole(PlayerRole playerRole) {
        this.rolesByWeight.put(playerRole.getWeight(), playerRole);
        this.rolesById.put(playerRole.getId(), playerRole);
        this.rolesByName.put(playerRole.getName().toUpperCase(), playerRole);
    }

}
