package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.island.permissions.RolePermissionNode;
import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public final class SPlayerRole implements PlayerRole {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final String name;
    private final int id, weight;
    private final RolePermissionNode defaultPermissions;

    public SPlayerRole(String name, int id, int weight, List<String> defaultPermissions, SPlayerRole previousRole){
        this.name = name;
        this.id = id;
        this.weight = weight;

        StringBuilder permissions = new StringBuilder();
        defaultPermissions.forEach(perm -> permissions.append(";").append(perm));

        this.defaultPermissions = new RolePermissionNode(null,
                previousRole == null ? RolePermissionNode.EmptyRolePermissionNode.INSTANCE : previousRole.defaultPermissions,
                permissions.length() == 0 ? "" : permissions.substring(1));
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
    public String toString() {
        return name;
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

    public RolePermissionNode getDefaultPermissions() {
        return defaultPermissions;
    }

    public static PlayerRole defaultRole(){
        return plugin.getRoles().getDefaultRole();
    }

    public static PlayerRole lastRole(){
        return plugin.getRoles().getLastRole();
    }

    public static PlayerRole guestRole(){
        return plugin.getRoles().getGuestRole();
    }

    public static PlayerRole coopRole(){
        return plugin.getRoles().getCoopRole();
    }

    public static PlayerRole of(int weight){
        return plugin.getRoles().getPlayerRole(weight);
    }

    public static PlayerRole fromId(int id){
        return plugin.getRoles().getPlayerRoleFromId(id);
    }

    public static PlayerRole of(String name){
        return plugin.getRoles().getPlayerRole(name);
    }

    public static String getValuesString(){
        StringBuilder stringBuilder = new StringBuilder();
        plugin.getRoles().getRoles().forEach(playerRole -> stringBuilder.append(", ").append(playerRole.toString().toLowerCase()));
        return stringBuilder.substring(2);
    }

}
