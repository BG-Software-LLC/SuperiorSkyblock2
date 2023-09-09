package com.bgsoftware.superiorskyblock.api.island;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.annotations.Size;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandBlocksTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandEntitiesTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.bank.IslandBank;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DelegateIsland implements Island {

    protected final Island handle;

    protected DelegateIsland(Island handle) {
        this.handle = handle;
    }

    @Override
    public SuperiorPlayer getOwner() {
        return this.handle.getOwner();
    }

    @Override
    public UUID getUniqueId() {
        return this.handle.getUniqueId();
    }

    @Override
    public long getCreationTime() {
        return this.handle.getCreationTime();
    }

    @Override
    public String getCreationTimeDate() {
        return this.handle.getCreationTimeDate();
    }

    @Override
    public void updateDatesFormatter() {
        this.handle.updateDatesFormatter();
    }

    @Override
    public List<SuperiorPlayer> getIslandMembers(boolean includeOwner) {
        return this.handle.getIslandMembers(includeOwner);
    }

    @Override
    public List<SuperiorPlayer> getIslandMembers(PlayerRole... playerRoles) {
        return this.handle.getIslandMembers(playerRoles);
    }

    @Override
    public List<SuperiorPlayer> getBannedPlayers() {
        return this.handle.getBannedPlayers();
    }

    @Override
    public List<SuperiorPlayer> getIslandVisitors() {
        return this.handle.getIslandVisitors();
    }

    @Override
    public List<SuperiorPlayer> getIslandVisitors(boolean vanishPlayers) {
        return this.handle.getIslandVisitors(vanishPlayers);
    }

    @Override
    public List<SuperiorPlayer> getAllPlayersInside() {
        return this.handle.getAllPlayersInside();
    }

    @Override
    public List<SuperiorPlayer> getUniqueVisitors() {
        return this.handle.getUniqueVisitors();
    }

    @Override
    public List<Pair<SuperiorPlayer, Long>> getUniqueVisitorsWithTimes() {
        return this.handle.getUniqueVisitorsWithTimes();
    }

    @Override
    public void inviteMember(SuperiorPlayer superiorPlayer) {
        this.handle.inviteMember(superiorPlayer);
    }

    @Override
    public void revokeInvite(SuperiorPlayer superiorPlayer) {
        this.handle.revokeInvite(superiorPlayer);
    }

    @Override
    public boolean isInvited(SuperiorPlayer superiorPlayer) {
        return this.handle.isInvited(superiorPlayer);
    }

    @Override
    public List<SuperiorPlayer> getInvitedPlayers() {
        return this.handle.getInvitedPlayers();
    }

    @Override
    public void addMember(SuperiorPlayer superiorPlayer, PlayerRole playerRole) {
        this.handle.addMember(superiorPlayer, playerRole);
    }

    @Override
    public void kickMember(SuperiorPlayer superiorPlayer) {
        this.handle.kickMember(superiorPlayer);
    }

    @Override
    public boolean isMember(SuperiorPlayer superiorPlayer) {
        return this.handle.isMember(superiorPlayer);
    }

    @Override
    public void banMember(SuperiorPlayer superiorPlayer) {
        this.handle.banMember(superiorPlayer);
    }

    @Override
    public void banMember(SuperiorPlayer superiorPlayer, @Nullable SuperiorPlayer whom) {
        this.handle.banMember(superiorPlayer, whom);
    }

    @Override
    public void unbanMember(SuperiorPlayer superiorPlayer) {
        this.handle.unbanMember(superiorPlayer);
    }

    @Override
    public boolean isBanned(SuperiorPlayer superiorPlayer) {
        return this.handle.isBanned(superiorPlayer);
    }

    @Override
    public void addCoop(SuperiorPlayer superiorPlayer) {
        this.handle.addCoop(superiorPlayer);
    }

    @Override
    public void removeCoop(SuperiorPlayer superiorPlayer) {
        this.handle.removeCoop(superiorPlayer);
    }

    @Override
    public boolean isCoop(SuperiorPlayer superiorPlayer) {
        return this.handle.isCoop(superiorPlayer);
    }

    @Override
    public List<SuperiorPlayer> getCoopPlayers() {
        return this.handle.getCoopPlayers();
    }

    @Override
    public int getCoopLimit() {
        return this.handle.getCoopLimit();
    }

    @Override
    public int getCoopLimitRaw() {
        return this.handle.getCoopLimitRaw();
    }

    @Override
    public void setCoopLimit(int coopLimit) {
        this.handle.setCoopLimit(coopLimit);
    }

    @Override
    public void setPlayerInside(SuperiorPlayer superiorPlayer, boolean inside) {
        this.handle.setPlayerInside(superiorPlayer, inside);
    }

    @Override
    public boolean isVisitor(SuperiorPlayer superiorPlayer, boolean checkCoopStatus) {
        return this.handle.isVisitor(superiorPlayer, checkCoopStatus);
    }

    @Override
    public Location getCenter(World.Environment environment) {
        return this.handle.getCenter(environment);
    }

    @Override
    public BlockPosition getCenterPosition() {
        return this.handle.getCenterPosition();
    }

    @Nullable
    @Override
    public Location getTeleportLocation(World.Environment environment) {
        return this.handle.getTeleportLocation(environment);
    }

    @Override
    public Map<World.Environment, Location> getTeleportLocations() {
        return this.handle.getTeleportLocations();
    }

    @Override
    public void setTeleportLocation(Location teleportLocation) {
        this.handle.setTeleportLocation(teleportLocation);
    }

    @Override
    public void setTeleportLocation(World.Environment environment, @Nullable Location teleportLocation) {
        this.handle.setTeleportLocation(environment, teleportLocation);
    }

    @Nullable
    @Override
    public Location getIslandHome(World.Environment environment) {
        return this.handle.getIslandHome(environment);
    }

    @Override
    public Map<World.Environment, Location> getIslandHomes() {
        return this.handle.getIslandHomes();
    }

    @Override
    public void setIslandHome(Location homeLocation) {
        this.handle.setIslandHome(homeLocation);
    }

    @Override
    public void setIslandHome(World.Environment environment, @Nullable Location homeLocation) {
        this.handle.setIslandHome(environment, homeLocation);
    }

    @Nullable
    @Override
    @Deprecated
    public Location getVisitorsLocation() {
        return this.handle.getVisitorsLocation();
    }

    @Nullable
    @Override
    public Location getVisitorsLocation(World.Environment environment) {
        return this.handle.getVisitorsLocation(environment);
    }

    @Override
    public void setVisitorsLocation(@Nullable Location visitorsLocation) {
        this.handle.setVisitorsLocation(visitorsLocation);
    }

    @Override
    public Location getMinimum() {
        return this.handle.getMinimum();
    }

    @Override
    public BlockPosition getMinimumPosition() {
        return this.handle.getMinimumPosition();
    }

    @Override
    public Location getMinimumProtected() {
        return this.handle.getMinimumProtected();
    }

    @Override
    public BlockPosition getMinimumProtectedPosition() {
        return this.handle.getMinimumProtectedPosition();
    }

    @Override
    public Location getMaximum() {
        return this.handle.getMaximum();
    }

    @Override
    public BlockPosition getMaximumPosition() {
        return this.handle.getMaximumPosition();
    }

    @Override
    public Location getMaximumProtected() {
        return this.handle.getMaximumProtected();
    }

    @Override
    public BlockPosition getMaximumProtectedPosition() {
        return this.handle.getMaximumProtectedPosition();
    }

    @Override
    public List<Chunk> getAllChunks() {
        return this.handle.getAllChunks();
    }

    @Override
    public List<Chunk> getAllChunks(@IslandChunkFlags int flags) {
        return this.handle.getAllChunks(flags);
    }

    @Override
    public List<Chunk> getAllChunks(World.Environment environment) {
        return this.handle.getAllChunks(environment);
    }

    @Override
    public List<Chunk> getAllChunks(World.Environment environment, @IslandChunkFlags int flags) {
        return this.handle.getAllChunks(environment, flags);
    }

    @Override
    @Deprecated
    public List<Chunk> getAllChunks(boolean onlyProtected) {
        return this.handle.getAllChunks(onlyProtected);
    }

    @Override
    @Deprecated
    public List<Chunk> getAllChunks(World.Environment environment, boolean onlyProtected) {
        return this.handle.getAllChunks(environment, onlyProtected);
    }

    @Override
    @Deprecated
    public List<Chunk> getAllChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks) {
        return this.handle.getAllChunks(environment, onlyProtected, noEmptyChunks);
    }

    @Override
    public List<Chunk> getLoadedChunks() {
        return this.handle.getLoadedChunks();
    }

    @Override
    public List<Chunk> getLoadedChunks(@IslandChunkFlags int flags) {
        return this.handle.getLoadedChunks(flags);
    }

    @Override
    public List<Chunk> getLoadedChunks(World.Environment environment) {
        return this.handle.getLoadedChunks(environment);
    }

    @Override
    public List<Chunk> getLoadedChunks(World.Environment environment, @IslandChunkFlags int flags) {
        return this.handle.getLoadedChunks(environment, flags);
    }

    @Override
    @Deprecated
    public List<Chunk> getLoadedChunks(boolean onlyProtected, boolean noEmptyChunks) {
        return this.handle.getLoadedChunks(onlyProtected, noEmptyChunks);
    }

    @Override
    @Deprecated
    public List<Chunk> getLoadedChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks) {
        return this.handle.getLoadedChunks(environment, onlyProtected, noEmptyChunks);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment) {
        return getAllChunksAsync(environment, 0);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, int flags) {
        return getAllChunksAsync(environment, flags, null);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment,
                                                            @Nullable Consumer<Chunk> onChunkLoad) {
        return this.handle.getAllChunksAsync(environment, onChunkLoad);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment,
                                                            @IslandChunkFlags int flags,
                                                            @Nullable Consumer<Chunk> onChunkLoad) {
        return this.handle.getAllChunksAsync(environment, flags, onChunkLoad);
    }

    @Override
    @Deprecated
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected, @Nullable Consumer<Chunk> onChunkLoad) {
        return this.handle.getAllChunksAsync(environment, onlyProtected, onChunkLoad);
    }

    @Override
    @Deprecated
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks, @Nullable Consumer<Chunk> onChunkLoad) {
        return this.handle.getAllChunksAsync(environment, onlyProtected, noEmptyChunks, onChunkLoad);
    }

    @Override
    public void resetChunks() {
        this.handle.resetChunks();
    }

    @Override
    public void resetChunks(@Nullable Runnable onFinish) {
        this.handle.resetChunks(onFinish);
    }

    @Override
    public void resetChunks(World.Environment environment) {
        this.handle.resetChunks(environment);
    }

    @Override
    public void resetChunks(World.Environment environment, @Nullable Runnable onFinish) {
        this.handle.resetChunks(environment, onFinish);
    }

    @Override
    public void resetChunks(@IslandChunkFlags int flags) {
        this.handle.resetChunks(flags);
    }

    @Override
    public void resetChunks(@IslandChunkFlags int flags, @Nullable Runnable onFinish) {
        this.handle.resetChunks(flags, onFinish);
    }

    @Override
    public void resetChunks(World.Environment environment, @IslandChunkFlags int flags) {
        this.handle.resetChunks(environment, flags);
    }

    @Override
    public void resetChunks(World.Environment environment, @IslandChunkFlags int flags, @Nullable Runnable onFinish) {
        this.handle.resetChunks(environment, flags, onFinish);
    }

    @Override
    @Deprecated
    public void resetChunks(World.Environment environment, boolean onlyProtected) {
        this.handle.resetChunks(environment, onlyProtected);
    }

    @Override
    @Deprecated
    public void resetChunks(World.Environment environment, boolean onlyProtected, @Nullable Runnable onFinish) {
        this.handle.resetChunks(environment, onlyProtected, onFinish);
    }

    @Override
    @Deprecated
    public void resetChunks(boolean onlyProtected) {
        this.handle.resetChunks(onlyProtected);
    }

    @Override
    @Deprecated
    public void resetChunks(boolean onlyProtected, @Nullable Runnable onFinish) {
        this.handle.resetChunks(onlyProtected, onFinish);
    }

    @Override
    public boolean isInside(Location location) {
        return this.handle.isInside(location);
    }

    @Override
    public boolean isInside(World world, int chunkX, int chunkZ) {
        return this.handle.isInside(world, chunkX, chunkZ);
    }

    @Override
    public boolean isInsideRange(Location location) {
        return this.handle.isInsideRange(location);
    }

    @Override
    public boolean isInsideRange(Location location, int extraRadius) {
        return this.handle.isInsideRange(location, extraRadius);
    }

    @Override
    public boolean isInsideRange(Chunk chunk) {
        return this.handle.isInsideRange(chunk);
    }

    @Override
    public boolean isNormalEnabled() {
        return this.handle.isNormalEnabled();
    }

    @Override
    public void setNormalEnabled(boolean enabled) {
        this.handle.setNormalEnabled(enabled);
    }

    @Override
    public boolean isNetherEnabled() {
        return this.handle.isNetherEnabled();
    }

    @Override
    public void setNetherEnabled(boolean enabled) {
        this.handle.setNetherEnabled(enabled);
    }

    @Override
    public boolean isEndEnabled() {
        return this.handle.isEndEnabled();
    }

    @Override
    public void setEndEnabled(boolean enabled) {
        this.handle.setEndEnabled(enabled);
    }

    @Override
    public int getUnlockedWorldsFlag() {
        return this.handle.getUnlockedWorldsFlag();
    }

    @Override
    public boolean hasPermission(CommandSender sender, IslandPrivilege islandPrivilege) {
        return this.handle.hasPermission(sender, islandPrivilege);
    }

    @Override
    public boolean hasPermission(SuperiorPlayer superiorPlayer, IslandPrivilege islandPrivilege) {
        return this.handle.hasPermission(superiorPlayer, islandPrivilege);
    }

    @Override
    public boolean hasPermission(PlayerRole playerRole, IslandPrivilege islandPrivilege) {
        return this.handle.hasPermission(playerRole, islandPrivilege);
    }

    @Override
    @Deprecated
    public void setPermission(PlayerRole playerRole, IslandPrivilege islandPrivilege, boolean value) {
        this.handle.setPermission(playerRole, islandPrivilege, value);
    }

    @Override
    public void setPermission(PlayerRole playerRole, IslandPrivilege islandPrivilege) {
        this.handle.setPermission(playerRole, islandPrivilege);
    }

    @Override
    public void resetPermissions() {
        this.handle.resetPermissions();
    }

    @Override
    public void setPermission(SuperiorPlayer superiorPlayer, IslandPrivilege islandPrivilege, boolean value) {
        this.handle.setPermission(superiorPlayer, islandPrivilege, value);
    }

    @Override
    public void resetPermissions(SuperiorPlayer superiorPlayer) {
        this.handle.resetPermissions(superiorPlayer);
    }

    @Override
    public PermissionNode getPermissionNode(SuperiorPlayer superiorPlayer) {
        return this.handle.getPermissionNode(superiorPlayer);
    }

    @Override
    public PlayerRole getRequiredPlayerRole(IslandPrivilege islandPrivilege) {
        return this.handle.getRequiredPlayerRole(islandPrivilege);
    }

    @Override
    public Map<SuperiorPlayer, PermissionNode> getPlayerPermissions() {
        return this.handle.getPlayerPermissions();
    }

    @Override
    public Map<IslandPrivilege, PlayerRole> getRolePermissions() {
        return this.handle.getRolePermissions();
    }

    @Override
    public boolean isSpawn() {
        return this.handle.isSpawn();
    }

    @Override
    public String getName() {
        return this.handle.getName();
    }

    @Override
    public void setName(String islandName) {
        this.handle.setName(islandName);
    }

    @Override
    public String getRawName() {
        return this.handle.getRawName();
    }

    @Override
    public String getDescription() {
        return this.handle.getDescription();
    }

    @Override
    public void setDescription(String description) {
        this.handle.setDescription(description);
    }

    @Override
    public void disbandIsland() {
        this.handle.disbandIsland();
    }

    @Override
    public boolean transferIsland(SuperiorPlayer superiorPlayer) {
        return this.handle.transferIsland(superiorPlayer);
    }

    @Override
    public void replacePlayers(SuperiorPlayer originalPlayer, SuperiorPlayer newPlayer) {
        this.handle.replacePlayers(originalPlayer, newPlayer);
    }

    @Override
    public void calcIslandWorth(@Nullable SuperiorPlayer asker) {
        this.handle.calcIslandWorth(asker);
    }

    @Override
    public void calcIslandWorth(@Nullable SuperiorPlayer asker, @Nullable Runnable callback) {
        this.handle.calcIslandWorth(asker, callback);
    }

    @Override
    public IslandCalculationAlgorithm getCalculationAlgorithm() {
        return this.handle.getCalculationAlgorithm();
    }

    @Override
    public void updateBorder() {
        this.handle.updateBorder();
    }

    @Override
    public void updateIslandFly(SuperiorPlayer superiorPlayer) {
        this.handle.updateIslandFly(superiorPlayer);
    }

    @Override
    public int getIslandSize() {
        return this.handle.getIslandSize();
    }

    @Override
    public void setIslandSize(int islandSize) {
        this.handle.setIslandSize(islandSize);
    }

    @Override
    public int getIslandSizeRaw() {
        return this.handle.getIslandSizeRaw();
    }

    @Override
    public String getDiscord() {
        return this.handle.getDiscord();
    }

    @Override
    public void setDiscord(String discord) {
        this.handle.setDiscord(discord);
    }

    @Override
    public String getPaypal() {
        return this.handle.getPaypal();
    }

    @Override
    public void setPaypal(String paypal) {
        this.handle.setPaypal(paypal);
    }

    @Override
    public Biome getBiome() {
        return this.handle.getBiome();
    }

    @Override
    public void setBiome(Biome biome) {
        this.handle.setBiome(biome);
    }

    @Override
    public void setBiome(Biome biome, boolean updateBlocks) {
        this.handle.setBiome(biome, updateBlocks);
    }

    @Override
    public boolean isLocked() {
        return this.handle.isLocked();
    }

    @Override
    public void setLocked(boolean locked) {
        this.handle.setLocked(locked);
    }

    @Override
    public boolean isIgnored() {
        return this.handle.isIgnored();
    }

    @Override
    public void setIgnored(boolean ignored) {
        this.handle.setIgnored(ignored);
    }

    @Override
    public void sendMessage(String message, UUID... ignoredMembers) {
        this.handle.sendMessage(message, ignoredMembers);
    }

    @Override
    public void sendMessage(IMessageComponent messageComponent, Object... args) {
        this.handle.sendMessage(messageComponent, args);
    }

    @Override
    public void sendMessage(IMessageComponent messageComponent, List<UUID> ignoredMembers, Object... args) {
        this.handle.sendMessage(messageComponent, ignoredMembers, args);
    }

    @Override
    public void sendTitle(@Nullable String title, @Nullable String subtitle, int fadeIn, int duration, int fadeOut, UUID... ignoredMembers) {
        this.handle.sendTitle(title, subtitle, fadeIn, duration, fadeOut, ignoredMembers);
    }

    @Override
    public void executeCommand(String command, boolean onlyOnlineMembers, UUID... ignoredMembers) {
        this.handle.executeCommand(command, onlyOnlineMembers, ignoredMembers);
    }

    @Override
    public boolean isBeingRecalculated() {
        return this.handle.isBeingRecalculated();
    }

    @Override
    public void updateLastTime() {
        this.handle.updateLastTime();
    }

    @Override
    public void setCurrentlyActive() {
        this.handle.setCurrentlyActive();
    }

    @Override
    public void completeMission(Mission<?> mission) {
        this.handle.completeMission(mission);
    }

    @Override
    public void setCurrentlyActive(boolean active) {
        this.handle.setCurrentlyActive(active);
    }

    @Override
    public void resetMission(Mission<?> mission) {
        this.handle.resetMission(mission);
    }

    @Override
    public boolean isCurrentlyActive() {
        return this.handle.isCurrentlyActive();
    }

    @Override
    public boolean hasCompletedMission(Mission<?> mission) {
        return this.handle.hasCompletedMission(mission);
    }

    @Override
    public long getLastTimeUpdate() {
        return this.handle.getLastTimeUpdate();
    }

    @Override
    public boolean canCompleteMissionAgain(Mission<?> mission) {
        return this.handle.canCompleteMissionAgain(mission);
    }

    @Override
    public void setLastTimeUpdate(long lastTimeUpdate) {
        this.handle.setLastTimeUpdate(lastTimeUpdate);
    }

    @Override
    public int getAmountMissionCompleted(Mission<?> mission) {
        return this.handle.getAmountMissionCompleted(mission);
    }

    @Override
    public IslandBank getIslandBank() {
        return this.handle.getIslandBank();
    }

    @Override
    public void setAmountMissionCompleted(Mission<?> mission, int finishCount) {
        this.handle.setAmountMissionCompleted(mission, finishCount);
    }

    @Override
    public BigDecimal getBankLimit() {
        return this.handle.getBankLimit();
    }

    @Override
    public List<Mission<?>> getCompletedMissions() {
        return this.handle.getCompletedMissions();
    }

    @Override
    public void setBankLimit(BigDecimal bankLimit) {
        this.handle.setBankLimit(bankLimit);
    }

    @Override
    public Map<Mission<?>, Integer> getCompletedMissionsWithAmounts() {
        return this.handle.getCompletedMissionsWithAmounts();
    }

    @Override
    public BigDecimal getBankLimitRaw() {
        return this.handle.getBankLimitRaw();
    }

    @Override
    public DatabaseBridge getDatabaseBridge() {
        return this.handle.getDatabaseBridge();
    }

    @Override
    public boolean giveInterest(boolean checkOnlineOwner) {
        return this.handle.giveInterest(checkOnlineOwner);
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return this.handle.getPersistentDataContainer();
    }

    @Override
    public long getLastInterestTime() {
        return this.handle.getLastInterestTime();
    }

    @Override
    public boolean isPersistentDataContainerEmpty() {
        return this.handle.isPersistentDataContainerEmpty();
    }

    @Override
    public void setLastInterestTime(long lastInterest) {
        this.handle.setLastInterestTime(lastInterest);
    }

    @Override
    public void savePersistentDataContainer() {
        this.handle.savePersistentDataContainer();
    }

    @Override
    public long getNextInterest() {
        return this.handle.getNextInterest();
    }

    @Override
    public void handleBlockPlace(Block block) {
        this.handle.handleBlockPlace(block);
    }

    @Override
    public BlockChangeResult handleBlockPlaceWithResult(Block block) {
        return this.handle.handleBlockPlaceWithResult(block);
    }

    @Override
    public void handleBlockPlace(Key key) {
        this.handle.handleBlockPlace(key);
    }

    @Override
    public BlockChangeResult handleBlockPlaceWithResult(Key key) {
        return this.handle.handleBlockPlaceWithResult(key);
    }

    @Override
    public void handleBlockPlace(Block block, @Size int amount) {
        this.handle.handleBlockPlace(block, amount);
    }

    @Override
    public BlockChangeResult handleBlockPlaceWithResult(Block block, @Size int amount) {
        return this.handle.handleBlockPlaceWithResult(block, amount);
    }

    @Override
    public void handleBlockPlace(Key key, @Size int amount) {
        this.handle.handleBlockPlace(key, amount);
    }

    @Override
    public BlockChangeResult handleBlockPlaceWithResult(Key key, @Size int amount) {
        return this.handle.handleBlockPlaceWithResult(key, amount);
    }

    @Override
    public void handleBlockPlace(Block block, @Size int amount, @IslandBlockFlags int flags) {
        this.handle.handleBlockPlace(block, amount, flags);
    }

    @Override
    public BlockChangeResult handleBlockPlaceWithResult(Block block, @Size int amount, @IslandBlockFlags int flags) {
        return this.handle.handleBlockPlaceWithResult(block, amount, flags);
    }

    @Override
    public void handleBlockPlace(Key key, @Size int amount, @IslandBlockFlags int flags) {
        this.handle.handleBlockPlace(key, amount, flags);
    }

    @Override
    public BlockChangeResult handleBlockPlaceWithResult(Key key, @Size int amount, @IslandBlockFlags int flags) {
        return this.handle.handleBlockPlaceWithResult(key, amount, flags);
    }

    @Override
    @Deprecated
    public void handleBlockPlace(Block block, @Size int amount, boolean save) {
        this.handle.handleBlockPlace(block, amount, save);
    }

    @Override
    @Deprecated
    public void handleBlockPlace(Key key, @Size int amount, boolean save) {
        this.handle.handleBlockPlace(key, amount, save);
    }

    @Override
    @Deprecated
    public void handleBlockPlace(Key key, BigInteger amount, boolean save) {
        this.handle.handleBlockPlace(key, amount, save);
    }

    @Override
    @Deprecated
    public void handleBlockPlace(Key key, BigInteger amount, boolean save, boolean updateLastTimeStatus) {
        this.handle.handleBlockPlace(key, amount, save, updateLastTimeStatus);
    }

    @Override
    public void handleBlocksPlace(Map<Key, Integer> blocks) {
        this.handle.handleBlocksPlace(blocks);
    }

    @Override
    public Map<Key, BlockChangeResult> handleBlocksPlaceWithResult(Map<Key, Integer> blocks) {
        return this.handle.handleBlocksPlaceWithResult(blocks);
    }

    @Override
    public void handleBlocksPlace(Map<Key, Integer> blocks, @IslandBlockFlags int flags) {
        this.handle.handleBlocksPlace(blocks, flags);
    }

    @Override
    public Map<Key, BlockChangeResult> handleBlocksPlaceWithResult(Map<Key, Integer> blocks, int flags) {
        return this.handle.handleBlocksPlaceWithResult(blocks, flags);
    }

    @Override
    public void handleBlockBreak(Block block) {
        this.handle.handleBlockBreak(block);
    }

    @Override
    public BlockChangeResult handleBlockBreakWithResult(Block block) {
        return this.handle.handleBlockBreakWithResult(block);
    }

    @Override
    public void handleBlockBreak(Key key) {
        this.handle.handleBlockBreak(key);
    }

    @Override
    public BlockChangeResult handleBlockBreakWithResult(Key key) {
        return this.handle.handleBlockBreakWithResult(key);
    }

    @Override
    public void handleBlockBreak(Block block, @Size int amount) {
        this.handle.handleBlockBreak(block, amount);
    }

    @Override
    public BlockChangeResult handleBlockBreakWithResult(Block block, @Size int amount) {
        return this.handle.handleBlockBreakWithResult(block, amount);
    }

    @Override
    public void handleBlockBreak(Key key, @Size int amount) {
        this.handle.handleBlockBreak(key, amount);
    }

    @Override
    public BlockChangeResult handleBlockBreakWithResult(Key key, @Size int amount) {
        return this.handle.handleBlockBreakWithResult(key, amount);
    }

    @Override
    public void handleBlockBreak(Block block, @Size int amount, @IslandBlockFlags int flags) {
        this.handle.handleBlockBreak(block, amount, flags);
    }

    @Override
    public BlockChangeResult handleBlockBreakWithResult(Block block, @Size int amount, @IslandBlockFlags int flags) {
        return this.handle.handleBlockBreakWithResult(block, amount, flags);
    }

    @Override
    public void handleBlockBreak(Key key, @Size int amount, @IslandBlockFlags int flags) {
        this.handle.handleBlockBreak(key, amount, flags);
    }

    @Override
    public BlockChangeResult handleBlockBreakWithResult(Key key, @Size int amount, @IslandBlockFlags int flags) {
        return this.handle.handleBlockBreakWithResult(key, amount, flags);
    }

    @Override
    @Deprecated
    public void handleBlockBreak(Block block, @Size int amount, boolean save) {
        this.handle.handleBlockBreak(block, amount, save);
    }

    @Override
    @Deprecated
    public void handleBlockBreak(Key key, @Size int amount, boolean save) {
        this.handle.handleBlockBreak(key, amount, save);
    }

    @Override
    @Deprecated
    public void handleBlockBreak(Key key, BigInteger amount, boolean save) {
        this.handle.handleBlockBreak(key, amount, save);
    }

    @Override
    public void handleBlocksBreak(Map<Key, Integer> blocks) {
        this.handle.handleBlocksBreak(blocks);
    }

    @Override
    public Map<Key, BlockChangeResult> handleBlocksBreakWithResult(Map<Key, Integer> blocks) {
        return this.handle.handleBlocksBreakWithResult(blocks);
    }

    @Override
    public void handleBlocksBreak(Map<Key, Integer> blocks, @IslandBlockFlags int flags) {
        this.handle.handleBlocksBreak(blocks, flags);
    }

    @Override
    public Map<Key, BlockChangeResult> handleBlocksBreakWithResult(Map<Key, Integer> blocks, @IslandBlockFlags int flags) {
        return this.handle.handleBlocksBreakWithResult(blocks, flags);
    }

    @Override
    public boolean isChunkDirty(World world, int chunkX, int chunkZ) {
        return this.handle.isChunkDirty(world, chunkX, chunkZ);
    }

    @Override
    public boolean isChunkDirty(String worldName, int chunkX, int chunkZ) {
        return this.handle.isChunkDirty(worldName, chunkX, chunkZ);
    }

    @Override
    public void markChunkDirty(World world, int chunkX, int chunkZ, boolean save) {
        this.handle.markChunkDirty(world, chunkX, chunkZ, save);
    }

    @Override
    public void markChunkEmpty(World world, int chunkX, int chunkZ, boolean save) {
        this.handle.markChunkEmpty(world, chunkX, chunkZ, save);
    }

    @Override
    public BigInteger getBlockCountAsBigInteger(Key key) {
        return this.handle.getBlockCountAsBigInteger(key);
    }

    @Override
    public Map<Key, BigInteger> getBlockCountsAsBigInteger() {
        return this.handle.getBlockCountsAsBigInteger();
    }

    @Override
    public BigInteger getExactBlockCountAsBigInteger(Key key) {
        return this.handle.getExactBlockCountAsBigInteger(key);
    }

    @Override
    public void clearBlockCounts() {
        this.handle.clearBlockCounts();
    }

    @Override
    public IslandBlocksTrackerAlgorithm getBlocksTracker() {
        return this.handle.getBlocksTracker();
    }

    @Override
    public BigDecimal getWorth() {
        return this.handle.getWorth();
    }

    @Override
    public BigDecimal getRawWorth() {
        return this.handle.getRawWorth();
    }

    @Override
    public BigDecimal getBonusWorth() {
        return this.handle.getBonusWorth();
    }

    @Override
    public void setBonusWorth(BigDecimal bonusWorth) {
        this.handle.setBonusWorth(bonusWorth);
    }

    @Override
    public BigDecimal getBonusLevel() {
        return this.handle.getBonusLevel();
    }

    @Override
    public void setBonusLevel(BigDecimal bonusLevel) {
        this.handle.setBonusLevel(bonusLevel);
    }

    @Override
    public BigDecimal getIslandLevel() {
        return this.handle.getIslandLevel();
    }

    @Override
    public BigDecimal getRawLevel() {
        return this.handle.getRawLevel();
    }

    @Override
    public UpgradeLevel getUpgradeLevel(Upgrade upgrade) {
        return this.handle.getUpgradeLevel(upgrade);
    }

    @Override
    public void setUpgradeLevel(Upgrade upgrade, int level) {
        this.handle.setUpgradeLevel(upgrade, level);
    }

    @Override
    public Map<String, Integer> getUpgrades() {
        return this.handle.getUpgrades();
    }

    @Override
    public void syncUpgrades() {
        this.handle.syncUpgrades();
    }

    @Override
    public void updateUpgrades() {
        this.handle.updateUpgrades();
    }

    @Override
    public long getLastTimeUpgrade() {
        return this.handle.getLastTimeUpgrade();
    }

    @Override
    public boolean hasActiveUpgradeCooldown() {
        return this.handle.hasActiveUpgradeCooldown();
    }

    @Override
    public double getCropGrowthMultiplier() {
        return this.handle.getCropGrowthMultiplier();
    }

    @Override
    public void setCropGrowthMultiplier(double cropGrowth) {
        this.handle.setCropGrowthMultiplier(cropGrowth);
    }

    @Override
    public double getCropGrowthRaw() {
        return this.handle.getCropGrowthRaw();
    }

    @Override
    public double getSpawnerRatesMultiplier() {
        return this.handle.getSpawnerRatesMultiplier();
    }

    @Override
    public void setSpawnerRatesMultiplier(double spawnerRates) {
        this.handle.setSpawnerRatesMultiplier(spawnerRates);
    }

    @Override
    public double getSpawnerRatesRaw() {
        return this.handle.getSpawnerRatesRaw();
    }

    @Override
    public double getMobDropsMultiplier() {
        return this.handle.getMobDropsMultiplier();
    }

    @Override
    public void setMobDropsMultiplier(double mobDrops) {
        this.handle.setMobDropsMultiplier(mobDrops);
    }

    @Override
    public double getMobDropsRaw() {
        return this.handle.getMobDropsRaw();
    }

    @Override
    public int getBlockLimit(Key key) {
        return this.handle.getBlockLimit(key);
    }

    @Override
    public int getExactBlockLimit(Key key) {
        return this.handle.getExactBlockLimit(key);
    }

    @Override
    public Key getBlockLimitKey(Key key) {
        return this.handle.getBlockLimitKey(key);
    }

    @Override
    public Map<Key, Integer> getBlocksLimits() {
        return this.handle.getBlocksLimits();
    }

    @Override
    public Map<Key, Integer> getCustomBlocksLimits() {
        return this.handle.getCustomBlocksLimits();
    }

    @Override
    public void clearBlockLimits() {
        this.handle.clearBlockLimits();
    }

    @Override
    public void setBlockLimit(Key key, int limit) {
        this.handle.setBlockLimit(key, limit);
    }

    @Override
    public void removeBlockLimit(Key key) {
        this.handle.removeBlockLimit(key);
    }

    @Override
    public boolean hasReachedBlockLimit(Key key) {
        return this.handle.hasReachedBlockLimit(key);
    }

    @Override
    public boolean hasReachedBlockLimit(Key key, @Size int amount) {
        return this.handle.hasReachedBlockLimit(key, amount);
    }

    @Override
    public int getEntityLimit(EntityType entityType) {
        return this.handle.getEntityLimit(entityType);
    }

    @Override
    public int getEntityLimit(Key key) {
        return this.handle.getEntityLimit(key);
    }

    @Override
    public Map<Key, Integer> getEntitiesLimitsAsKeys() {
        return this.handle.getEntitiesLimitsAsKeys();
    }

    @Override
    public Map<Key, Integer> getCustomEntitiesLimits() {
        return this.handle.getCustomEntitiesLimits();
    }

    @Override
    public void clearEntitiesLimits() {
        this.handle.clearEntitiesLimits();
    }

    @Override
    public void setEntityLimit(EntityType entityType, int limit) {
        this.handle.setEntityLimit(entityType, limit);
    }

    @Override
    public void setEntityLimit(Key key, int limit) {
        this.handle.setEntityLimit(key, limit);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(EntityType entityType) {
        return this.handle.hasReachedEntityLimit(entityType);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(Key key) {
        return this.handle.hasReachedEntityLimit(key);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(EntityType entityType, @Size int amount) {
        return this.handle.hasReachedEntityLimit(entityType, amount);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(Key key, @Size int amount) {
        return this.handle.hasReachedEntityLimit(key, amount);
    }

    @Override
    public IslandEntitiesTrackerAlgorithm getEntitiesTracker() {
        return this.handle.getEntitiesTracker();
    }

    @Override
    public int getTeamLimit() {
        return this.handle.getTeamLimit();
    }

    @Override
    public void setTeamLimit(int teamLimit) {
        this.handle.setTeamLimit(teamLimit);
    }

    @Override
    public int getTeamLimitRaw() {
        return this.handle.getTeamLimitRaw();
    }

    @Override
    public int getWarpsLimit() {
        return this.handle.getWarpsLimit();
    }

    @Override
    public void setWarpsLimit(int warpsLimit) {
        this.handle.setWarpsLimit(warpsLimit);
    }

    @Override
    public int getWarpsLimitRaw() {
        return this.handle.getWarpsLimitRaw();
    }

    @Override
    public void setPotionEffect(PotionEffectType type, int level) {
        this.handle.setPotionEffect(type, level);
    }

    @Override
    public void removePotionEffect(PotionEffectType type) {
        this.handle.removePotionEffect(type);
    }

    @Override
    public int getPotionEffectLevel(PotionEffectType type) {
        return this.handle.getPotionEffectLevel(type);
    }

    @Override
    public Map<PotionEffectType, Integer> getPotionEffects() {
        return this.handle.getPotionEffects();
    }

    @Override
    public void applyEffects(SuperiorPlayer superiorPlayer) {
        this.handle.applyEffects(superiorPlayer);
    }

    @Override
    public void applyEffects() {
        this.handle.applyEffects();
    }

    @Override
    public void removeEffects(SuperiorPlayer superiorPlayer) {
        this.handle.removeEffects(superiorPlayer);
    }

    @Override
    public void removeEffects() {
        this.handle.removeEffects();
    }

    @Override
    public void clearEffects() {
        this.handle.clearEffects();
    }

    @Override
    public void setRoleLimit(PlayerRole playerRole, int limit) {
        this.handle.setRoleLimit(playerRole, limit);
    }

    @Override
    public void removeRoleLimit(PlayerRole playerRole) {
        this.handle.removeRoleLimit(playerRole);
    }

    @Override
    public int getRoleLimit(PlayerRole playerRole) {
        return this.handle.getRoleLimit(playerRole);
    }

    @Override
    public int getRoleLimitRaw(PlayerRole playerRole) {
        return this.handle.getRoleLimitRaw(playerRole);
    }

    @Override
    public Map<PlayerRole, Integer> getRoleLimits() {
        return this.handle.getRoleLimits();
    }

    @Override
    public Map<PlayerRole, Integer> getCustomRoleLimits() {
        return this.handle.getCustomRoleLimits();
    }

    @Override
    public WarpCategory createWarpCategory(String name) {
        return this.handle.createWarpCategory(name);
    }

    @Nullable
    @Override
    public WarpCategory getWarpCategory(String name) {
        return this.handle.getWarpCategory(name);
    }

    @Nullable
    @Override
    public WarpCategory getWarpCategory(int slot) {
        return this.handle.getWarpCategory(slot);
    }

    @Override
    public void renameCategory(WarpCategory warpCategory, String newName) {
        this.handle.renameCategory(warpCategory, newName);
    }

    @Override
    public void deleteCategory(WarpCategory warpCategory) {
        this.handle.deleteCategory(warpCategory);
    }

    @Override
    public Map<String, WarpCategory> getWarpCategories() {
        return this.handle.getWarpCategories();
    }

    @Override
    public IslandWarp createWarp(String name, Location location, @Nullable WarpCategory warpCategory) {
        return this.handle.createWarp(name, location, warpCategory);
    }

    @Override
    public void renameWarp(IslandWarp islandWarp, String newName) {
        this.handle.renameWarp(islandWarp, newName);
    }

    @Nullable
    @Override
    public IslandWarp getWarp(Location location) {
        return this.handle.getWarp(location);
    }

    @Nullable
    @Override
    public IslandWarp getWarp(String name) {
        return this.handle.getWarp(name);
    }

    @Override
    public void warpPlayer(SuperiorPlayer superiorPlayer, String warp) {
        this.handle.warpPlayer(superiorPlayer, warp);
    }

    @Override
    public void deleteWarp(@Nullable SuperiorPlayer superiorPlayer, Location location) {
        this.handle.deleteWarp(superiorPlayer, location);
    }

    @Override
    public void deleteWarp(String name) {
        this.handle.deleteWarp(name);
    }

    @Override
    public Map<String, IslandWarp> getIslandWarps() {
        return this.handle.getIslandWarps();
    }

    @Override
    public Rating getRating(SuperiorPlayer superiorPlayer) {
        return this.handle.getRating(superiorPlayer);
    }

    @Override
    public void setRating(SuperiorPlayer superiorPlayer, Rating rating) {
        this.handle.setRating(superiorPlayer, rating);
    }

    @Override
    public void removeRating(SuperiorPlayer superiorPlayer) {
        this.handle.removeRating(superiorPlayer);
    }

    @Override
    public double getTotalRating() {
        return this.handle.getTotalRating();
    }

    @Override
    public int getRatingAmount() {
        return this.handle.getRatingAmount();
    }

    @Override
    public Map<UUID, Rating> getRatings() {
        return this.handle.getRatings();
    }

    @Override
    public void removeRatings() {
        this.handle.removeRatings();
    }

    @Override
    public boolean hasSettingsEnabled(IslandFlag islandFlag) {
        return this.handle.hasSettingsEnabled(islandFlag);
    }

    @Override
    public Map<IslandFlag, Byte> getAllSettings() {
        return this.handle.getAllSettings();
    }

    @Override
    public void enableSettings(IslandFlag islandFlag) {
        this.handle.enableSettings(islandFlag);
    }

    @Override
    public void disableSettings(IslandFlag islandFlag) {
        this.handle.disableSettings(islandFlag);
    }

    @Override
    public void setGeneratorPercentage(Key key, int percentage, World.Environment environment) {
        this.handle.setGeneratorPercentage(key, percentage, environment);
    }

    @Override
    public boolean setGeneratorPercentage(Key key, int percentage, World.Environment environment, @Nullable SuperiorPlayer caller, boolean callEvent) {
        return this.handle.setGeneratorPercentage(key, percentage, environment, caller, callEvent);
    }

    @Override
    public int getGeneratorPercentage(Key key, World.Environment environment) {
        return this.handle.getGeneratorPercentage(key, environment);
    }

    @Override
    public Map<String, Integer> getGeneratorPercentages(World.Environment environment) {
        return this.handle.getGeneratorPercentages(environment);
    }

    @Override
    public void setGeneratorAmount(Key key, @Size int amount, World.Environment environment) {
        this.handle.setGeneratorAmount(key, amount, environment);
    }

    @Override
    public void removeGeneratorAmount(Key key, World.Environment environment) {
        this.handle.removeGeneratorAmount(key, environment);
    }

    @Override
    public int getGeneratorAmount(Key key, World.Environment environment) {
        return this.handle.getGeneratorAmount(key, environment);
    }

    @Override
    public int getGeneratorTotalAmount(World.Environment environment) {
        return this.handle.getGeneratorTotalAmount(environment);
    }

    @Override
    public Map<String, Integer> getGeneratorAmounts(World.Environment environment) {
        return this.handle.getGeneratorAmounts(environment);
    }

    @Override
    public Map<Key, Integer> getCustomGeneratorAmounts(World.Environment environment) {
        return this.handle.getCustomGeneratorAmounts(environment);
    }

    @Override
    public void clearGeneratorAmounts(World.Environment environment) {
        this.handle.clearGeneratorAmounts(environment);
    }

    @Nullable
    @Override
    public Key generateBlock(Location location, boolean optimizeCobblestone) {
        return this.handle.generateBlock(location, optimizeCobblestone);
    }

    @Nullable
    @Override
    public Key generateBlock(Location location, World.Environment environment, boolean optimizeCobblestone) {
        return this.handle.generateBlock(location, environment, optimizeCobblestone);
    }

    @Override
    public boolean wasSchematicGenerated(World.Environment environment) {
        return this.handle.wasSchematicGenerated(environment);
    }

    @Override
    public void setSchematicGenerate(World.Environment environment) {
        this.handle.setSchematicGenerate(environment);
    }

    @Override
    public void setSchematicGenerate(World.Environment environment, boolean generated) {
        this.handle.setSchematicGenerate(environment, generated);
    }

    @Override
    public int getGeneratedSchematicsFlag() {
        return this.handle.getGeneratedSchematicsFlag();
    }

    @Override
    public String getSchematicName() {
        return this.handle.getSchematicName();
    }

    @Override
    public int getPosition(SortingType sortingType) {
        return this.handle.getPosition(sortingType);
    }

    @Override
    public IslandChest[] getChest() {
        return this.handle.getChest();
    }

    @Override
    public int getChestSize() {
        return this.handle.getChestSize();
    }

    @Override
    public void setChestRows(int index, int rows) {
        this.handle.setChestRows(index, rows);
    }

    @Override
    public int compareTo(Island o) {
        return this.handle.compareTo(o);
    }

}
