package com.bgsoftware.superiorskyblock.island.role.container;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.collections.CollectionsFactory;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2ObjectMapView;
import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DefaultRolesContainer implements RolesContainer {

    private Int2ObjectMapView<PlayerRole> rolesByWeight = CollectionsFactory.createInt2ObjectArrayMap();
    private Int2ObjectMapView<PlayerRole> rolesById = CollectionsFactory.createInt2ObjectArrayMap();
    private Map<String, PlayerRole> rolesByName = new HashMap<>();

    @Nullable
    @Override
    public synchronized PlayerRole getPlayerRole(int index) {
        return rolesByWeight.get(index);
    }

    @Nullable
    @Override
    public synchronized PlayerRole getPlayerRoleFromId(int id) {
        return rolesById.get(id);
    }

    @Override
    public PlayerRole getPlayerRole(String name) {
        String roleName = name.toUpperCase(Locale.ENGLISH);
        PlayerRole playerRole;
        synchronized (this) {
            playerRole = rolesByName.get(roleName);
        }

        Preconditions.checkArgument(playerRole != null, "Invalid role name: " + name);

        return playerRole;
    }

    @Override
    public List<PlayerRole> getRoles() {
        List<PlayerRole> roles;
        synchronized (this) {
            roles = new SequentialListBuilder<PlayerRole>().mutable().build(rolesById.valueIterator());
        }
        roles.sort(Comparator.comparingInt(PlayerRole::getId));
        return Collections.unmodifiableList(roles);
    }

    @Override
    public void addPlayerRole(PlayerRole playerRole) {
        int weight = playerRole.getWeight();
        int id = playerRole.getId();
        String name = playerRole.getName().toUpperCase(Locale.ENGLISH);
        synchronized (this) {
            this.rolesByWeight.put(weight, playerRole);
            this.rolesById.put(id, playerRole);
            this.rolesByName.put(name, playerRole);
        }
    }

    @Override
    public void replaceRoles(Collection<PlayerRole> roles) {
        DefaultRolesContainer replacement = new DefaultRolesContainer();
        roles.forEach(replacement::addPlayerRole);
        synchronized (this) {
            this.rolesByWeight = replacement.rolesByWeight;
            this.rolesById = replacement.rolesById;
            this.rolesByName = replacement.rolesByName;
        }
    }

    @Override
    public synchronized void clearRoles() {
        this.rolesByWeight.clear();
        this.rolesById.clear();
        this.rolesByName.clear();
    }
}
