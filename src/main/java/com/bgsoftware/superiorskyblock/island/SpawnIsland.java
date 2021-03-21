package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.IslandDataHandler;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PermissionNode;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.island.bank.IslandBank;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.hooks.SWMHook;
import com.bgsoftware.superiorskyblock.island.data.SEmptyIslandDataHandler;
import com.bgsoftware.superiorskyblock.island.permissions.PermissionNodeAbstract;
import com.bgsoftware.superiorskyblock.island.permissions.PlayerPermissionNode;
import com.bgsoftware.superiorskyblock.player.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.utils.exceptions.HandlerLoadException;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.islands.SortingComparators;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.locations.SmartLocation;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class SpawnIsland implements Island {

    private static final UUID spawnUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final SSuperiorPlayer ownerPlayer = new SSuperiorPlayer(spawnUUID);
    private static SuperiorSkyblockPlugin plugin;

    private final PriorityQueue<SuperiorPlayer> playersInside = new PriorityQueue<>(SortingComparators.PLAYER_NAMES_COMPARATOR);
    private final Location center;
    private final int islandSize;
    private final List<IslandFlag> islandSettings;
    private Biome biome = Biome.PLAINS;

    public SpawnIsland(SuperiorSkyblockPlugin plugin){
        SpawnIsland.plugin = plugin;

        String spawnLocation = plugin.getSettings().spawnLocation.replace(" ", "");

        SmartLocation smartCenter = LocationUtils.getLocation(spawnLocation);
        assert smartCenter != null;
        center = smartCenter.add(0.5, 0, 0.5);
        islandSize = plugin.getSettings().spawnSize;
        islandSettings = plugin.getSettings().spawnSettings.stream().map(IslandFlag::getByName).collect(Collectors.toList());

        if(center.getWorld() == null)
            SWMHook.tryWorldLoad(spawnLocation.split(",")[0]);

        if(center.getWorld() == null){
            new HandlerLoadException("The spawn location is in invalid world.", HandlerLoadException.ErrorLevel.SERVER_SHUTDOWN).printStackTrace();
            Bukkit.shutdown();
            return;
        }

        Executor.sync(() -> biome = getCenter(World.Environment.NORMAL).getBlock().getBiome());
    }

    @Override
    public SuperiorPlayer getOwner() {
        return ownerPlayer;
    }

    @Override
    public UUID getUniqueId() {
        return spawnUUID;
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
    public void updateDatesFormatter() {

    }

    @Override
    public List<SuperiorPlayer> getIslandMembers(boolean includeOwner) {
        return new ArrayList<>();
    }

    @Override
    public List<SuperiorPlayer> getIslandMembers(PlayerRole... playerRoles) {
        return new ArrayList<>();
    }

    @Override
    public List<SuperiorPlayer> getBannedPlayers() {
        return new ArrayList<>();
    }

    @Override
    public List<SuperiorPlayer> getIslandVisitors() {
        return getIslandVisitors(true);
    }

    @Override
    public List<SuperiorPlayer> getIslandVisitors(boolean vanishPlayers) {
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
    public List<Pair<SuperiorPlayer, Long>> getUniqueVisitorsWithTimes() {
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
    public int getCoopLimit() {
        return 0;
    }

    @Override
    public int getCoopLimitRaw() {
        return -1;
    }

    @Override
    public void setCoopLimit(int coopLimit) {

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
    public Location getCenter(World.Environment environment) {
        return center.clone();
    }

    @Override
    public Location getTeleportLocation(World.Environment environment) {
        return getCenter(environment);
    }

    @Override
    public Map<World.Environment, Location> getTeleportLocations() {
        Map<World.Environment, Location> map = new HashMap<>();
        map.put(World.Environment.NORMAL, center);
        return map;
    }

    @Override
    public Location getVisitorsLocation() {
        return getCenter(World.Environment.NORMAL);
    }

    @Override
    public void setTeleportLocation(Location teleportLocation) {

    }

    @Override
    public void setTeleportLocation(World.Environment environment, @Nullable Location teleportLocation) {

    }

    @Override
    public void setVisitorsLocation(Location visitorsLocation) {

    }

    @Override
    public Location getMinimum() {
        return getCenter(World.Environment.NORMAL).subtract(islandSize, 0, islandSize);
    }

    @Override
    public Location getMinimumProtected() {
        return getMinimum();
    }

    @Override
    public Location getMaximum() {
        return getCenter(World.Environment.NORMAL).add(islandSize, 0, islandSize);
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
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected, Consumer<Chunk> onChunkLoad) {
        return getAllChunksAsync(environment, onlyProtected, false, onChunkLoad);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks, Consumer<Chunk> onChunkLoad) {
        return IslandUtils.getAllChunksAsync(this, center.getWorld(), onlyProtected, noEmptyChunks, onChunkLoad);
    }

    @Override
    public void resetChunks(World.Environment environment, boolean onlyProtected) {

    }

    @Override
    public void resetChunks(World.Environment environment, boolean onlyProtected, Runnable onFinish) {

    }

    @Override
    public void resetChunks(boolean onlyProtected) {

    }

    @Override
    public void resetChunks(boolean onlyProtected, Runnable onFinish) {

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
    public int getUnlockedWorldsFlag() {
        return 0;
    }

    @Override
    public boolean hasPermission(CommandSender sender, IslandPrivilege islandPrivilege) {
        return sender instanceof ConsoleCommandSender || hasPermission(plugin.getPlayers().getSuperiorPlayer(sender), islandPrivilege);
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
    public void setPermission(PlayerRole playerRole, IslandPrivilege islandPrivilege, boolean value) {

    }

    @Override
    public void resetPermissions() {

    }

    @Override
    public void setPermission(SuperiorPlayer superiorPlayer, IslandPrivilege islandPrivilege, boolean value) {

    }

    @Override
    public void resetPermissions(SuperiorPlayer superiorPlayer) {

    }

    @Override
    public PermissionNodeAbstract getPermissionNode(SuperiorPlayer superiorPlayer) {
        return PlayerPermissionNode.EmptyPlayerPermissionNode.INSTANCE;
    }

    @Override
    public PlayerRole getRequiredPlayerRole(IslandPrivilege islandPrivilege) {
        return plugin.getSettings().spawnPermissions.contains(islandPrivilege.getName()) ?
                SPlayerRole.guestRole() : SPlayerRole.lastRole();
    }

    @Override
    public Map<SuperiorPlayer, PermissionNode> getPlayerPermissions() {
        return new HashMap<>();
    }

    @Override
    public Map<IslandPrivilege, PlayerRole> getRolePermissions() {
        return new HashMap<>();
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
    public void replacePlayers(SuperiorPlayer originalPlayer, SuperiorPlayer newPlayer) {

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
    public void updateIslandFly(SuperiorPlayer superiorPlayer) {
        IslandUtils.updateIslandFly(this, superiorPlayer);
    }

    @Override
    public int getIslandSize() {
        return islandSize;
    }

    @Override
    public int getIslandSizeRaw() {
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
    public void setBiome(Biome biome, boolean updateBlocks) {

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
    public void sendTitle(String title, String subtitle, int fadeIn, int duration, int fadeOut, UUID... ignoredMembers){

    }

    @Override
    public void executeCommand(String command, boolean onlyOnlineMembers, UUID... ignoredMembers) {

    }

    @Override
    public boolean isBeingRecalculated() {
        return false;
    }

    @Override
    public void updateLastTime() {

    }

    @Override
    public void setCurrentlyActive() {

    }

    @Override
    public long getLastTimeUpdate() {
        return -1;
    }

    @Override
    public void setLastTimeUpdate(long lastTimeUpdate) {

    }

    @Override
    public IslandBank getIslandBank() {
        return null;
    }

    @Override
    public BigDecimal getBankLimit() {
        return BigDecimal.valueOf(-1);
    }

    @Override
    public BigDecimal getBankLimitRaw() {
        return BigDecimal.valueOf(-1);
    }

    @Override
    public void setBankLimit(BigDecimal bankLimit) {

    }

    @Override
    public boolean giveInterest(boolean checkOnlineOwner) {
        return false;
    }

    @Override
    public long getLastInterestTime() {
        return -1;
    }

    @Override
    public long getNextInterest() {
        return -1;
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
    public void handleBlockPlace(Key key, BigInteger amount, boolean save) {

    }

    @Override
    public void handleBlocksPlace(Map<Key, Integer> blocks) {

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
    public void handleBlockBreak(Key key, BigInteger amount, boolean save) {

    }

    @Override
    public BigInteger getBlockCountAsBigInteger(Key key) {
        return BigInteger.ZERO;
    }

    @Override
    public Map<Key, BigInteger> getBlockCountsAsBigInteger() {
        return new HashMap<>();
    }

    @Override
    public BigInteger getExactBlockCountAsBigInteger(Key key) {
        return BigInteger.ZERO;
    }

    @Override
    public void clearBlockCounts() {

    }

    @Override
    public BigDecimal getWorth() {
        return BigDecimal.ZERO;
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
    public BigDecimal getIslandLevel() {
        return getRawLevel();
    }

    @Override
    public BigDecimal getRawLevel() {
        return BigDecimal.ZERO;
    }

    @Override
    public UpgradeLevel getUpgradeLevel(Upgrade upgrade) {
        return upgrade.getUpgradeLevel(1);
    }

    @Override
    public void setUpgradeLevel(Upgrade upgrade, int level) {

    }

    @Override
    public Map<String, Integer> getUpgrades() {
        return new HashMap<>();
    }

    @Override
    public void syncUpgrades() {

    }

    @Override
    public void updateUpgrades() {

    }

    @Override
    public long getLastTimeUpgrade() {
        return -1;
    }

    @Override
    public boolean hasActiveUpgradeCooldown() {
        return false;
    }

    @Override
    public double getCropGrowthMultiplier() {
        return 1;
    }

    @Override
    public double getCropGrowthRaw() {
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
    public double getSpawnerRatesRaw() {
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
    public double getMobDropsRaw() {
        return 1;
    }

    @Override
    public void setMobDropsMultiplier(double mobDrops) {

    }

    @Override
    public int getBlockLimit(Key key) {
        return IslandUtils.NO_LIMIT.get();
    }

    @Override
    public int getExactBlockLimit(Key key) {
        return IslandUtils.NO_LIMIT.get();
    }

    @Override
    public Map<Key, Integer> getBlocksLimits() {
        return new HashMap<>();
    }

    @Override
    public Map<Key, Integer> getCustomBlocksLimits() {
        return new HashMap<>();
    }

    @Override
    public void clearBlockLimits() {

    }

    @Override
    public void setBlockLimit(Key key, int limit) {

    }

    @Override
    public void removeBlockLimit(Key key) {

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
        return IslandUtils.NO_LIMIT.get();
    }

    @Override
    public int getEntityLimit(Key key) {
        return IslandUtils.NO_LIMIT.get();
    }

    @Override
    public Map<Key, Integer> getEntitiesLimitsAsKeys() {
        return new KeyMap<>();
    }

    @Override
    public Map<Key, Integer> getCustomEntitiesLimits() {
        return new HashMap<>();
    }

    @Override
    public void clearEntitiesLimits() {

    }

    @Override
    public void setEntityLimit(EntityType entityType, int limit) {

    }

    @Override
    public void setEntityLimit(Key key, int limit) {

    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(EntityType entityType) {
        return hasReachedEntityLimit(entityType, 1);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(Key key) {
        return hasReachedEntityLimit(key, 1);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(EntityType entityType, int amount) {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(Key key, int amount) {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public int getTeamLimit() {
        return IslandUtils.NO_LIMIT.get();
    }

    @Override
    public int getTeamLimitRaw() {
        return 0;
    }

    @Override
    public void setTeamLimit(int teamLimit) {

    }

    @Override
    public int getWarpsLimit() {
        return IslandUtils.NO_LIMIT.get();
    }

    @Override
    public int getWarpsLimitRaw() {
        return IslandUtils.NO_LIMIT.get();
    }

    @Override
    public void setWarpsLimit(int warpsLimit) {

    }

    @Override
    public void setPotionEffect(PotionEffectType type, int level) {

    }

    @Override
    public int getPotionEffectLevel(PotionEffectType type) {
        return 0;
    }

    @Override
    public Map<PotionEffectType, Integer> getPotionEffects() {
        return new HashMap<>();
    }

    @Override
    public void applyEffects(SuperiorPlayer superiorPlayer) {

    }

    @Override
    public void removeEffects(SuperiorPlayer superiorPlayer) {

    }

    @Override
    public void removeEffects() {

    }

    @Override
    public void clearEffects() {

    }

    @Override
    public void setRoleLimit(PlayerRole playerRole, int limit) {

    }

    @Override
    public int getRoleLimit(PlayerRole playerRole) {
        return IslandUtils.NO_LIMIT.get();
    }

    @Override
    public int getRoleLimitRaw(PlayerRole playerRole) {
        return IslandUtils.NO_LIMIT.get();
    }

    @Override
    public Map<PlayerRole, Integer> getRoleLimits() {
        return new HashMap<>();
    }

    @Override
    public Map<PlayerRole, Integer> getCustomRoleLimits() {
        return new HashMap<>();
    }

    @Override
    public WarpCategory createWarpCategory(String name) {
        return null;
    }

    @Override
    public WarpCategory getWarpCategory(String name) {
        return null;
    }

    @Override
    public WarpCategory getWarpCategory(int slot) {
        return null;
    }

    @Override
    public void renameCategory(WarpCategory warpCategory, String newName) {

    }

    @Override
    public void deleteCategory(WarpCategory warpCategory) {

    }

    @Override
    public Map<String, WarpCategory> getWarpCategories() {
        return new HashMap<>();
    }

    @Override
    public IslandWarp createWarp(String name, Location location, WarpCategory warpCategory) {
        return null;
    }

    @Override
    public void renameWarp(IslandWarp islandWarp, String newName) {

    }

    @Override
    public IslandWarp getWarp(String name) {
        return null;
    }

    @Override
    public IslandWarp getWarp(Location location) {
        return null;
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
    public Map<String, IslandWarp> getIslandWarps() {
        return new HashMap<>();
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
    public void removeRatings() {

    }

    @Override
    public void completeMission(Mission<?> mission) {

    }

    @Override
    public void resetMission(Mission<?> mission) {

    }

    @Override
    public boolean hasCompletedMission(Mission<?> mission) {
        return false;
    }

    @Override
    public boolean canCompleteMissionAgain(Mission<?> mission) {
        return false;
    }

    @Override
    public int getAmountMissionCompleted(Mission<?> mission) {
        return 0;
    }

    @Override
    public List<Mission<?>> getCompletedMissions() {
        return new ArrayList<>();
    }

    @Override
    public Map<Mission<?>, Integer> getCompletedMissionsWithAmounts() {
        return new HashMap<>();
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
    public Map<IslandFlag, Byte> getAllSettings() {
        return new HashMap<>();
    }

    @Override
    public void setGeneratorPercentage(Key key, int percentage, World.Environment environment) {

    }

    @Override
    public int getGeneratorPercentage(Key key, World.Environment environment) {
        return 0;
    }

    @Override
    public Map<String, Integer> getGeneratorPercentages(World.Environment environment) {
        return new HashMap<>();
    }

    @Override
    public void setGeneratorAmount(Key key, int amount, World.Environment environment) {

    }

    @Override
    public int getGeneratorAmount(Key key, World.Environment environment) {
        return 0;
    }

    @Override
    public int getGeneratorTotalAmount(World.Environment environment) {
        return 0;
    }

    @Override
    public Map<String, Integer> getGeneratorAmounts(World.Environment environment) {
        return new HashMap<>();
    }

    @Override
    public Map<Key, Integer> getCustomGeneratorAmounts(World.Environment environment) {
        return new HashMap<>();
    }

    @Override
    public void clearGeneratorAmounts(World.Environment environment) {

    }

    @Override
    public boolean wasSchematicGenerated(World.Environment environment) {
        return true;
    }

    @Override
    public void setSchematicGenerate(World.Environment environment) {

    }

    @Override
    public void setSchematicGenerate(World.Environment environment, boolean generated) {

    }

    @Override
    public int getGeneratedSchematicsFlag() {
        return 0;
    }

    @Override
    public String getSchematicName() {
        return "";
    }

    @Override
    public int getPosition(SortingType sortingType) {
        return -1;
    }

    @Override
    public IslandChest[] getChest() {
        return new IslandChest[0];
    }

    @Override
    public int getChestSize() {
        return 0;
    }

    @Override
    public void setChestRows(int index, int rows) {

    }

    @Override
    public IslandDataHandler getDataHandler() {
        return SEmptyIslandDataHandler.getHandler();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Island o) {
        return 0;
    }

}
