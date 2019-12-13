package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.IslandSettings;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.islands.SortingComparators;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.stream.Collectors;

public final class SpawnIsland implements Island {

    private static SuperiorSkyblockPlugin plugin;

    private final PriorityQueue<SuperiorPlayer> playersInside = new PriorityQueue<>(SortingComparators.PLAYER_NAMES_COMPARATOR);
    private final Map<Object, SPermissionNode> permissionNodes = new HashMap<>();
    private final SBlockPosition center;
    private final int islandSize;
    private final List<IslandSettings> islandSettings;
    private Biome biome = Biome.PLAINS;

    public SpawnIsland(SuperiorSkyblockPlugin plugin){
        SpawnIsland.plugin = plugin;

        center = SBlockPosition.of(plugin.getSettings().spawnLocation);
        islandSize = plugin.getSettings().maxIslandSize;
        islandSettings = plugin.getSettings().spawnSettings.stream().map(IslandSettings::valueOf).collect(Collectors.toList());

        assignPermissionNodes();

        Executor.sync(() -> biome = getCenter(World.Environment.NORMAL).getBlock().getBiome());
    }

    @Override
    public SuperiorPlayer getOwner() {
        return null;
    }

    @Override
    public List<SuperiorPlayer> getIslandMembers(boolean includeOwner) {
        return new ArrayList<>();
    }

    @Override
    public List<SuperiorPlayer> getBannedPlayers() {
        return new ArrayList<>();
    }

    @Override
    public List<SuperiorPlayer> getIslandVisitors() {
        return new ArrayList<>();
    }

