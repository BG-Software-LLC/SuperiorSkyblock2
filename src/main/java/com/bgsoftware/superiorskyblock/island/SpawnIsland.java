package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.IslandSettings;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.math.BigDecimal;
import java.util.UUID;

public final class SpawnIsland extends SIsland {

    private static SuperiorSkyblockPlugin plugin;

    private Location center;
    private String world;
    private int islandSize;

    public SpawnIsland(SuperiorSkyblockPlugin plugin) {
        super(null, SBlockPosition.of(plugin.getSettings().spawnLocation), "");
        SpawnIsland.plugin = plugin;

        String[] loc = plugin.getSettings().spawnLocation.split(", ");
        this.world = loc[0];
        double x = ((int) Double.parseDouble(loc[1])) + 0.5;
        double y = Integer.parseInt(loc[2]);
        double z = ((int) Double.parseDouble(loc[3])) + 0.5;
        float yaw = loc.length == 6 ? Float.parseFloat(loc[4]) : 0;
        float pitch = loc.length == 6 ? Float.parseFloat(loc[5]) : 0;
        this.center = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);

        this.islandSize = plugin.getSettings().maxIslandSize;
    }

    @Override
    public Location getCenter() {
        if(center.getWorld() == null)
            center.setWorld(Bukkit.getWorld(world));

        return center.clone();
    }

    @Override
    public Location getTeleportLocation() {
        return getCenter();
    }

    @Override
    public SuperiorPlayer getOwner() {
        return null;
    }

    @Override
    public void inviteMember(SuperiorPlayer superiorPlayer) {

    }

    @Override
    public void revokeInvite(SuperiorPlayer superiorPlayer) {

    }

    @Override
    public boolean isInvited(SuperiorPlayer superiorPlayer) {
        return false;
    }

    @Override
    public void addMember(SuperiorPlayer superiorPlayer, PlayerRole playerRole) {

    }

    @Override
    public void kickMember(SuperiorPlayer superiorPlayer) {

    }

    @Override
    public boolean isMember(SuperiorPlayer superiorPlayer) {
        return false;
    }

    @Override
    public void banMember(SuperiorPlayer superiorPlayer) {

    }

    @Override
    public void unbanMember(SuperiorPlayer superiorPlayer) {

    }

    @Override
    public boolean isBanned(SuperiorPlayer superiorPlayer) {
        return false;
    }

    @Override
    public void addCoop(SuperiorPlayer superiorPlayer) {

    }

    @Override
    public void removeCoop(SuperiorPlayer superiorPlayer) {

    }

    @Override
    public boolean isCoop(SuperiorPlayer superiorPlayer) {
        return false;
    }

    @Override
    public void setTeleportLocation(Location teleportLocation) {

    }

    @Override
    public void setVisitorsLocation(Location visitorsLocation) {

    }

    @Override
    public boolean hasPermission(SuperiorPlayer superiorPlayer, IslandPermission islandPermission) {
        return !plugin.getSettings().spawnProtection || super.hasPermission(superiorPlayer, islandPermission);
    }

    @Override
    public void setPermission(PlayerRole playerRole, IslandPermission islandPermission, boolean value) {

    }

    @Override
    public void setPermission(SuperiorPlayer superiorPlayer, IslandPermission islandPermission, boolean value) {

    }

    @Override
    public boolean isSpawn() {
        return true;
    }

    @Override
    public void setName(String islandName) {

    }

    @Override
    public void setDescription(String description) {

    }

    @Override
    public void disbandIsland() {

    }

    @Override
    public boolean transferIsland(SuperiorPlayer superiorPlayer) {
        return false;
    }

    @Override
    public void calcIslandWorth(SuperiorPlayer asker) {

    }

    @Override
    public int getIslandSize() {
        return islandSize;
    }

    @Override
    public void setIslandSize(int islandSize) {

    }

    @Override
    public void setDiscord(String discord) {

    }

    @Override
    public void setPaypal(String paypal) {

    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public void setLocked(boolean locked) {

    }

    @Override
    public boolean isIgnored() {
        return false;
    }

    @Override
    public void setIgnored(boolean ignored) {

    }

    @Override
    public void depositMoney(double amount) {

    }

    @Override
    public void withdrawMoney(double amount) {

    }

    @Override
    public void handleBlockBreak(Key key, int amount, boolean save) {

    }

    @Override
    public void setBonusWorth(BigDecimal bonusWorth) {

    }

    @Override
    public void setUpgradeLevel(String upgradeName, int level) {

    }

    @Override
    public void setCropGrowthMultiplier(double cropGrowth) {

    }

    @Override
    public void setSpawnerRatesMultiplier(double spawnerRates) {

    }

    @Override
    public void setMobDropsMultiplier(double mobDrops) {

    }

    @Override
    public void setBlockLimit(Key key, int limit) {

    }

    @Override
    public void setTeamLimit(int teamLimit) {

    }

    @Override
    public void setWarpsLimit(int warpsLimit) {

    }

    @Override
    public void setWarpLocation(String name, Location location, boolean privateFlag) {

    }

    @Override
    public void warpPlayer(SuperiorPlayer superiorPlayer, String warp) {

    }

    @Override
    public void deleteWarp(SuperiorPlayer superiorPlayer, Location location) {

    }

    @Override
    public void deleteWarp(String name) {

    }

    @Override
    public void setRating(UUID uuid, Rating rating) {

    }

    @Override
    public void setRating(SuperiorPlayer superiorPlayer, Rating rating) {

    }

    @Override
    public void completeMission(Mission mission) {

    }

    @Override
    public void resetMission(Mission mission) {

    }

    @Override
    public boolean hasCompletedMission(Mission mission) {
        return false;
    }

    @Override
    public boolean hasSettingsEnabled(IslandSettings islandSettings) {
        return false;
    }

    @Override
    public void enableSettings(IslandSettings islandSettings) {

    }

    @Override
    public void disableSettings(IslandSettings islandSettings) {

    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SpawnIsland;
    }
}
