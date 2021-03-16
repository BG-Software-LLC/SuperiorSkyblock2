package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.island.permissions.RolePermissionNode;
import com.google.common.base.Preconditions;

import java.util.List;

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
        return getWeight() < 0 ? null : plugin.getPlayers().getPlayerRole(getWeight() + 1);
    }

    @Override
    public PlayerRole getPreviousRole() {
        return getWeight() <= 0 ? null : plugin.getPlayers().getPlayerRole(getWeight() - 1);
    }

    @Override
    public String toString() {
        return name;
    }

    public RolePermissionNode getDefaultPermissions() {
        return defaultPermissions;
    }

    public static PlayerRole defaultRole(){
        return plugin.getPlayers().getDefaultRole();
    }

    public static PlayerRole lastRole(){
        return plugin.getPlayers().getLastRole();
    }

    public static PlayerRole guestRole(){
        return plugin.getPlayers().getGuestRole();
    }

    public static PlayerRole coopRole(){
        return plugin.getPlayers().getCoopRole();
    }

    public static PlayerRole of(int weight){
        return plugin.getPlayers().getPlayerRole(weight);
    }

    public static PlayerRole fromId(int id){
        return plugin.getPlayers().getPlayerRoleFromId(id);
    }

    public static PlayerRole of(String name){
        return plugin.getPlayers().getPlayerRole(name);
    }

    public static String getValuesString(){
        StringBuilder stringBuilder = new StringBuilder();
        plugin.getPlayers().getRoles().forEach(playerRole -> stringBuilder.append(", ").append(playerRole.toString().toLowerCase()));
        return stringBuilder.toString().substring(2);
    }

}
