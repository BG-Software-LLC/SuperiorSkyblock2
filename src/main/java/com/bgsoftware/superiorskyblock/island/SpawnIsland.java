package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.annotations.Size;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.enums.MemberRemoveReason;
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
import com.bgsoftware.superiorskyblock.api.island.cache.IslandCache;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.api.wrappers.WorldPosition;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.IslandArea;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.SBlockPosition;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.WorldInfoImpl;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateSet;
import com.bgsoftware.superiorskyblock.core.database.bridge.EmptyDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventType;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsDispatcher;
import com.bgsoftware.superiorskyblock.core.key.map.KeyMaps;
import com.bgsoftware.superiorskyblock.core.persistence.EmptyPersistentDataContainer;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.algorithm.SpawnIslandBlocksTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.island.algorithm.SpawnIslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.island.algorithm.SpawnIslandEntitiesTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.island.cache.IslandCacheImpl;
import com.bgsoftware.superiorskyblock.island.chunk.DirtyChunksContainer;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.island.privilege.PlayerPrivilegeNode;
import com.bgsoftware.superiorskyblock.island.privilege.PrivilegeNodeAbstract;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.bgsoftware.superiorskyblock.island.top.SortingComparators;
import com.bgsoftware.superiorskyblock.player.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.player.builder.SuperiorPlayerBuilderImpl;
import com.bgsoftware.superiorskyblock.world.Dimensions;
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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SpawnIsland implements Island {

    private static final UUID spawnUUID = new UUID(0, 0);
    private static final LazyReference<SSuperiorPlayer> ownerPlayer = new LazyReference<SSuperiorPlayer>() {
        @Override
        protected SSuperiorPlayer create() {
            return new SSuperiorPlayer((SuperiorPlayerBuilderImpl) SuperiorPlayer.newBuilder().setUniqueId(spawnUUID));
        }
    };
    private static final IslandChest[] EMPTY_ISLAND_CHESTS = new IslandChest[0];
    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static EnumerateSet<IslandFlag> DEFAULT_SPAWN_FLAGS_CACHE;
    private static EnumerateSet<IslandPrivilege> DEFAULT_SPAWN_PRIVILEGES_CACHE;

    public static void registerListeners(PluginEventsDispatcher dispatcher) {
        dispatcher.registerCallback(PluginEventType.SETTINGS_UPDATE_EVENT, SpawnIsland::onSettingsUpdate);
    }

    private static void onSettingsUpdate() {
        DEFAULT_SPAWN_FLAGS_CACHE = new EnumerateSet<>(IslandFlag.values());
        plugin.getSettings().getSpawn().getSettings().forEach(flagName -> {
            try {
                DEFAULT_SPAWN_FLAGS_CACHE.add(IslandFlag.getByName(flagName));
            } catch (Throwable ignored) {
            }
        });

        DEFAULT_SPAWN_PRIVILEGES_CACHE = new EnumerateSet<>(IslandPrivilege.values());
        plugin.getSettings().getSpawn().getPermissions().forEach(privilegeName -> {
            try {
                DEFAULT_SPAWN_PRIVILEGES_CACHE.add(IslandPrivilege.getByName(privilegeName));
            } catch (Throwable ignored) {
            }
        });
    }

    private final PriorityQueue<SuperiorPlayer> playersInside = new PriorityQueue<>(SortingComparators.PLAYER_NAMES_COMPARATOR);
    private final DirtyChunksContainer dirtyChunksContainer;
    private final LazyReference<IslandCache> islandCache = new LazyReference<IslandCache>() {
        @Override
        protected IslandCache create() {
            return new IslandCacheImpl(SpawnIsland.this);
        }
    };

    private final BlockPosition center;
    private final World spawnWorld;
    private final WorldInfo spawnWorldInfo;
    private final int islandSize;
    private final IslandArea islandArea = new IslandArea();

    private Biome biome = Biome.PLAINS;


    public SpawnIsland() throws ManagerLoadException {
        String spawnLocation = plugin.getSettings().getSpawn().getLocation();
        Location centerLocation = Serializers.LOCATION_SPACED_SERIALIZER.deserialize(spawnLocation);
        if (centerLocation == null) {
            throw new ManagerLoadException("The spawn location could not be parsed", ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN);
        }

        String worldName = spawnLocation.split(", ")[0];

        if (centerLocation.getWorld() == null)
            plugin.getProviders().runWorldsListeners(worldName);

        this.spawnWorld = centerLocation.getWorld();

        if (this.spawnWorld == null)
            throw new ManagerLoadException("The spawn location is in invalid world.", ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN);

        this.islandSize = plugin.getSettings().getSpawn().getSize();

        this.center = SBlockPosition.of(centerLocation);
        this.islandArea.update(this.center, this.islandSize);
        this.spawnWorldInfo = new WorldInfoImpl(this.spawnWorld.getName(), Dimensions.fromEnvironment(this.spawnWorld.getEnvironment()));

        this.dirtyChunksContainer = new DirtyChunksContainer(this);

        BukkitExecutor.sync(() -> biome = getCenter(null /* unused */).getBlock().getBiome());
    }

    @Override
    public SuperiorPlayer getOwner() {
        return ownerPlayer.get();
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
    public IslandCache getCache() {
        return this.islandCache.get();
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
    @Deprecated
    public void kickMember(SuperiorPlayer superiorPlayer) {
        // Do nothing.
    }

    @Override
    public void removeMember(SuperiorPlayer superiorPlayer, MemberRemoveReason memberRemoveReason) {
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
    public Location getCenter(Dimension unused) {
        return this.center.toWorldPosition().toLocation(this.spawnWorld);
    }

    @Override
    public BlockPosition getCenterPosition() {
        return this.center;
    }

    @Override
    public CompletableFuture<World> accessIslandWorld(Dimension unused) {
        return CompletableFuture.completedFuture(this.spawnWorld);
    }

    public World getSpawnWorld() {
        return spawnWorld;
    }

    public WorldInfo getSpawnWorldInfo() {
        return spawnWorldInfo;
    }

    @Override
    public Location getIslandHome(Dimension unused) {
        return getCenter(null /*unused*/);
    }

    @Override
    public WorldPosition getIslandHomePosition(Dimension unused) {
        return getCenterPosition().toWorldPosition();
    }

    @Override
    public Map<Dimension, Location> getIslandHomesAsDimensions() {
        return Collections.singletonMap(
                plugin.getSettings().getWorlds().getDefaultWorldDimension(),
                getIslandHome(null /*unused*/));
    }

    @Override
    @Deprecated
    public Map<Dimension, WorldPosition> getIslandHomes() {
        return Collections.singletonMap(
                plugin.getSettings().getWorlds().getDefaultWorldDimension(),
                getIslandHomePosition(null /*unused*/));
    }

    @Override
    public void setIslandHome(Location homeLocation) {
        // Do nothing.
    }

    @Override
    public void setIslandHome(Dimension dimension, Location homeLocation) {
        // Do nothing.
    }

    @Override
    public void setIslandHome(Dimension dimension, WorldPosition homePosition) {
        // Do nothing.
    }

    @Override
    public Location getVisitorsLocation(Dimension unused) {
        return this.getIslandHome(null /*unused*/);
    }

    @Override
    public WorldPosition getVisitorsPosition(Dimension unused) {
        return getIslandHomePosition(null /*unused*/);
    }

    @Override
    public void setVisitorsLocation(Location visitorsLocation) {
        // Do nothing.
    }

    @Override
    public void setVisitorsLocation(Dimension dimension, WorldPosition visitorsPosition) {
        // Do nothing.
    }

    @Override
    public Location getMinimum() {
        return this.getMinimumPosition().toWorldPosition().toLocation(this.spawnWorld);
    }

    @Override
    public BlockPosition getMinimumPosition() {
        return getCenterPosition().offset(-this.islandSize, 0, -this.islandSize);
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
        return this.getMaximumPosition().toWorldPosition().toLocation(this.spawnWorld);
    }

    @Override
    public BlockPosition getMaximumPosition() {
        return getCenterPosition().offset(this.islandSize, 0, this.islandSize);
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
        return getAllChunks(plugin.getSettings().getWorlds().getDefaultWorldDimension(), flags);
    }

    @Override
    @Deprecated
    public List<Chunk> getAllChunks(Dimension unused) {
        return getAllChunks((Dimension) null /*unused*/, 0);
    }

    @Override
    public List<Chunk> getAllChunks(Dimension unused, @IslandChunkFlags int flags) {
        boolean onlyProtected = (flags & IslandChunkFlags.ONLY_PROTECTED) != 0;
        boolean noEmptyChunks = (flags & IslandChunkFlags.NO_EMPTY_CHUNKS) != 0;

        Location min = onlyProtected ? getMinimumProtected() : getMinimum();
        Location max = onlyProtected ? getMaximumProtected() : getMaximum();
        Chunk minChunk = min.getChunk();
        Chunk maxChunk = max.getChunk();

        List<Chunk> chunks = new LinkedList<>();

        for (int x = minChunk.getX(); x <= maxChunk.getX(); x++) {
            for (int z = minChunk.getZ(); z <= maxChunk.getZ(); z++) {
                boolean addChunk;
                try (ChunkPosition chunkPosition = ChunkPosition.of(this.spawnWorldInfo, x, z)) {
                    addChunk = !noEmptyChunks || this.dirtyChunksContainer.isMarkedDirty(chunkPosition);
                }
                if (addChunk)
                    chunks.add(minChunk.getWorld().getChunkAt(x, z));
            }
        }


        return Collections.unmodifiableList(chunks);
    }

    @Override
    public List<Chunk> getLoadedChunks() {
        return getLoadedChunks(0);
    }

    @Override
    public List<Chunk> getLoadedChunks(@IslandChunkFlags int flags) {
        return getLoadedChunks(plugin.getSettings().getWorlds().getDefaultWorldDimension(), flags);
    }

    @Override
    public List<Chunk> getLoadedChunks(Dimension unused) {
        return getLoadedChunks((Dimension) null /*unused*/, 0);
    }

    @Override
    public List<Chunk> getLoadedChunks(Dimension unused, @IslandChunkFlags int flags) {
        boolean onlyProtected = (flags & IslandChunkFlags.ONLY_PROTECTED) != 0;
        boolean noEmptyChunks = (flags & IslandChunkFlags.NO_EMPTY_CHUNKS) != 0;

        Location min = onlyProtected ? getMinimumProtected() : getMinimum();
        Location max = onlyProtected ? getMaximumProtected() : getMaximum();

        List<Chunk> chunks = new LinkedList<>();

        for (int chunkX = min.getBlockX() >> 4; chunkX <= max.getBlockX() >> 4; chunkX++) {
            for (int chunkZ = min.getBlockZ() >> 4; chunkZ <= max.getBlockZ() >> 4; chunkZ++) {
                boolean addChunk;
                try (ChunkPosition chunkPosition = ChunkPosition.of(this.spawnWorldInfo, chunkX, chunkZ)) {
                    addChunk = this.spawnWorld.isChunkLoaded(chunkX, chunkZ) &&
                            (!noEmptyChunks || this.dirtyChunksContainer.isMarkedDirty(chunkPosition));
                }
                if (addChunk)
                    chunks.add(this.spawnWorld.getChunkAt(chunkX, chunkZ));
            }
        }

        return Collections.unmodifiableList(chunks);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(Dimension unused) {
        return getAllChunksAsync((Dimension) null /*unused*/, 0);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(Dimension unused, @IslandChunkFlags int flags) {
        return getAllChunksAsync((Dimension) null /*unused*/, flags, null);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(Dimension unused,
                                                            @Nullable Consumer<Chunk> onChunkLoad) {
        return getAllChunksAsync((Dimension) null /*unused*/, 0, onChunkLoad);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(Dimension unused,
                                                            @IslandChunkFlags int flags,
                                                            @Nullable Consumer<Chunk> onChunkLoad) {
        return IslandUtils.getAllChunksAsync(this, spawnWorldInfo, flags, ChunkLoadReason.API_REQUEST, onChunkLoad);
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
    public void resetChunks(Dimension dimension) {
        // Do nothing.
    }

    @Override
    public void resetChunks(Dimension dimension, @Nullable Runnable onFinish) {
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
    public void resetChunks(Dimension dimension, @IslandChunkFlags int flags) {
        // Do nothing.
    }

    @Override
    public void resetChunks(Dimension dimension, @IslandChunkFlags int flags, @Nullable Runnable onFinish) {
        // Do nothing.
    }

    @Override
    public boolean isInside(Location location) {
        return isInside(location, 0D);
    }

    @Override
    public boolean isInside(Location location, int extraRadius) {
        return isInside(location, (double) extraRadius);
    }

    @Override
    public boolean isInside(Location location, double extraRadius) {
        World bukkitWorld = location.getWorld();
        if (bukkitWorld == null || !bukkitWorld.equals(this.spawnWorld))
            return false;

        return this.islandArea.expandAndIntercepts(location.getBlockX(), location.getBlockZ(), extraRadius);
    }

    @Override
    public boolean isInside(BlockPosition blockPosition) {
        return isInside(blockPosition, 0D);
    }

    @Override
    public boolean isInside(BlockPosition blockPosition, int extraRadius) {
        return isInside(blockPosition, (double) extraRadius);
    }

    @Override
    public boolean isInside(BlockPosition blockPosition, double extraRadius) {
        return this.islandArea.expandAndIntercepts(blockPosition.getX(), blockPosition.getZ(), extraRadius);
    }

    @Override
    public boolean isInside(WorldPosition worldPosition) {
        return isInside(worldPosition, 0D);
    }

    @Override
    public boolean isInside(WorldPosition worldPosition, int extraRadius) {
        return isInside(worldPosition, (double) extraRadius);
    }

    @Override
    public boolean isInside(WorldPosition worldPosition, double extraRadius) {
        return this.islandArea.expandAndIntercepts(worldPosition.getX(), worldPosition.getZ(), extraRadius);
    }

    @Override
    public boolean isInside(Chunk chunk) {
        return isInside(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    @Override
    public boolean isInside(World world, int chunkX, int chunkZ) {
        return isInside(world, chunkX, chunkZ, 0D);
    }

    @Override
    public boolean isInside(World world, int chunkX, int chunkZ, int extraRadius) {
        return isInside(world, chunkX, chunkZ, (double) extraRadius);
    }

    @Override
    public boolean isInside(World world, int chunkX, int chunkZ, double extraRadius) {
        return world.equals(this.spawnWorld) && isInside(chunkX, chunkZ, extraRadius);
    }

    @Override
    public boolean isInside(WorldInfo worldInfo, int chunkX, int chunkZ) {
        return isInside(worldInfo, chunkX, chunkZ, 0D);
    }

    @Override
    public boolean isInside(WorldInfo worldInfo, int chunkX, int chunkZ, int extraRadius) {
        return isInside(worldInfo, chunkX, chunkZ, (double) extraRadius);
    }

    @Override
    public boolean isInside(WorldInfo worldInfo, int chunkX, int chunkZ, double extraRadius) {
        return this.spawnWorldInfo.getName().equals(worldInfo.getName()) && isInside(chunkX, chunkZ, extraRadius);
    }

    @Override
    public boolean isInside(int chunkX, int chunkZ) {
        return isInside(chunkX, chunkZ, 0D);
    }

    @Override
    public boolean isInside(int chunkX, int chunkZ, int extraRadius) {
        return isInside(chunkX, chunkZ, (double) extraRadius);
    }

    @Override
    public boolean isInside(int chunkX, int chunkZ, double extraRadius) {
        return this.islandArea.expandRshiftAndIntercepts(chunkX, chunkZ, extraRadius, 4);
    }

    @Override
    public boolean isInsideRange(Location location) {
        return isInside(location);
    }

    @Override
    public boolean isInsideRange(Location location, int extraRadius) {
        return isInside(location, extraRadius);
    }

    @Override
    public boolean isInsideRange(Location location, double extraRadius) {
        return isInside(location, extraRadius);
    }

    @Override
    public boolean isInsideRange(BlockPosition blockPosition) {
        return isInside(blockPosition);
    }

    @Override
    public boolean isInsideRange(BlockPosition blockPosition, int extraRadius) {
        return isInside(blockPosition, extraRadius);
    }

    @Override
    public boolean isInsideRange(BlockPosition blockPosition, double extraRadius) {
        return isInside(blockPosition, extraRadius);
    }

    @Override
    public boolean isInsideRange(WorldPosition worldPosition) {
        return isInside(worldPosition);
    }

    @Override
    public boolean isInsideRange(WorldPosition worldPosition, int extraRadius) {
        return isInside(worldPosition, extraRadius);
    }

    @Override
    public boolean isInsideRange(WorldPosition worldPosition, double extraRadius) {
        return isInside(worldPosition, extraRadius);
    }

    @Override
    public boolean isInsideRange(Chunk chunk) {
        return isInside(chunk);
    }

    @Override
    public boolean isInsideRange(World world, int chunkX, int chunkZ) {
        return isInside(world, chunkX, chunkZ);
    }

    @Override
    public boolean isInsideRange(World world, int chunkX, int chunkZ, int extraRadius) {
        return isInside(world, chunkX, chunkZ, extraRadius);
    }

    @Override
    public boolean isInsideRange(World world, int chunkX, int chunkZ, double extraRadius) {
        return isInside(world, chunkX, chunkZ, extraRadius);
    }

    @Override
    public boolean isInsideRange(WorldInfo worldInfo, int chunkX, int chunkZ) {
        return isInside(worldInfo, chunkX, chunkZ);
    }

    @Override
    public boolean isInsideRange(WorldInfo worldInfo, int chunkX, int chunkZ, int extraRadius) {
        return isInside(worldInfo, chunkX, chunkZ, extraRadius);
    }

    @Override
    public boolean isInsideRange(WorldInfo worldInfo, int chunkX, int chunkZ, double extraRadius) {
        return isInside(worldInfo, chunkX, chunkZ, extraRadius);
    }

    @Override
    public boolean isInsideRange(int chunkX, int chunkZ) {
        return isInside(chunkX, chunkZ);
    }

    @Override
    public boolean isInsideRange(int chunkX, int chunkZ, int extraRadius) {
        return isInside(chunkX, chunkZ, extraRadius);
    }

    @Override
    public boolean isInsideRange(int chunkX, int chunkZ, double extraRadius) {
        return isInside(chunkX, chunkZ, extraRadius);
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
    public boolean isDimensionEnabled(Dimension dimension) {
        return false;
    }

    @Override
    public void setDimensionEnabled(Dimension dimension, boolean enabled) {
        // Do nothing.
    }

    @Override
    public Collection<Dimension> getUnlockedWorlds() {
        return Collections.emptyList();
    }

    @Override
    @Deprecated
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
        return DEFAULT_SPAWN_PRIVILEGES_CACHE.contains(islandPrivilege) ?
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
    public String getStrippedName() {
        return "";
    }

    @Override
    public String getFormattedName() {
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
    public void sendMessage(String message) {
        // Do nothing.
    }

    @Override
    public void sendMessage(String message, UUID... ignoredMembers) {
        // Do nothing.
    }

    @Override
    public void sendMessage(IMessageComponent messageComponent) {
        // Do nothing.
    }

    @Override
    public void sendMessage(IMessageComponent messageComponent, Object... args) {
        // Do nothing.
    }

    @Override
    public void sendMessage(IMessageComponent messageComponent, List<UUID> ignoredMembers) {
        // Do nothing.
    }

    @Override
    public void sendMessage(IMessageComponent messageComponent, List<UUID> ignoredMembers, Object... args) {
        // Do nothing.
    }

    @Override
    public void sendTitle(String title, String subtitle, int fadeIn, int duration, int fadeOut) {
        // Do nothing.
    }

    @Override
    public void sendTitle(String title, String subtitle, int fadeIn, int duration, int fadeOut, UUID... ignoredMembers) {
        // Do nothing.
    }

    @Override
    public void executeCommand(String command, boolean onlyOnlineMembers) {
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
        try (ChunkPosition chunkPosition = ChunkPosition.of(this.spawnWorldInfo, chunkX, chunkZ)) {
            return this.dirtyChunksContainer.isMarkedDirty(chunkPosition);
        }
    }

    @Override
    public boolean isChunkDirty(String worldName, int chunkX, int chunkZ) {
        Preconditions.checkNotNull(worldName, "worldName parameter cannot be null.");
        Preconditions.checkArgument(this.spawnWorldInfo.getName().equals(worldName) && isInside(chunkX, chunkZ),
                "Chunk must be within the island boundaries.");
        try (ChunkPosition chunkPosition = ChunkPosition.of(this.spawnWorldInfo, chunkX, chunkZ)) {
            return this.dirtyChunksContainer.isMarkedDirty(chunkPosition);
        }
    }

    @Override
    public boolean isChunkDirty(WorldInfo worldInfo, int chunkX, int chunkZ) {
        Preconditions.checkNotNull(worldInfo, "worldInfo parameter cannot be null.");
        Preconditions.checkArgument(isInside(worldInfo, chunkX, chunkZ), "Chunk must be within the island boundaries.");
        try (ChunkPosition chunkPosition = ChunkPosition.of(this.spawnWorldInfo, chunkX, chunkZ)) {
            return this.dirtyChunksContainer.isMarkedDirty(chunkPosition);
        }
    }

    @Override
    public void markChunkDirty(World world, int chunkX, int chunkZ, boolean save) {
        Preconditions.checkNotNull(world, "world parameter cannot be null.");
        Preconditions.checkArgument(isInside(world, chunkX, chunkZ), "Chunk must be within the island boundaries.");
        try (ChunkPosition chunkPosition = ChunkPosition.of(this.spawnWorldInfo, chunkX, chunkZ)) {
            this.dirtyChunksContainer.markDirty(chunkPosition, save);
        }
    }

    @Override
    public void markChunkDirty(WorldInfo worldInfo, int chunkX, int chunkZ, boolean save) {
        Preconditions.checkNotNull(worldInfo, "worldInfo parameter cannot be null.");
        Preconditions.checkArgument(isInside(worldInfo, chunkX, chunkZ), "Chunk must be within the island boundaries.");
        try (ChunkPosition chunkPosition = ChunkPosition.of(this.spawnWorldInfo, chunkX, chunkZ)) {
            this.dirtyChunksContainer.markDirty(chunkPosition, save);
        }
    }

    @Override
    public void markChunkEmpty(World world, int chunkX, int chunkZ, boolean save) {
        Preconditions.checkNotNull(world, "world parameter cannot be null.");
        Preconditions.checkArgument(isInside(world, chunkX, chunkZ), "Chunk must be within the island boundaries.");
        try (ChunkPosition chunkPosition = ChunkPosition.of(this.spawnWorldInfo, chunkX, chunkZ)) {
            this.dirtyChunksContainer.markEmpty(chunkPosition, save);
        }
    }

    @Override
    public void markChunkEmpty(WorldInfo worldInfo, int chunkX, int chunkZ, boolean save) {
        Preconditions.checkNotNull(worldInfo, "worldInfo parameter cannot be null.");
        Preconditions.checkArgument(isInside(worldInfo, chunkX, chunkZ), "Chunk must be within the island boundaries.");
        try (ChunkPosition chunkPosition = ChunkPosition.of(this.spawnWorldInfo, chunkX, chunkZ)) {
            this.dirtyChunksContainer.markEmpty(chunkPosition, save);
        }
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
    public void removeEntityLimit(Key key) {
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
    public Map<PotionEffectType, Integer> getCustomPotionEffects() {
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
    public void warpPlayer(SuperiorPlayer superiorPlayer, String warpName) {
        // Do nothing.
    }

    @Override
    public void warpPlayer(SuperiorPlayer superiorPlayer, String warpName, boolean force) {
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
        return DEFAULT_SPAWN_FLAGS_CACHE.contains(islandFlag);
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
    public void resetSettings() {
        // Do nothing.
    }

    @Override
    public void setGeneratorPercentage(Key key, int percentage, Dimension dimension) {
        // Do nothing.
    }

    @Override
    public boolean setGeneratorPercentage(Key key, int percentage, Dimension dimension,
                                          @Nullable SuperiorPlayer caller, boolean callEvent) {
        return true;
    }

    @Override
    public int getGeneratorPercentage(Key key, Dimension dimension) {
        return 0;
    }

    @Override
    public Map<String, Integer> getGeneratorPercentages(Dimension dimension) {
        return Collections.emptyMap();
    }

    @Override
    public void setGeneratorAmount(Key key, @Size int amount, Dimension dimension) {
        // Do nothing.
    }

    @Override
    public void removeGeneratorAmount(Key key, Dimension dimension) {
        // Do nothing.
    }

    @Override
    public int getGeneratorAmount(Key key, Dimension dimension) {
        return 0;
    }

    @Override
    public int getGeneratorTotalAmount(Dimension dimension) {
        return 0;
    }

    @Override
    public Map<String, Integer> getGeneratorAmounts(Dimension dimension) {
        return Collections.emptyMap();
    }

    @Override
    public Map<Key, Integer> getCustomGeneratorAmounts(Dimension dimension) {
        return Collections.emptyMap();
    }

    @Override
    public void clearGeneratorAmounts(Dimension dimension) {
        // Do nothing.
    }

    @Nullable
    @Override
    public Key generateBlock(Location location, boolean optimizeDefaultBlock) {
        return null;
    }

    @Override
    public Key generateBlock(Location location, Dimension dimension, boolean optimizeDefaultBlock) {
        return null;
    }

    @Override
    public boolean wasSchematicGenerated(Dimension dimension) {
        return false;
    }

    @Override
    public void setSchematicGenerate(Dimension dimension) {
        // Do nothing.
    }

    @Override
    public void setSchematicGenerate(Dimension dimension, boolean generated) {
        // Do nothing.
    }

    @Override
    public Collection<Dimension> getGeneratedSchematics() {
        return Collections.emptySet();
    }

    @Override
    @Deprecated
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
