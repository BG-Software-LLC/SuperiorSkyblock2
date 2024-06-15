package com.bgsoftware.superiorskyblock.island.role.container;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.collections.Maps;
import com.bgsoftware.common.collections.ints.Int2ObjectMap;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.google.common.base.Preconditions;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DefaultRolesContainer implements RolesContainer {

    private final Int2ObjectMap<PlayerRole> rolesByWeight = Maps.newInt2ObjectArrayMap();
    private final Int2ObjectMap<PlayerRole> rolesById = Maps.newInt2ObjectArrayMap();
    private final Map<String, PlayerRole> rolesByName = Maps.newHashMap();

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
                .build(rolesById.valuesIterator());
    }

    @Override
    public void addPlayerRole(PlayerRole playerRole) {
        this.rolesByWeight.put(playerRole.getWeight(), playerRole);
        this.rolesById.put(playerRole.getId(), playerRole);
        this.rolesByName.put(playerRole.getName().toUpperCase(Locale.ENGLISH), playerRole);
    }

}
