package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.annotations.Size;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.BlockChangeResult;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandBlockFlags;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.island.IslandChunkFlags;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PermissionNode;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
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
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.IslandArea;
import com.bgsoftware.superiorskyblock.core.SBlockPosition;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.WorldInfoImpl;
import com.bgsoftware.superiorskyblock.core.database.bridge.EmptyDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;
import com.bgsoftware.superiorskyblock.core.key.KeyMaps;
import com.bgsoftware.superiorskyblock.core.persistence.EmptyPersistentDataContainer;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.algorithm.SpawnIslandBlocksTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.island.algorithm.SpawnIslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.island.algorithm.SpawnIslandEntitiesTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.island.chunk.DirtyChunksContainer;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.island.privilege.PlayerPrivilegeNode;
import com.bgsoftware.superiorskyblock.island.privilege.PrivilegeNodeAbstract;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.bgsoftware.superiorskyblock.island.top.SortingComparators;
import com.bgsoftware.superiorskyblock.player.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.player.builder.SuperiorPlayerBuilderImpl;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import com.google.common.base.Preconditions;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SpawnIsland implements Island {

    private static final UUID spawnUUID = new UUID(0, 0);
    private static final SSuperiorPlayer ownerPlayer = new SSuperiorPlayer((SuperiorPlayerBuilderImpl)
            SuperiorPlayer.newBuilder().setUniqueId(spawnUUID));
    private static final IslandChest[] EMPTY_ISLAND_CHESTS = new IslandChest[0];
    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final PriorityQueue<SuperiorPlayer> playersInside = new PriorityQueue<>(SortingComparators.PLAYER_NAMES_COMPARATOR);
    private final DirtyChunksContainer dirtyChunksContainer;

    private final BlockPosition center;
    private final World spawnWorld;
    private final WorldInfo spawnWorldInfo;
    private final IslandArea islandArea;
    private final int islandSize;

    private final float homeYaw;
    private final float homePitch;

    private Biome biome = Biome.PLAINS;


    public SpawnIsland() throws ManagerLoadException {
        String spawnLocation = plugin.getSettings().getSpawn().getLocation();
        Location smartCenter = Serializers.LOCATION_SPACED_SERIALIZER.deserialize(spawnLocation);
        assert smartCenter != null;

        String worldName = spawnLocation.split(", ")[0];

        if (smartCenter.getWorld() == null)
            plugin.getProviders().runWorldsListeners(worldName);

        this.spawnWorld = smartCenter.getWorld();

        if (this.spawnWorld == null)
            throw new ManagerLoadException("The spawn location is in invalid world.", ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN);

        this.islandSize = plugin.getSettings().getSpawn().getSize();

        this.center = new SBlockPosition(worldName, smartCenter.getBlockX(), smartCenter.getBlockY(), smartCenter.getBlockZ());
        this.islandArea = new IslandArea(this.center, this.islandSize);
        this.spawnWorldInfo = new WorldInfoImpl(this.spawnWorld.getName(), this.spawnWorld.getEnvironment());

        this.homeYaw = smartCenter.getYaw();
        this.homePitch = smartCenter.getPitch();

        this.dirtyChunksContainer = new DirtyChunksContainer(this);

        BukkitExecutor.sync(() -> biome = getCenter(null /* unused */).getBlock().getBiome());
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
        // Do nothing.
    }

    @Override
    public List<SuperiorPlayer> getIslandMembers(boolean includeOwner) {
        return Collections.emptyList();
    }

    @Override
    public List<SuperiorPlayer> getIslandMembers(PlayerRole... playerRoles) {
        return Collections.emptyList();
    }

    @Override
    public List<SuperiorPlayer> getBannedPlayers() {
        return Collections.emptyList();
    }

    @Override
    public List<SuperiorPlayer> getIslandVisitors() {
        return getIslandVisitors(true);
    }

    @Override
    public List<SuperiorPlayer> getIslandVisitors(boolean vanishPlayers) {
        return Collections.emptyList();
    }

    @Override
    public List<SuperiorPlayer> getAllPlayersInside() {
        return new SequentialListBuilder<SuperiorPlayer>().build(playersInside);
    }

    @Override
    public List<SuperiorPlayer> getUniqueVisitors() {
        return Collections.emptyList();
    }

    @Override
    public List<Pair<SuperiorPlayer, Long>> getUniqueVisitorsWithTimes() {
        return Collections.emptyList();
    }

    @Override
    public void inviteMember(SuperiorPlayer superiorPlayer) {
        // Do nothing.
    }

    @Override
    public void revokeInvite(SuperiorPlayer superiorPlayer) {
        // Do nothing.
    }

    @Override
    public boolean isInvited(SuperiorPlayer superiorPlayer) {
        return false;
    }

    @Override
    public List<SuperiorPlayer> getInvitedPlayers() {
        return Collections.emptyList();
    }

    @Override
    public void addMember(SuperiorPlayer superiorPlayer, PlayerRole playerRole) {
        // Do nothing.
    }

    @Override
    public void kickMember(SuperiorPlayer superiorPlayer) {
        // Do nothing.
    }

    @Override
    public boolean isMember(SuperiorPlayer superiorPlayer) {
        return false;
    }

    @Override
    public void banMember(SuperiorPlayer superiorPlayer) {
        // Do nothing.
    }

    @Override
    public void banMember(SuperiorPlayer superiorPlayer, SuperiorPlayer whom) {
        // Do nothing.
    }

    @Override
    public void unbanMember(SuperiorPlayer superiorPlayer) {
        // Do nothing.
    }

    @Override
    public boolean isBanned(SuperiorPlayer superiorPlayer) {
        return false;
    }

    @Override
    public void addCoop(SuperiorPlayer superiorPlayer) {
        // Do nothing.
    }

    @Override
    public void removeCoop(SuperiorPlayer superiorPlayer) {
        // Do nothing.
    }

    @Override
    public boolean isCoop(SuperiorPlayer superiorPlayer) {
        return false;
    }

    @Override
    public List<SuperiorPlayer> getCoopPlayers() {
        return Collections.emptyList();
    }

    @Override
    public int getCoopLimit() {
        return 0;
    }

    @Override
    public void setCoopLimit(int coopLimit) {
        // Do nothing.
    }

    @Override
    public void setPlayerInside(SuperiorPlayer superiorPlayer, boolean inside) {
        if (inside)
            playersInside.add(superiorPlayer);
        else
            playersInside.remove(superiorPlayer);
    }

    @Override
    public boolean isVisitor(SuperiorPlayer superiorPlayer, boolean includeCoopStatus) {
        return true;
    }

    @Override
    public Location getCenter(World.Environment unused) {
        return center.parse(this.spawnWorld).add(0.5, 0, 0.5);
    }

    @Override
    public BlockPosition getCenterPosition() {
        return this.center;
    }

    @Override
    public Location getTeleportLocation(World.Environment environment) {
        return this.getIslandHome(environment);
    }

    @Override
    public Map<World.Environment, Location> getTeleportLocations() {
        return this.getIslandHomes();
    }

    @Override
    public void setTeleportLocation(Location teleportLocation) {
        this.setIslandHome(teleportLocation);
    }

    @Override
    public void setTeleportLocation(World.Environment environment, @Nullable Location teleportLocation) {
        this.setIslandHome(environment, teleportLocation);
    }

    @Override
    public Location getIslandHome(World.Environment unused) {
        Location center = getCenter(null /*unused*/);
        center.setYaw(this.homeYaw);
        center.setPitch(this.homePitch);
        return center;
    }

    @Override
    public Map<World.Environment, Location> getIslandHomes() {
        Map<World.Environment, Location> map = new HashMap<>();
        map.put(plugin.getSettings().getWorlds().getDefaultWorld(), getIslandHome(null /*unused*/));
        return map;
    }

    @Override
    public void setIslandHome(Location homeLocation) {
        // Do nothing.
    }

    @Override
    public void setIslandHome(World.Environment environment, @Nullable Location homeLocation) {
        // Do nothing.
    }

    @Override
    public Location getVisitorsLocation() {
        return getVisitorsLocation(null /* unused */);
    }

    @Nullable
    @Override
    public Location getVisitorsLocation(World.Environment unused) {
        return this.getIslandHome(null /* unused */);
    }

    @Override
    public void setVisitorsLocation(Location visitorsLocation) {
        // Do nothing.
    }

    @Override
    public Location getMinimum() {
        return this.getMinimumPosition().parse(this.spawnWorld).add(0.5, 0, 0.5);
    }

    @Override
    public BlockPosition getMinimumPosition() {
        return this.center.offset(-this.islandSize, 0, -this.islandSize);
    }

    @Override
    public Location getMinimumProtected() {
        return this.getMinimum();
    }

    @Override
    public BlockPosition getMinimumProtectedPosition() {
        return this.getMinimumPosition();
    }

    @Override
    public Location getMaximum() {
        return this.getMaximumPosition().parse(this.spawnWorld).add(0.5, 0, 0.5);
    }

    @Override
    public BlockPosition getMaximumPosition() {
        return this.center.offset(this.islandSize, 0, this.islandSize);
    }

    @Override
    public Location getMaximumProtected() {
        return this.getMaximum();
    }

    @Override
    public BlockPosition getMaximumProtectedPosition() {
        return this.getMaximumPosition();
    }

    @Override
    public List<Chunk> getAllChunks() {
        return getAllChunks(0);
    }

    @Override
    public List<Chunk> getAllChunks(@IslandChunkFlags int flags) {
        return getAllChunks(plugin.getSettings().getWorlds().getDefaultWorld(), flags);
    }

    @Override
    @Deprecated
    public List<Chunk> getAllChunks(World.Environment environment) {
        return getAllChunks(environment, 0);
    }

    @Override
    public List<Chunk> getAllChunks(World.Environment environment, @IslandChunkFlags int flags) {
        boolean onlyProtected = (flags & IslandChunkFlags.ONLY_PROTECTED) != 0;
        boolean noEmptyChunks = (flags & IslandChunkFlags.NO_EMPTY_CHUNKS) != 0;

        Location min = onlyProtected ? getMinimumProtected() : getMinimum();
        Location max = onlyProtected ? getMaximumProtected() : getMaximum();
        Chunk minChunk = min.getChunk();
        Chunk maxChunk = max.getChunk();

        List<Chunk> chunks = new LinkedList<>();

        for (int x = minChunk.getX(); x <= maxChunk.getX(); x++) {
            for (int z = minChunk.getZ(); z <= maxChunk.getZ(); z++) {
                if (!noEmptyChunks || this.dirtyChunksContainer.isMarkedDirty(ChunkPosition.of(this.spawnWorldInfo, x, z)))
                    chunks.add(minChunk.getWorld().getChunkAt(x, z));
            }
        }


        return Collections.unmodifiableList(chunks);
    }

    @Override
    @Deprecated
    public List<Chunk> getAllChunks(boolean onlyProtected) {
        return getAllChunks(onlyProtected ? IslandChunkFlags.ONLY_PROTECTED : 0);
    }

    @Override
    @Deprecated
    public List<Chunk> getAllChunks(World.Environment environment, boolean onlyProtected) {
        return getAllChunks(environment, onlyProtected ? IslandChunkFlags.ONLY_PROTECTED : 0);
    }

    @Override
    @Deprecated
    public List<Chunk> getAllChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks) {
        int flags = 0;
        if (onlyProtected) flags |= IslandChunkFlags.ONLY_PROTECTED;
        if (noEmptyChunks) flags |= IslandChunkFlags.NO_EMPTY_CHUNKS;
        return getAllChunks(environment, flags);
    }

    @Override
    public List<Chunk> getLoadedChunks() {
        return getLoadedChunks(0);
    }

    @Override
    public List<Chunk> getLoadedChunks(@IslandChunkFlags int flags) {
        return getLoadedChunks(plugin.getSettings().getWorlds().getDefaultWorld(), flags);
    }

    @Override
    public List<Chunk> getLoadedChunks(World.Environment environment) {
        return getLoadedChunks(environment, 0);
    }

    @Override
    public List<Chunk> getLoadedChunks(World.Environment environment, @IslandChunkFlags int flags) {
        boolean onlyProtected = (flags & IslandChunkFlags.ONLY_PROTECTED) != 0;
        boolean noEmptyChunks = (flags & IslandChunkFlags.NO_EMPTY_CHUNKS) != 0;

        Location min = onlyProtected ? getMinimumProtected() : getMinimum();
        Location max = onlyProtected ? getMaximumProtected() : getMaximum();

        List<Chunk> chunks = new LinkedList<>();

        for (int chunkX = min.getBlockX() >> 4; chunkX <= max.getBlockX() >> 4; chunkX++) {
            for (int chunkZ = min.getBlockZ() >> 4; chunkZ <= max.getBlockZ() >> 4; chunkZ++) {
                if (this.spawnWorld.isChunkLoaded(chunkX, chunkZ) && (!noEmptyChunks || this.dirtyChunksContainer.isMarkedDirty(
                        ChunkPosition.of(this.spawnWorldInfo, chunkX, chunkZ)))) {
                    chunks.add(this.spawnWorld.getChunkAt(chunkX, chunkZ));
                }
            }
        }

        return Collections.unmodifiableList(chunks);
    }

    @Override
    public List<Chunk> getLoadedChunks(boolean onlyProtected, boolean noEmptyChunks) {
        int flags = 0;
        if (onlyProtected) flags |= IslandChunkFlags.ONLY_PROTECTED;
        if (noEmptyChunks) flags |= IslandChunkFlags.NO_EMPTY_CHUNKS;
        return getLoadedChunks(flags);
    }

    @Override
    public List<Chunk> getLoadedChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks) {
        int flags = 0;
        if (onlyProtected) flags |= IslandChunkFlags.ONLY_PROTECTED;
        if (noEmptyChunks) flags |= IslandChunkFlags.NO_EMPTY_CHUNKS;
        return getLoadedChunks(environment, flags);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment) {
        return getAllChunksAsync(environment, 0);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, @IslandChunkFlags int flags) {
        return getAllChunksAsync(environment, flags, null);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment,
                                                            @Nullable Consumer<Chunk> onChunkLoad) {
        return getAllChunksAsync(environment, 0, onChunkLoad);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment,
                                                            @IslandChunkFlags int flags,
                                                            @Nullable Consumer<Chunk> onChunkLoad) {
        return IslandUtils.getAllChunksAsync(this, center.getWorld(), flags, ChunkLoadReason.API_REQUEST, onChunkLoad);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected,
                                                            @Nullable Consumer<Chunk> onChunkLoad) {
        return getAllChunksAsync(environment, onlyProtected ? IslandChunkFlags.ONLY_PROTECTED : 0, onChunkLoad);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment,
                                                            boolean onlyProtected, boolean noEmptyChunks,
                                                            @Nullable Consumer<Chunk> onChunkLoad) {
        int flags = 0;
        if (onlyProtected) flags |= IslandChunkFlags.ONLY_PROTECTED;
        if (noEmptyChunks) flags |= IslandChunkFlags.NO_EMPTY_CHUNKS;
        return getAllChunksAsync(environment, flags, onChunkLoad);
    }

    @Override
    public void resetChunks() {
        // Do nothing.
    }

    @Override
    public void resetChunks(@Nullable Runnable onFinish) {
        // Do nothing.
    }

    @Override
    public void resetChunks(World.Environment environment) {
        // Do nothing.
    }

    @Override
    public void resetChunks(World.Environment environment, @Nullable Runnable onFinish) {
        // Do nothing.
    }

    @Override
    public void resetChunks(@IslandChunkFlags int flags) {
        // Do nothing.
    }

    @Override
    public void resetChunks(@IslandChunkFlags int flags, @Nullable Runnable onFinish) {
        // Do nothing.
    }

    @Override
    public void resetChunks(World.Environment environment, @IslandChunkFlags int flags) {
        // Do nothing.
    }

    @Override
    public void resetChunks(World.Environment environment, @IslandChunkFlags int flags, @Nullable Runnable onFinish) {
        // Do nothing.
    }

    @Override
    @Deprecated
    public void resetChunks(World.Environment environment, boolean onlyProtected) {
        // Do nothing.
    }

    @Override
    @Deprecated
    public void resetChunks(World.Environment environment, boolean onlyProtected, @Nullable Runnable onFinish) {
        // Do nothing.
    }

    @Override
    @Deprecated
    public void resetChunks(boolean onlyProtected) {
        // Do nothing.
    }

    @Override
    @Deprecated
    public void resetChunks(boolean onlyProtected, @Nullable Runnable onFinish) {
        // Do nothing.
    }

    @Override
    public boolean isInside(Location location) {
        return location.getWorld().equals(this.spawnWorld) &&
                this.islandArea.intercepts(location.getBlockX(), location.getBlockZ());
    }

    @Override
    public boolean isInside(World world, int chunkX, int chunkZ) {
        return world.equals(this.spawnWorld) && isChunkInside(chunkX, chunkZ);
    }

    public boolean isChunkInside(int chunkX, int chunkZ) {
        IslandArea islandArea = this.islandArea.copy();
        islandArea.rshift(4);
        return islandArea.intercepts(chunkX, chunkZ);
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
        if (!chunk.getWorld().equals(this.spawnWorld))
            return false;

        Location min = getMinimum();
        Location max = getMaximum();

        return (min.getBlockX() >> 4) <= chunk.getX() && (min.getBlockZ() >> 4) <= chunk.getZ() &&
                (max.getBlockX() >> 4) >= chunk.getX() && (max.getBlockZ() >> 4) >= chunk.getZ();
    }

    @Override
    public boolean isNormalEnabled() {
        return false;
    }

    @Override
    public void setNormalEnabled(boolean enabled) {
        // Do nothing.
    }

    @Override
    public boolean isNetherEnabled() {
        return false;
    }

    @Override
    public void setNetherEnabled(boolean enabled) {
        // Do nothing.
    }

    @Override
    public boolean isEndEnabled() {
        return false;
    }

    @Override
    public void setEndEnabled(boolean enabled) {
        // Do nothing.
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
        return (checkForProtection && !plugin.getSettings().getSpawn().isProtected()) || superiorPlayer.hasBypassModeEnabled() ||
                superiorPlayer.hasPermissionWithoutOP("superior.admin.bypass." + islandPrivilege.getName()) ||
                hasPermission(SPlayerRole.guestRole(), islandPrivilege);
    }

    @Override
    public boolean hasPermission(PlayerRole playerRole, IslandPrivilege islandPrivilege) {
        return getRequiredPlayerRole(islandPrivilege).getWeight() <= playerRole.getWeight();
    }

    @Override
    public void setPermission(PlayerRole playerRole, IslandPrivilege islandPrivilege, boolean value) {
        // Do nothing.
    }

    @Override
    public void setPermission(PlayerRole playerRole, IslandPrivilege islandPrivilege) {
        // Do nothing.
    }

    @Override
    public void resetPermissions() {
        // Do nothing.
    }

    @Override
    public void setPermission(SuperiorPlayer superiorPlayer, IslandPrivilege islandPrivilege, boolean value) {
        // Do nothing.
    }

    @Override
    public void resetPermissions(SuperiorPlayer superiorPlayer) {
        // Do nothing.
    }

    @Override
    public PrivilegeNodeAbstract getPermissionNode(SuperiorPlayer superiorPlayer) {
        return PlayerPrivilegeNode.EmptyPlayerPermissionNode.INSTANCE;
    }

    @Override
    public PlayerRole getRequiredPlayerRole(IslandPrivilege islandPrivilege) {
        return plugin.getSettings().getSpawn().getPermissions().contains(islandPrivilege.getName()) ?
                SPlayerRole.guestRole() : SPlayerRole.lastRole();
    }

    @Override
    public Map<SuperiorPlayer, PermissionNode> getPlayerPermissions() {
        return Collections.emptyMap();
    }

    @Override
    public Map<IslandPrivilege, PlayerRole> getRolePermissions() {
        return Collections.emptyMap();
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
        // Do nothing.
    }

    @Override
    public String getRawName() {
        return "";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public void setDescription(String description) {
        // Do nothing.
    }

    @Override
    public void disbandIsland() {
        // Do nothing.
    }

    @Override
    public boolean transferIsland(SuperiorPlayer superiorPlayer) {
        return false;
    }

    @Override
    public void replacePlayers(SuperiorPlayer originalPlayer, SuperiorPlayer newPlayer) {
        // Do nothing.
    }

    @Override
    public void calcIslandWorth(SuperiorPlayer asker) {
        // Do nothing.
    }

    @Override
    public void calcIslandWorth(SuperiorPlayer asker, Runnable callback) {
        // Do nothing.
    }

    @Override
    public IslandCalculationAlgorithm getCalculationAlgorithm() {
        return SpawnIslandCalculationAlgorithm.getInstance();
    }

    @Override
    public void updateBorder() {
        getAllPlayersInside().forEach(superiorPlayer -> superiorPlayer.updateWorldBorder(this));
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
    public void setIslandSize(int islandSize) {
        // Do nothing.
    }

    @Override
    public int getIslandSizeRaw() {
        return islandSize;
    }

    @Override
    public String getDiscord() {
        return "";
    }

    @Override
    public void setDiscord(String discord) {
        // Do nothing.
    }

    @Override
    public String getPaypal() {
        return "";
    }

    @Override
    public void setPaypal(String paypal) {
        // Do nothing.
    }

    @Override
    public Biome getBiome() {
        return biome;
    }

    @Override
    public void setBiome(Biome biome) {
        // Do nothing.
    }

    @Override
    public void setBiome(Biome biome, boolean updateBlocks) {
        // Do nothing.
    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public void setLocked(boolean locked) {
        // Do nothing.
    }

    @Override
    public boolean isIgnored() {
        return false;
    }

    @Override
    public void setIgnored(boolean ignored) {
        // Do nothing.
    }

    @Override
    public void sendMessage(String message, UUID... ignoredMembers) {
        // Do nothing.
    }

    @Override
    public void sendMessage(IMessageComponent messageComponent, Object... args) {
        // Do nothing.
    }

    @Override
    public void sendMessage(IMessageComponent messageComponent, List<UUID> ignoredMembers, Object... args) {
        // Do nothing.
    }

    @Override
    public void sendTitle(String title, String subtitle, int fadeIn, int duration, int fadeOut, UUID... ignoredMembers) {
        // Do nothing.
    }

    @Override
    public void executeCommand(String command, boolean onlyOnlineMembers, UUID... ignoredMembers) {
        // Do nothing.
    }

    @Override
    public boolean isBeingRecalculated() {
        return false;
    }

    @Override
    public void updateLastTime() {
        // Do nothing.
    }

    @Override
    public void setCurrentlyActive() {
        // Do nothing.
    }

    @Override
    public boolean isCurrentlyActive() {
        return true;
    }

    @Override
    public void setCurrentlyActive(boolean active) {
        // Do nothing.
    }

    @Override
    public long getLastTimeUpdate() {
        return -1;
    }

    @Override
    public void setLastTimeUpdate(long lastTimeUpdate) {
        // Do nothing.
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
    public void setBankLimit(BigDecimal bankLimit) {
        // Do nothing.
    }

    @Override
    public BigDecimal getBankLimitRaw() {
        return BigDecimal.valueOf(-1);
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
    public void setLastInterestTime(long lastInterest) {
        // Do nothing.
    }

    @Override
    public long getNextInterest() {
        return -1;
    }

    @Override
    public void handleBlockPlace(Block block) {
        // Do nothing.
    }

    @Override
    public BlockChangeResult handleBlockPlaceWithResult(Block block) {
        return BlockChangeResult.SUCCESS;
    }

    @Override
    public void handleBlockPlace(Key key) {
        // Do nothing.
    }

    @Override
    public BlockChangeResult handleBlockPlaceWithResult(Key key) {
        return BlockChangeResult.SPAWN_ISLAND;
    }

    @Override
    public void handleBlockPlace(Block block, @Size int amount) {
        // Do nothing.
    }

    @Override
    public BlockChangeResult handleBlockPlaceWithResult(Block block, @Size int amount) {
        return BlockChangeResult.SPAWN_ISLAND;
    }

    @Override
    public void handleBlockPlace(Key key, @Size int amount) {
        // Do nothing.
    }

    @Override
    public BlockChangeResult handleBlockPlaceWithResult(Key key, @Size int amount) {
        return BlockChangeResult.SPAWN_ISLAND;
    }

    @Override
    public void handleBlockPlace(Block block, @Size int amount, @IslandBlockFlags int flags) {
        // Do nothing.
    }

    @Override
    public BlockChangeResult handleBlockPlaceWithResult(Block block, @Size int amount, int flags) {
        return BlockChangeResult.SPAWN_ISLAND;
    }

    @Override
    public void handleBlockPlace(Key key, @Size int amount, @IslandBlockFlags int flags) {
        // Do nothing.
    }

    @Override
    public BlockChangeResult handleBlockPlaceWithResult(Key key, @Size int amount, @IslandBlockFlags int flags) {
        return BlockChangeResult.SPAWN_ISLAND;
    }

    @Override
    @Deprecated
    public void handleBlockPlace(Block block, @Size int amount, boolean save) {
        // Do nothing.
    }

    @Override
    @Deprecated
    public void handleBlockPlace(Key key, @Size int amount, boolean save) {
        // Do nothing.
    }

    @Override
    @Deprecated
    public void handleBlockPlace(Key key, BigInteger amount, boolean save) {
        // Do nothing.
    }

    @Override
    @Deprecated
    public void handleBlockPlace(Key key, BigInteger amount, boolean save, boolean updateLastTimeStatus) {
        // Do nothing.
    }

    @Override
    public void handleBlocksPlace(Map<Key, Integer> blocks) {
        // Do nothing.
    }

    @Override
    public Map<Key, BlockChangeResult> handleBlocksPlaceWithResult(Map<Key, Integer> blocks) {
        return KeyMaps.createEmptyMap();
    }

    @Override
    public void handleBlocksPlace(Map<Key, Integer> blocks, int flags) {
        // Do nothing.
    }

    @Override
    public Map<Key, BlockChangeResult> handleBlocksPlaceWithResult(Map<Key, Integer> blocks, int flags) {
        return KeyMaps.createEmptyMap();
    }

    @Override
    public void handleBlockBreak(Block block) {
        // Do nothing.
    }

    @Override
    public BlockChangeResult handleBlockBreakWithResult(Block block) {
        return BlockChangeResult.SPAWN_ISLAND;
    }

    @Override
    public void handleBlockBreak(Key key) {
        // Do nothing.
    }

    @Override
    public BlockChangeResult handleBlockBreakWithResult(Key key) {
        return BlockChangeResult.SPAWN_ISLAND;
    }

    @Override
    public void handleBlockBreak(Block block, @Size int amount) {
        // Do nothing.
    }

    @Override
    public BlockChangeResult handleBlockBreakWithResult(Block block, @Size int amount) {
        return BlockChangeResult.SPAWN_ISLAND;
    }

    @Override
    public void handleBlockBreak(Key key, @Size int amount) {
        // Do nothing.
    }

    @Override
    public BlockChangeResult handleBlockBreakWithResult(Key key, @Size int amount) {
        return BlockChangeResult.SPAWN_ISLAND;
    }

    @Override
    public void handleBlockBreak(Block block, @Size int amount, @IslandBlockFlags int flags) {
        // Do nothing.
    }

    @Override
    public BlockChangeResult handleBlockBreakWithResult(Block block, @Size int amount, int flags) {
        return BlockChangeResult.SPAWN_ISLAND;
    }

    @Override
    public void handleBlockBreak(Key key, @Size int amount, @IslandBlockFlags int flags) {
        // Do nothing.
    }

    @Override
    public BlockChangeResult handleBlockBreakWithResult(Key key, @Size int amount, int flags) {
        return BlockChangeResult.SPAWN_ISLAND;
    }

    @Override
    @Deprecated
    public void handleBlockBreak(Block block, @Size int amount, boolean save) {
        // Do nothing.
    }

    @Override
    @Deprecated
    public void handleBlockBreak(Key key, @Size int amount, boolean save) {
        // Do nothing.
    }

    @Override
    @Deprecated
    public void handleBlockBreak(Key key, BigInteger amount, boolean save) {
        // Do nothing.
    }

    @Override
    public void handleBlocksBreak(Map<Key, Integer> blocks) {
        // Do nothing.
    }

    @Override
    public Map<Key, BlockChangeResult> handleBlocksBreakWithResult(Map<Key, Integer> blocks) {
        return KeyMaps.createEmptyMap();
    }

    @Override
    public void handleBlocksBreak(Map<Key, Integer> blocks, @IslandBlockFlags int flags) {
        // Do nothing.
    }

    @Override
    public Map<Key, BlockChangeResult> handleBlocksBreakWithResult(Map<Key, Integer> blocks, int flags) {
        return KeyMaps.createEmptyMap();
    }

    @Override
    public boolean isChunkDirty(World world, int chunkX, int chunkZ) {
        Preconditions.checkNotNull(world, "world parameter cannot be null.");
        Preconditions.checkArgument(isInside(world, chunkX, chunkZ), "Chunk must be within the island boundaries.");
        return this.dirtyChunksContainer.isMarkedDirty(ChunkPosition.of(this.spawnWorldInfo, chunkX, chunkZ));
    }

    @Override
    public boolean isChunkDirty(String worldName, int chunkX, int chunkZ) {
        Preconditions.checkNotNull(worldName, "worldName parameter cannot be null.");
        Preconditions.checkArgument(this.spawnWorldInfo.getName().equals(worldName) && isChunkInside(chunkX, chunkZ),
                "Chunk must be within the island boundaries.");
        return this.dirtyChunksContainer.isMarkedDirty(ChunkPosition.of(this.spawnWorldInfo, chunkX, chunkZ));
    }

    @Override
    public void markChunkDirty(World world, int chunkX, int chunkZ, boolean save) {
        Preconditions.checkNotNull(world, "world parameter cannot be null.");
        Preconditions.checkArgument(isInside(world, chunkX, chunkZ), "Chunk must be within the island boundaries.");
        this.dirtyChunksContainer.markDirty(ChunkPosition.of(this.spawnWorldInfo, chunkX, chunkZ), save);
    }

    @Override
    public void markChunkEmpty(World world, int chunkX, int chunkZ, boolean save) {
        Preconditions.checkNotNull(world, "world parameter cannot be null.");
        Preconditions.checkArgument(isInside(world, chunkX, chunkZ), "Chunk must be within the island boundaries.");
        this.dirtyChunksContainer.markEmpty(ChunkPosition.of(this.spawnWorldInfo, chunkX, chunkZ), save);
    }

    @Override
    public BigInteger getBlockCountAsBigInteger(Key key) {
        return BigInteger.ZERO;
    }

    @Override
    public Map<Key, BigInteger> getBlockCountsAsBigInteger() {
        return Collections.emptyMap();
    }

    @Override
    public BigInteger getExactBlockCountAsBigInteger(Key key) {
        return BigInteger.ZERO;
    }

    @Override
    public void clearBlockCounts() {
        // Do nothing.
    }

    @Override
    public IslandBlocksTrackerAlgorithm getBlocksTracker() {
        return SpawnIslandBlocksTrackerAlgorithm.getInstance();
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
        // Do nothing.
    }

    @Override
    public BigDecimal getBonusLevel() {
        return BigDecimal.ZERO;
    }

    @Override
    public void setBonusLevel(BigDecimal bonusLevel) {
        // Do nothing.
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
        // Do nothing.
    }

    @Override
    public Map<String, Integer> getUpgrades() {
        return Collections.emptyMap();
    }

    @Override
    public void syncUpgrades() {
        // Do nothing.
    }

    @Override
    public void updateUpgrades() {
        // Do nothing.
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
    public void setCropGrowthMultiplier(double cropGrowth) {
        // Do nothing.
    }

    @Override
    public double getCropGrowthRaw() {
        return 1;
    }

    @Override
    public double getSpawnerRatesMultiplier() {
        return 1;
    }

    @Override
    public void setSpawnerRatesMultiplier(double spawnerRates) {
        // Do nothing.
    }

    @Override
    public double getSpawnerRatesRaw() {
        return 1;
    }

    @Override
    public double getMobDropsMultiplier() {
        return 1;
    }

    @Override
    public void setMobDropsMultiplier(double mobDrops) {
        // Do nothing.
    }

    @Override
    public double getMobDropsRaw() {
        return 1;
    }

    @Override
    public int getBlockLimit(Key key) {
        return -1;
    }

    @Override
    public int getExactBlockLimit(Key key) {
        return -1;
    }

    @Override
    public Key getBlockLimitKey(Key key) {
        return key;
    }

    @Override
    public Map<Key, Integer> getBlocksLimits() {
        return Collections.emptyMap();
    }

    @Override
    public Map<Key, Integer> getCustomBlocksLimits() {
        return Collections.emptyMap();
    }

    @Override
    public void clearBlockLimits() {
        // Do nothing.
    }

    @Override
    public void setBlockLimit(Key key, int limit) {
        // Do nothing.
    }

    @Override
    public void removeBlockLimit(Key key) {
        // Do nothing.
    }

    @Override
    public boolean hasReachedBlockLimit(Key key) {
        return false;
    }

    @Override
    public boolean hasReachedBlockLimit(Key key, @Size int amount) {
        return false;
    }

    @Override
    public int getEntityLimit(EntityType entityType) {
        return -1;
    }

    @Override
    public int getEntityLimit(Key key) {
        return -1;
    }

    @Override
    public Map<Key, Integer> getEntitiesLimitsAsKeys() {
        return Collections.emptyMap();
    }

    @Override
    public Map<Key, Integer> getCustomEntitiesLimits() {
        return Collections.emptyMap();
    }

    @Override
    public void clearEntitiesLimits() {
        // Do nothing.
    }

    @Override
    public void setEntityLimit(EntityType entityType, int limit) {
        // Do nothing.
    }

    @Override
    public void setEntityLimit(Key key, int limit) {
        // Do nothing.
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
    public CompletableFuture<Boolean> hasReachedEntityLimit(EntityType entityType, @Size int amount) {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(Key key, @Size int amount) {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public IslandEntitiesTrackerAlgorithm getEntitiesTracker() {
        return SpawnIslandEntitiesTrackerAlgorithm.getInstance();
    }

    @Override
    public int getTeamLimit() {
        return -1;
    }

    @Override
    public void setTeamLimit(int teamLimit) {
        // Do nothing.
    }

    @Override
    public int getTeamLimitRaw() {
        return 0;
    }

    @Override
    public int getWarpsLimit() {
        return -1;
    }

    @Override
    public void setWarpsLimit(int warpsLimit) {
        // Do nothing.
    }

    @Override
    public int getWarpsLimitRaw() {
        return -1;
    }

    @Override
    public void setPotionEffect(PotionEffectType type, int level) {
        // Do nothing.
    }

    @Override
    public void removePotionEffect(PotionEffectType type) {
        // Do nothing.
    }

    @Override
    public int getPotionEffectLevel(PotionEffectType type) {
        return 0;
    }

    @Override
    public Map<PotionEffectType, Integer> getPotionEffects() {
        return Collections.emptyMap();
    }

    @Override
    public void applyEffects(SuperiorPlayer superiorPlayer) {
        // Do nothing.
    }

    @Override
    public void applyEffects() {
        // Do nothing.
    }

    @Override
    public void removeEffects(SuperiorPlayer superiorPlayer) {
        // Do nothing.
    }

    @Override
    public void removeEffects() {
        // Do nothing.
    }

    @Override
    public void clearEffects() {
        // Do nothing.
    }

    @Override
    public void setRoleLimit(PlayerRole playerRole, int limit) {
        // Do nothing.
    }

    @Override
    public void removeRoleLimit(PlayerRole playerRole) {
        // Do nothing.
    }

    @Override
    public int getRoleLimit(PlayerRole playerRole) {
        return -1;
    }

    @Override
    public int getRoleLimitRaw(PlayerRole playerRole) {
        return -1;
    }

    @Override
    public Map<PlayerRole, Integer> getRoleLimits() {
        return Collections.emptyMap();
    }

    @Override
    public Map<PlayerRole, Integer> getCustomRoleLimits() {
        return Collections.emptyMap();
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
        // Do nothing.
    }

    @Override
    public void deleteCategory(WarpCategory warpCategory) {
        // Do nothing.
    }

    @Override
    public Map<String, WarpCategory> getWarpCategories() {
        return Collections.emptyMap();
    }

    @Override
    public IslandWarp createWarp(String name, Location location, WarpCategory warpCategory) {
        return null;
    }

    @Override
    public void renameWarp(IslandWarp islandWarp, String newName) {
        // Do nothing.
    }

    @Override
    public IslandWarp getWarp(Location location) {
        return null;
    }

    @Override
    public IslandWarp getWarp(String name) {
        return null;
    }

    @Override
    public void warpPlayer(SuperiorPlayer superiorPlayer, String warp) {
        // Do nothing.
    }

    @Override
    public void deleteWarp(SuperiorPlayer superiorPlayer, Location location) {
        // Do nothing.
    }

    @Override
    public void deleteWarp(String name) {
        // Do nothing.
    }

    @Override
    public Map<String, IslandWarp> getIslandWarps() {
        return Collections.emptyMap();
    }

    @Override
    public Rating getRating(SuperiorPlayer superiorPlayer) {
        return Rating.UNKNOWN;
    }

    @Override
    public void setRating(SuperiorPlayer superiorPlayer, Rating rating) {
        // Do nothing.
    }

    @Override
    public void removeRating(SuperiorPlayer superiorPlayer) {
        // Do nothing.
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
        return Collections.emptyMap();
    }

    @Override
    public void removeRatings() {
        // Do nothing.
    }

    @Override
    public boolean hasSettingsEnabled(IslandFlag islandFlag) {
        return plugin.getSettings().getSpawn().getSettings().contains(islandFlag.getName());
    }

    @Override
    public Map<IslandFlag, Byte> getAllSettings() {
        return Collections.emptyMap();
    }

    @Override
    public void enableSettings(IslandFlag islandFlag) {
        // Do nothing.
    }

    @Override
    public void disableSettings(IslandFlag islandFlag) {
        // Do nothing.
    }

    @Override
    public void setGeneratorPercentage(Key key, int percentage, World.Environment environment) {
        // Do nothing.
    }

    @Override
    public boolean setGeneratorPercentage(Key key, int percentage, World.Environment environment,
                                          @Nullable SuperiorPlayer caller, boolean callEvent) {
        return true;
    }

    @Override
    public int getGeneratorPercentage(Key key, World.Environment environment) {
        return 0;
    }

    @Override
    public Map<String, Integer> getGeneratorPercentages(World.Environment environment) {
        return Collections.emptyMap();
    }

    @Override
    public void setGeneratorAmount(Key key, @Size int amount, World.Environment environment) {
        // Do nothing.
    }

    @Override
    public void removeGeneratorAmount(Key key, World.Environment environment) {
        // Do nothing.
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
        return Collections.emptyMap();
    }

    @Override
    public Map<Key, Integer> getCustomGeneratorAmounts(World.Environment environment) {
        return Collections.emptyMap();
    }

    @Override
    public void clearGeneratorAmounts(World.Environment environment) {
        // Do nothing.
    }

    @Nullable
    @Override
    public Key generateBlock(Location location, boolean optimizeCobblestone) {
        return null;
    }

    @Nullable
    @Override
    public Key generateBlock(Location location, World.Environment environment, boolean optimizeCobblestone) {
        return null;
    }

    @Override
    public boolean wasSchematicGenerated(World.Environment environment) {
        return true;
    }

    @Override
    public void setSchematicGenerate(World.Environment environment) {
        // Do nothing.
    }

    @Override
    public void setSchematicGenerate(World.Environment environment, boolean generated) {
        // Do nothing.
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
        return EMPTY_ISLAND_CHESTS;
    }

    @Override
    public int getChestSize() {
        return 0;
    }

    @Override
    public void setChestRows(int index, int rows) {
        // Do nothing.
    }

    @Override
    public int getCoopLimitRaw() {
        return -1;
    }

    @Override
    public DatabaseBridge getDatabaseBridge() {
        return EmptyDatabaseBridge.getInstance();
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return EmptyPersistentDataContainer.getInstance();
    }

    @Override
    public boolean isPersistentDataContainerEmpty() {
        return true;
    }

    @Override
    public void savePersistentDataContainer() {
        // Do nothing.
    }

    @Override
    public void completeMission(Mission<?> mission) {
        // Do nothing.
    }

    @Override
    public void resetMission(Mission<?> mission) {
        // Do nothing.
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
    public void setAmountMissionCompleted(Mission<?> mission, int finishCount) {
        // Do nothing.
    }

    @Override
    public List<Mission<?>> getCompletedMissions() {
        return Collections.emptyList();
    }

    @Override
    public Map<Mission<?>, Integer> getCompletedMissionsWithAmounts() {
        return Collections.emptyMap();
    }

    @Override
    public int compareTo(Island o) {
        return 0;
    }

}
