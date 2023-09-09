package com.bgsoftware.superiorskyblock.island.role.container;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.google.common.base.Preconditions;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DefaultRolesContainer implements RolesContainer {

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
        PlayerRole playerRole = rolesByName.get(name.toUpperCase(Locale.ENGLISH));

        Preconditions.checkArgument(playerRole != null, "Invalid role name: " + name);

        return playerRole;
    }

    @Override
    public List<PlayerRole> getRoles() {
        return new SequentialListBuilder<PlayerRole>()
                .sorted(Comparator.comparingInt(PlayerRole::getId))
                .build(rolesById.values());
    }

    @Override
    public void addPlayerRole(PlayerRole playerRole) {
        this.rolesByWeight.put(playerRole.getWeight(), playerRole);
        this.rolesById.put(playerRole.getId(), playerRole);
        this.rolesByName.put(playerRole.getName().toUpperCase(Locale.ENGLISH), playerRole);
    }

}
