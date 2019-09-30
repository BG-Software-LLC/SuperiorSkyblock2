package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public final class SPlayerRole implements PlayerRole {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private String name;
    private int weight;
    private List<String> defaultPermissions;

    public SPlayerRole(String name, int weight, List<String> defaultPermissions){
        this.name = name;
        this.weight = weight;
        this.defaultPermissions = defaultPermissions;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public boolean isHigherThan(PlayerRole role) {
        return getWeight() > role.getWeight();
    }

    @Override
    public boolean isLessThan(PlayerRole role) {
        return getWeight() < role.getWeight();
    }

    @Override
    public boolean isFirstRole() {
        return getPreviousRole() == null && getNextRole() != null;
    }

    @Override
    public boolean isLastRole() {
        return getPreviousRole() != null && getNextRole() == null;
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

    public List<String> getDefaultPermissions() {
        List<String> permissions = new ArrayList<>(defaultPermissions);
        if(getPreviousRole() != null)
            permissions.addAll(((SPlayerRole) getPreviousRole()).getDefaultPermissions());
        return permissions;
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

    public static PlayerRole of(String name){
        return plugin.getPlayers().getPlayerRole(name);
    }

    public static String getValuesString(){
        StringBuilder stringBuilder = new StringBuilder();
        plugin.getPlayers().getRoles().forEach(playerRole -> stringBuilder.append(", ").append(playerRole.toString().toLowerCase()));
        return stringBuilder.toString().substring(2);
    }

}