    @Override
    public List<SuperiorPlayer> getAllPlayersInside() {
        return new ArrayList<>(playersInside);
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
    public void setPlayerInside(SuperiorPlayer superiorPlayer, boolean inside) {
        if(inside)
            playersInside.add(superiorPlayer);
        else
            playersInside.remove(superiorPlayer);
    }

    @Override
    @Deprecated
    public Location getCenter() {
        return getCenter(World.Environment.NORMAL);
    }

    @Override
    public Location getCenter(World.Environment environment) {
        return center.parse().add(0.5, 0, 0.5);
    }

    @Override
    @Deprecated
    public Location getTeleportLocation() {
        return getCenter(World.Environment.NORMAL);
    }

    @Override
    public Location getTeleportLocation(World.Environment environment) {
        return getCenter(environment);
    }

    @Override
    public Location getVisitorsLocation() {
        return getCenter(World.Environment.NORMAL);
    }

    @Override
    public void setTeleportLocation(Location teleportLocation) {

    }

    @Override
    public void setVisitorsLocation(Location visitorsLocation) {

    }

    @Override
    public Location getMinimum() {
        int islandDistance = plugin.getSettings().maxIslandSize;
        return getCenter(World.Environment.NORMAL).subtract(islandDistance, 0, islandDistance);
    }

    @Override
    public Location getMaximum() {
        int islandDistance = plugin.getSettings().maxIslandSize;
        return getCenter(World.Environment.NORMAL).add(islandDistance, 0, islandDistance);
    }

    @Override
    public List<Chunk> getAllChunks() {
        return getAllChunks(false);
    }

    @Override
    public List<Chunk> getAllChunks(boolean onlyProtected) {
        return getAllChunks(World.Environment.NORMAL, onlyProtected);
    }

    @Override
    public List<Chunk> getAllChunks(World.Environment environment) {
        return getAllChunks(environment, false);
    }

    @Override
    public List<Chunk> getAllChunks(World.Environment environment, boolean onlyProtected) {
        int islandSize = getIslandSize();
        Location min = onlyProtected ? center.parse().subtract(islandSize, 0, islandSize) : getMinimum();
        Location max = onlyProtected ? center.parse().add(islandSize, 0, islandSize) : getMaximum();
        Chunk minChunk = min.getChunk(), maxChunk = max.getChunk();

        List<Chunk> chunks = new ArrayList<>();

        for(int x = minChunk.getX(); x <= maxChunk.getX(); x++){
            for(int z = minChunk.getZ(); z <= maxChunk.getZ(); z++){
                chunks.add(minChunk.getWorld().getChunkAt(x, z));
            }
        }


        return chunks;
    }

    @Override
    public boolean isInside(Location location) {
        if(!location.getWorld().equals(getCenter(World.Environment.NORMAL).getWorld()))
            return false;

        Location min = getMinimum(), max = getMaximum();
        return min.getBlockX() <= location.getBlockX() && min.getBlockZ() <= location.getBlockZ() &&
                max.getBlockX() >= location.getBlockX() && max.getBlockZ() >= location.getBlockZ();
    }

    @Override
    public boolean isInsideRange(Location location) {
        return isInside(location);
    }

    @Override
    public boolean isInsideRange(Chunk chunk) {
        if(!chunk.getWorld().equals(getCenter(World.Environment.NORMAL).getWorld()))
            return false;

        Location min = getMinimum(), max = getMaximum();
        return (min.getBlockX() >> 4) <= chunk.getX() && (min.getBlockZ() >> 4) <= chunk.getZ() &&
                (max.getBlockX() >> 4) >= chunk.getX() && (max.getBlockZ() >> 4) >= chunk.getZ();
    }

    @Override
    public boolean hasPermission(CommandSender sender, IslandPermission islandPermission) {
        return sender instanceof ConsoleCommandSender || hasPermission(SSuperiorPlayer.of(sender), islandPermission);
    }

    @Override
    public boolean hasPermission(SuperiorPlayer superiorPlayer, IslandPermission islandPermission) {
        return !plugin.getSettings().spawnProtection || superiorPlayer.hasBypassModeEnabled() ||
                superiorPlayer.hasPermissionWithoutOP("superior.admin.bypass." + islandPermission) ||
                getPermissionNode(superiorPlayer).hasPermission(islandPermission);
    }

    @Override
    public void setPermission(PlayerRole playerRole, IslandPermission islandPermission, boolean value) {

    }

    @Override
    public void setPermission(SuperiorPlayer superiorPlayer, IslandPermission islandPermission, boolean value) {

    }

    @Override
    public SPermissionNode getPermissionNode(PlayerRole playerRole) {
        return permissionNodes.get(playerRole);
    }

    @Override
    public SPermissionNode getPermissionNode(SuperiorPlayer superiorPlayer) {
        PlayerRole playerRole = isMember(superiorPlayer) ? superiorPlayer.getPlayerRole() : isCoop(superiorPlayer) ? SPlayerRole.coopRole() : SPlayerRole.guestRole();
        return permissionNodes.getOrDefault(superiorPlayer.getUniqueId(), getPermissionNode(playerRole));
    }

    @Override
    public PlayerRole getRequiredPlayerRole(IslandPermission islandPermission) {
        return plugin.getPlayers().getRoles().stream()
                .filter(playerRole -> getPermissionNode(playerRole).hasPermission(islandPermission))
                .min(Comparator.comparingInt(PlayerRole::getWeight)).orElse(SPlayerRole.guestRole());
    }

    @Override
    public boolean isSpawn() {
        return true;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void setName(String islandName) {

    }

    @Override
    public String getDescription() {
        return "";
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
    public void calcIslandWorth(SuperiorPlayer asker, Runnable callback) {

    }

    @Override
    public void updateBorder() {
        getAllPlayersInside().forEach(superiorPlayer -> plugin.getNMSAdapter().setWorldBorder(superiorPlayer, this));
    }

    @Override
    public int getIslandSize() {
        return islandSize;
    }

    @Override
    public void setIslandSize(int islandSize) {

    }

    @Override
    public String getDiscord() {
        return "";
    }

    @Override
    public void setDiscord(String discord) {

    }

    @Override
    public String getPaypal() {
        return "";
    }

    @Override
    public void setPaypal(String paypal) {

    }

    @Override
    public Biome getBiome() {
        return biome;
    }

    @Override
    public void setBiome(Biome biome) {

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
    public void sendMessage(String message, UUID... ignoredMembers) {
        List<UUID> ignoredList = Arrays.asList(ignoredMembers);

        getIslandMembers(true).stream()
                .filter(superiorPlayer -> !ignoredList.contains(superiorPlayer.getUniqueId()) && superiorPlayer.isOnline())
                .forEach(superiorPlayer -> Locale.sendMessage(superiorPlayer, message));
    }

    @Override
    @Deprecated
    public BigDecimal getMoneyInBankAsBigDecimal() {
        return getMoneyInBank();
    }

    @Override
    public BigDecimal getMoneyInBank() {
        return BigDecimal.ZERO;
    }

    @Override
    public void depositMoney(double amount) {

    }

    @Override
    public void withdrawMoney(double amount) {

    }

    @Override
    public void handleBlockPlace(Block block) {

    }

    @Override
    public void handleBlockPlace(Block block, int amount) {

    }

    @Override
    public void handleBlockPlace(Block block, int amount, boolean save) {

    }

    @Override
    public void handleBlockPlace(Key key, int amount) {

    }

    @Override
    public void handleBlockPlace(Key key, int amount, boolean save) {

    }

    @Override
    public void handleBlockBreak(Block block) {

    }

    @Override
    public void handleBlockBreak(Block block, int amount) {

    }

    @Override
    public void handleBlockBreak(Block block, int amount, boolean save) {

    }

    @Override
    public void handleBlockBreak(Key key, int amount) {

    }

    @Override
    public void handleBlockBreak(Key key, int amount, boolean save) {

    }

    @Override
    public int getBlockCount(Key key) {
        return 0;
    }

    @Override
    public int getExactBlockCount(Key key) {
        return 0;
    }

    @Override
    @Deprecated
    public BigDecimal getWorthAsBigDecimal() {
        return getWorth();
    }

    @Override
    public BigDecimal getWorth() {
        return BigDecimal.ZERO;
    }

    @Override
    @Deprecated
    public BigDecimal getRawWorthAsBigDecimal() {
        return getRawWorth();
    }

    @Override
    public BigDecimal getRawWorth() {
        return BigDecimal.ZERO;
    }

    @Override
    public void setBonusWorth(BigDecimal bonusWorth) {

    }

    @Override
    @Deprecated
    public BigDecimal getIslandLevelAsBigDecimal() {
        return getIslandLevel();
    }

    @Override
    public BigDecimal getIslandLevel() {
        return BigDecimal.ZERO;
    }

    @Override
    public int getUpgradeLevel(String upgradeName) {
        return 0;
    }

    @Override
    public void setUpgradeLevel(String upgradeName, int level) {

    }

    @Override
    public double getCropGrowthMultiplier() {
        return 1;
    }

    @Override
    public void setCropGrowthMultiplier(double cropGrowth) {

    }

    @Override
    public double getSpawnerRatesMultiplier() {
        return 1;
    }

    @Override
    public void setSpawnerRatesMultiplier(double spawnerRates) {

    }

    @Override
    public double getMobDropsMultiplier() {
        return 1;
    }

    @Override
    public void setMobDropsMultiplier(double mobDrops) {

    }

    @Override
    public int getBlockLimit(Key key) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getExactBlockLimit(Key key) {
        return Integer.MAX_VALUE;
    }

    @Override
    public Map<Key, Integer> getBlocksLimits() {
        return new HashMap<>();
    }

    @Override
    public void setBlockLimit(Key key, int limit) {

    }

    @Override
    public boolean hasReachedBlockLimit(Key key) {
        return false;
    }

    @Override
    public boolean hasReachedBlockLimit(Key key, int amount) {
        return false;
    }

    @Override
    public int getTeamLimit() {
        return 0;
    }

    @Override
    public void setTeamLimit(int teamLimit) {

    }

    @Override
    public int getWarpsLimit() {
        return 0;
    }

    @Override
    public void setWarpsLimit(int warpsLimit) {

    }

    @Override
    public Location getWarpLocation(String name) {
        return null;
    }

    @Override
    public boolean isWarpPrivate(String name) {
        return false;
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
    public List<String> getAllWarps() {
        return new ArrayList<>();
    }

    @Override
    public boolean hasMoreWarpSlots() {
        return false;
    }

    @Override
    public Rating getRating(SuperiorPlayer superiorPlayer) {
        return Rating.UNKNOWN;
    }

    @Override
    public void setRating(SuperiorPlayer superiorPlayer, Rating rating) {

    }

    @Override
    public double getTotalRating() {
        return 0;
    }

    @Override
    public int getRatingAmount() {
        return 0;
    }

    @Override
    public Map<UUID, Rating> getRatings() {
        return new HashMap<>();
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
    public List<Mission> getCompletedMissions() {
        return new ArrayList<>();
    }

    @Override
    public boolean hasSettingsEnabled(IslandSettings islandSettings) {
        return this.islandSettings.contains(islandSettings);
    }

    @Override
    public void enableSettings(IslandSettings islandSettings) {

    }

    @Override
    public void disableSettings(IslandSettings islandSettings) {

    }

    @Override
    public void setGeneratorPercentage(Key key, int percentage) {

    }

    @Override
    public int getGeneratorPercentage(Key key) {
        return 0;
    }

    @Override
    public Map<String, Integer> getGeneratorPercentages() {
        return new HashMap<>();
    }

    @Override
    public boolean wasSchematicGenerated(World.Environment environment) {
        return true;
    }

    @Override
    public void setSchematicGenerate(World.Environment environment) {

    }

    @Override
    public String getSchematicName() {
        return "";
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Island o) {
        return 0;
    }

    private void assignPermissionNodes(){
        for(PlayerRole playerRole : plugin.getPlayers().getRoles()) {
            if(!permissionNodes.containsKey(playerRole)) {
                PlayerRole previousRole = SPlayerRole.of(playerRole.getWeight() - 1);
                permissionNodes.put(playerRole, new SPermissionNode(((SPlayerRole) playerRole).getDefaultPermissions(), permissionNodes.get(previousRole)));
            }
        }
    }

}
