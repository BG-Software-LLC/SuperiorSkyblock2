package com.bgsoftware.superiorskyblock.island.role;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.island.privilege.RolePrivilegeNode;
import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SPlayerRole implements PlayerRole {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final String name;
    private final String displayName;
    private final int id;
    private final int weight;
    private final RolePrivilegeNode defaultPermissions;

    public SPlayerRole(String name, @Nullable String displayName, int id, int weight, List<String> defaultPermissions,
                       @Nullable SPlayerRole previousRole) {
        this.name = name;
        this.displayName = displayName == null ? name : displayName;
        this.id = id;
        this.weight = weight;

        String permissions = defaultPermissions.isEmpty() ? null : String.join(";", defaultPermissions);

        this.defaultPermissions = new RolePrivilegeNode(null,
                previousRole == null ? RolePrivilegeNode.EmptyRolePermissionNode.INSTANCE : previousRole.defaultPermissions,
                permissions);
    }

    public static PlayerRole defaultRole() {
        return plugin.getRoles().getDefaultRole();
    }

    public static PlayerRole lastRole() {
        return plugin.getRoles().getLastRole();
    }

    public static PlayerRole guestRole() {
        return plugin.getRoles().getGuestRole();
    }

    public static PlayerRole coopRole() {
        return plugin.getRoles().getCoopRole();
    }

    public static PlayerRole of(int weight) {
        return plugin.getRoles().getPlayerRole(weight);
    }

    public static PlayerRole fromId(int id) {
        return plugin.getRoles().getPlayerRoleFromId(id);
    }

    public static PlayerRole of(String name) {
        return plugin.getRoles().getPlayerRole(name);
    }

    public static String getValuesString() {
        StringBuilder stringBuilder = new StringBuilder();
        plugin.getRoles().getRoles().forEach(playerRole -> stringBuilder.append(", ").append(playerRole.toString().toLowerCase(Locale.ENGLISH)));
        return stringBuilder.substring(2);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public boolean isHigherThan(PlayerRole role) {
        Preconditions.checkNotNull(role, "playerRole parameter cannot be null.");
        return getWeight() > role.getWeight();
    }

    @Override
    public boolean isLessThan(PlayerRole role) {
        Preconditions.checkNotNull(role, "playerRole parameter cannot be null.");
        return getWeight() < role.getWeight();
    }

    @Override
    public boolean isFirstRole() {
        return getWeight() == 0;
    }

    @Override
    public boolean isLastRole() {
        return getWeight() == lastRole().getWeight();
    }

    @Override
    public boolean isRoleLadder() {
        return getWeight() >= 0 && (getPreviousRole() != null || getNextRole() != null);
    }

    @Override
    public PlayerRole getNextRole() {
        return getWeight() < 0 ? null : plugin.getRoles().getPlayerRole(getWeight() + 1);
    }

    @Override
    public PlayerRole getPreviousRole() {
        return getWeight() <= 0 ? null : plugin.getRoles().getPlayerRole(getWeight() - 1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SPlayerRole that = (SPlayerRole) o;
        return id == that.id;
    }

    @Override
    public String toString() {
        return name;
    }

    public RolePrivilegeNode getDefaultPermissions() {
        return defaultPermissions;
    }

}
