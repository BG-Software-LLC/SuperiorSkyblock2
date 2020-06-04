package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.IslandSettings;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.permissions.PermissionNodeAbstract;
import com.bgsoftware.superiorskyblock.island.permissions.PlayerPermissionNode;
import com.bgsoftware.superiorskyblock.island.permissions.RolePermissionNode;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksProvider;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.utils.exceptions.HandlerLoadException;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.islands.SortingComparators;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class SpawnIsland implements Island {

    private static SuperiorSkyblockPlugin plugin;

    private final PriorityQueue<SuperiorPlayer> playersInside = new PriorityQueue<>(SortingComparators.PLAYER_NAMES_COMPARATOR);
    private final Location center;
    private final int islandSize;
    private final List<IslandFlag> islandSettings;
    private Biome biome = Biome.PLAINS;

    public SpawnIsland(SuperiorSkyblockPlugin plugin){
        SpawnIsland.plugin = plugin;

        center = LocationUtils.getLocation(plugin.getSettings().spawnLocation.replace(" ", "")).add(0.5, 0, 0.5);
        islandSize = plugin.getSettings().spawnSize;
        islandSettings = plugin.getSettings().spawnSettings.stream().map(IslandFlag::getByName).collect(Collectors.toList());

        if(center.getWorld() == null){
            new HandlerLoadException("The spawn location is in invalid world.", HandlerLoadException.ErrorLevel.SERVER_SHUTDOWN).printStackTrace();
            Bukkit.shutdown();
            return;
        }

        Executor.sync(() -> biome = getCenter(World.Environment.NORMAL).getBlock().getBiome());
    }

    @Override
    public SuperiorPlayer getOwner() {
        return null;
    }

    @Override
    public long getCreationTime() {
        return -1;
    }

    @Override
    public String getCreationTimeDate() {
        return "";
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
    public List<SuperiorPlayer> getUniqueVisitors() {
        return new ArrayList<>();
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
    public List<SuperiorPlayer> getInvitedPlayers() {
        return new ArrayList<>();
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
    public List<SuperiorPlayer> getCoopPlayers() {
        return new ArrayList<>();
    }

    @Override
    public void setPlayerInside(SuperiorPlayer superiorPlayer, boolean inside) {
        if(inside)
            playersInside.add(superiorPlayer);
        else
            playersInside.remove(superiorPlayer);
    }

    @Override
    public boolean isVisitor(SuperiorPlayer superiorPlayer, boolean includeCoopStatus) {
        return true;
    }

    @Override
    @Deprecated
    public Location getCenter() {
        return getCenter(World.Environment.NORMAL);
    }

    @Override
    public Location getCenter(World.Environment environment) {
        return center.clone();
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
    public Location getMinimumProtected() {
        return getMinimum();
    }

    @Override
    public Location getMaximum() {
        int islandDistance = plugin.getSettings().maxIslandSize;
        return getCenter(World.Environment.NORMAL).add(islandDistance, 0, islandDistance);
    }

    @Override
    public Location getMaximumProtected() {
        return getMaximum();
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
        return getAllChunks(environment, onlyProtected, false);
    }

    @Override
    public List<Chunk> getAllChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks) {
        Location min = onlyProtected ? getMinimumProtected() : getMinimum();
        Location max = onlyProtected ? getMaximumProtected() : getMaximum();
        Chunk minChunk = min.getChunk(), maxChunk = max.getChunk();
        World world = center.getWorld();

        List<Chunk> chunks = new ArrayList<>();

        for(int x = minChunk.getX(); x <= maxChunk.getX(); x++){
            for(int z = minChunk.getZ(); z <= maxChunk.getZ(); z++){
                if(!noEmptyChunks || ChunksTracker.isMarkedDirty(this, world, x, z))
                    chunks.add(minChunk.getWorld().getChunkAt(x, z));
            }
        }


        return chunks;
    }

    @Override
    public List<Chunk> getLoadedChunks(boolean onlyProtected, boolean noEmptyChunks) {
        return getLoadedChunks(World.Environment.NORMAL, onlyProtected, noEmptyChunks);
    }

    @Override
    public List<Chunk> getLoadedChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks) {
        World world = center.getWorld();
        Location min = onlyProtected ? getMinimumProtected() : getMinimum();
        Location max = onlyProtected ? getMaximumProtected() : getMaximum();

        List<Chunk> chunks = new ArrayList<>();

        for(int chunkX = min.getBlockX() >> 4; chunkX <= max.getBlockX() >> 4; chunkX++){
            for(int chunkZ = min.getBlockZ() >> 4; chunkZ <= max.getBlockZ() >> 4; chunkZ++){
                if(world.isChunkLoaded(chunkX, chunkZ) && (!noEmptyChunks || ChunksTracker.isMarkedDirty(this, world, chunkX, chunkZ))){
                    chunks.add(world.getChunkAt(chunkX, chunkZ));
                }
            }
        }

        return chunks;
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected, BiConsumer<Chunk, Throwable> whenComplete) {
        return getAllChunksAsync(environment, onlyProtected, false, whenComplete);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected, Consumer<Chunk> onChunkLoad) {
        return getAllChunksAsync(environment, onlyProtected, false, onChunkLoad);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks, BiConsumer<Chunk, Throwable> whenComplete) {
        List<CompletableFuture<Chunk>> chunks = new ArrayList<>();

        Location min = onlyProtected ? getMinimumProtected() : getMinimum();
        Location max = onlyProtected ? getMaximumProtected() : getMaximum();
        World world = min.getWorld();

        for(int x = min.getBlockX() >> 4; x <= max.getBlockX() >> 4; x++){
            for(int z = min.getBlockZ() >> 4; z <= max.getBlockZ() >> 4; z++){
                if(!noEmptyChunks || ChunksTracker.isMarkedDirty(this, world, x, z)) {
                    if (whenComplete != null)
                        chunks.add(ChunksProvider.loadChunk(world, x, z, null).whenComplete(whenComplete));
                    else
                        chunks.add(ChunksProvider.loadChunk(world, x, z, null));
                }
            }
        }

        return chunks;
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks, Consumer<Chunk> onChunkLoad) {
        List<CompletableFuture<Chunk>> chunks = new ArrayList<>();

        Location min = onlyProtected ? getMinimumProtected() : getMinimum();
        Location max = onlyProtected ? getMaximumProtected() : getMaximum();
        World world = min.getWorld();

        for(int x = min.getBlockX() >> 4; x <= max.getBlockX() >> 4; x++){
            for(int z = min.getBlockZ() >> 4; z <= max.getBlockZ() >> 4; z++){
                if(!noEmptyChunks || ChunksTracker.isMarkedDirty(this, world, x, z)) {
                    chunks.add(ChunksProvider.loadChunk(world, x, z, onChunkLoad));
                }
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
        return isInsideRange(location, 0);
    }

    @Override
    public boolean isInsideRange(Location location, int extraRadius) {
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
    public boolean isNetherEnabled() {
        return false;
    }

    @Override
    public void setNetherEnabled(boolean enabled) {

    }

    @Override
    public boolean isEndEnabled() {
        return false;
    }

    @Override
    public void setEndEnabled(boolean enabled) {

    }

    @Override
    @Deprecated
    public boolean hasPermission(CommandSender sender, IslandPermission islandPermission) {
        return hasPermission(sender, IslandPrivilege.getByName(islandPermission.name()));
    }

    @Override
    @Deprecated
    public boolean hasPermission(SuperiorPlayer superiorPlayer, IslandPermission islandPermission) {
        return hasPermission(superiorPlayer, IslandPrivilege.getByName(islandPermission.name()));
    }

    @Override
    public boolean hasPermission(CommandSender sender, IslandPrivilege islandPrivilege) {
        return sender instanceof ConsoleCommandSender || hasPermission(SSuperiorPlayer.of(sender), islandPrivilege);
    }

    @Override
    public boolean hasPermission(SuperiorPlayer superiorPlayer, IslandPrivilege islandPrivilege) {
        boolean checkForProtection = islandPrivilege != IslandPrivileges.FLY;
        return (checkForProtection && !plugin.getSettings().spawnProtection) || superiorPlayer.hasBypassModeEnabled() ||
                superiorPlayer.hasPermissionWithoutOP("superior.admin.bypass." + islandPrivilege.getName()) ||
                hasPermission(SPlayerRole.guestRole(), islandPrivilege);
    }

    @Override
    public boolean hasPermission(PlayerRole playerRole, IslandPrivilege islandPrivilege) {
        return getRequiredPlayerRole(islandPrivilege).getWeight() <= playerRole.getWeight();
    }

    @Override
    @Deprecated
    public void setPermission(PlayerRole playerRole, IslandPermission islandPermission, boolean value) {

    }

    @Override
    @Deprecated
    public void setPermission(SuperiorPlayer superiorPlayer, IslandPermission islandPermission, boolean value) {

    }

    @Override
    public void setPermission(PlayerRole playerRole, IslandPrivilege islandPrivilege, boolean value) {

    }

    @Override
    public void setPermission(SuperiorPlayer superiorPlayer, IslandPrivilege islandPrivilege, boolean value) {

    }

    @Override
    public PermissionNodeAbstract getPermissionNode(PlayerRole playerRole) {
        SuperiorSkyblockPlugin.log("&cIt seems like a plugin developer is using a deprecated method. Please inform him about it.");
        new Throwable().printStackTrace();
        return RolePermissionNode.EmptyRolePermissionNode.INSTANCE;
    }

    @Override
    public PermissionNodeAbstract getPermissionNode(SuperiorPlayer superiorPlayer) {
        return PlayerPermissionNode.EmptyPlayerPermissionNode.INSTANCE;
    }

    @Override
    @Deprecated
    public PlayerRole getRequiredPlayerRole(IslandPermission islandPermission) {
        return getRequiredPlayerRole(IslandPrivilege.getByName(islandPermission.name()));
    }

    @Override
    public PlayerRole getRequiredPlayerRole(IslandPrivilege islandPrivilege) {
        return plugin.getSettings().spawnPermissions.contains(islandPrivilege.getName()) ?
                SPlayerRole.guestRole() : SPlayerRole.lastRole();
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
    public String getRawName() {
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

    }

    @Override
    public boolean isBeingRecalculated() {
        return false;
    }

    @Override
    public void updateLastTime() {

    }

    @Override
    public long getLastTimeUpdate() {
        return -1;
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
    public Map<Key, Integer> getBlockCounts() {
        return new HashMap<>();
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
    public BigDecimal getBonusWorth() {
        return BigDecimal.ZERO;
    }

    @Override
    public void setBonusWorth(BigDecimal bonusWorth) {

    }

    @Override
    public BigDecimal getBonusLevel() {
        return BigDecimal.ZERO;
    }

    @Override
    public void setBonusLevel(BigDecimal bonusLevel) {

    }

    @Override
    @Deprecated
    public BigDecimal getIslandLevelAsBigDecimal() {
        return getIslandLevel();
    }

    @Override
    public BigDecimal getIslandLevel() {
        return getRawLevel();
    }

    @Override
    public BigDecimal getRawLevel() {
        return BigDecimal.ZERO;
    }

    @Override
    public int getUpgradeLevel(String upgradeName) {
        return 0;
    }

    @Override
    public UpgradeLevel getUpgradeLevel(Upgrade upgrade) {
        return upgrade.getUpgradeLevel(1);
    }

    @Override
    public void setUpgradeLevel(String upgradeName, int level) {

    }

    @Override
    public void setUpgradeLevel(Upgrade upgrade, int level) {

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
        return SIsland.NO_LIMIT;
    }

    @Override
    public int getExactBlockLimit(Key key) {
        return SIsland.NO_LIMIT;
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
    public int getEntityLimit(EntityType entityType) {
        return SIsland.NO_LIMIT;
    }

    @Override
    public Map<EntityType, Integer> getEntitiesLimits() {
        return new HashMap<>();
    }

    @Override
    public void setEntityLimit(EntityType entityType, int limit) {

    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(EntityType entityType) {
        return hasReachedEntityLimit(entityType, 1);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(EntityType entityType, int amount) {
        return CompletableFuture.completedFuture(false);
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
    public boolean canCompleteMissionAgain(Mission mission) {
        return false;
    }

    @Override
    public int getAmountMissionCompleted(Mission mission) {
        return 0;
    }

    @Override
    public List<Mission> getCompletedMissions() {
        return new ArrayList<>();
    }

    @Override
    @Deprecated
    public boolean hasSettingsEnabled(IslandSettings islandSettings) {
        return hasSettingsEnabled(IslandFlag.getByName(islandSettings.name()));
    }

    @Override
    @Deprecated
    public void enableSettings(IslandSettings islandSettings) {

    }

    @Override
    @Deprecated
    public void disableSettings(IslandSettings islandSettings) {

    }

    @Override
    public boolean hasSettingsEnabled(IslandFlag islandFlag) {
        return this.islandSettings.contains(islandFlag);
    }

    @Override
    public void enableSettings(IslandFlag islandFlag) {

    }

    @Override
    public void disableSettings(IslandFlag islandFlag) {

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
        return null;
    }

    @Override
    public void setGeneratorAmount(Key key, int amount) {

    }

    @Override
    public int getGeneratorAmount(Key key) {
        return 0;
    }

    @Override
    public int getGeneratorTotalAmount() {
        return 0;
    }

    @Override
    public Map<String, Integer> getGeneratorAmounts() {
        return new HashMap<>();
    }

    @Override
    public String[] getGeneratorArray() {
        return new String[0];
    }

    @Override
    public void clearGeneratorAmounts() {

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

}
