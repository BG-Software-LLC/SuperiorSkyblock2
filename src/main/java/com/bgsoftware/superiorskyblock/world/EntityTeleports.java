package com.bgsoftware.superiorskyblock.world;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandSetHomeEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChunkFlags;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksProvider;
import com.google.common.base.Preconditions;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class EntityTeleports {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private EntityTeleports() {
    }

    public static void teleport(Entity entity, Location location) {
        teleport(entity, location, null);
    }

    public static void teleport(Entity entity, Location location, @Nullable Consumer<Boolean> teleportResult) {
        Island island = plugin.getGrid().getIslandAt(location);

        if (island != null) {
            plugin.getProviders().getWorldsProvider().prepareTeleport(island, location.clone(),
                    () -> teleportEntity(entity, location, teleportResult));
        } else {
            teleportEntity(entity, location, teleportResult);
        }
    }

    public static void teleportUntilSuccess(Entity entity, Location location, long cooldown, @Nullable Runnable onFinish) {
        teleport(entity, location, succeed -> {
            if (!succeed) {
                if (cooldown > 0) {
                    BukkitExecutor.sync(() -> teleportUntilSuccess(entity, location, cooldown, onFinish), cooldown);
                } else {
                    teleportUntilSuccess(entity, location, cooldown, onFinish);
                }
            } else if (onFinish != null) {
                onFinish.run();
            }
        });
    }

    public static CompletableFuture<Location> findIslandSafeLocation(Island island, World.Environment environment) {
        Location homeLocation = island.getIslandHome(environment);

        Preconditions.checkNotNull(homeLocation, "Cannot find a suitable home location for island " +
                island.getUniqueId());

        World islandsWorld = Objects.requireNonNull(plugin.getGrid().getIslandsWorld(island, environment), "world is null");
        float rotationYaw = homeLocation.getYaw();
        float rotationPitch = homeLocation.getPitch();

        Log.debug(Debug.FIND_SAFE_TELEPORT, island.getOwner().getName(), environment);

        // We first check that the home location is safe. If it is, we can return.
        {
            Block homeLocationBlock = homeLocation.getBlock();
            if (island.isSpawn() || WorldBlocks.isSafeBlock(homeLocationBlock)) {
                Log.debugResult(Debug.FIND_SAFE_TELEPORT, "Result Location", homeLocation);
                return CompletableFuture.completedFuture(homeLocation);
            }
        }

        // In case it is not safe anymore, we check in the same location if the highest block is safe.
        {
            Block teleportLocationHighestBlock = islandsWorld.getHighestBlockAt(homeLocation).getRelative(BlockFace.UP);
            if (WorldBlocks.isSafeBlock(teleportLocationHighestBlock)) {
                return CompletableFuture.completedFuture(adjustLocationToHome(island,
                        teleportLocationHighestBlock, rotationYaw, rotationPitch));
            }
        }

        CompletableFuture<Location> result = new CompletableFuture<>();

        // The teleport location is not safe. We check for a safe spot in the center of the island.

        Location islandCenterLocation = island.getCenter(environment);

        if (!islandCenterLocation.equals(homeLocation)) {
            ChunksProvider.loadChunk(ChunkPosition.of(islandCenterLocation), ChunkLoadReason.FIND_SAFE_SPOT, chunk -> {
                {
                    Block islandCenterBlock = islandCenterLocation.getBlock().getRelative(BlockFace.UP);
                    if (WorldBlocks.isSafeBlock(islandCenterBlock)) {
                        result.complete(adjustLocationToHome(island, islandCenterBlock, rotationYaw, rotationPitch));
                        return;
                    }
                }

                // The center is not safe, we check in the same location if the highest block is safe.
                {
                    Block islandCenterHighestBlock = islandsWorld.getHighestBlockAt(islandCenterLocation).getRelative(BlockFace.UP);
                    if (WorldBlocks.isSafeBlock(islandCenterHighestBlock)) {
                        result.complete(adjustLocationToHome(island, islandCenterHighestBlock, rotationYaw, rotationPitch));
                        return;
                    }
                }

                // The center is not safe; we look for a new spot on the island.
                findNewSafeSpotOnIsland(island, islandsWorld, homeLocation, rotationYaw, rotationPitch, result);
            });
        } else {
            findNewSafeSpotOnIsland(island, islandsWorld, homeLocation, rotationYaw, rotationPitch, result);
        }

        return result;
    }

    private static void findNewSafeSpotOnIsland(Island island, World islandsWorld, Location homeLocation,
                                                float rotationYaw, float rotationPitch, CompletableFuture<Location> result) {
        ChunkPosition homeChunk = ChunkPosition.of(homeLocation);

        List<ChunkPosition> islandChunks = new ArrayList<>(IslandUtils.getChunkCoords(island,
                WorldInfo.of(islandsWorld), IslandChunkFlags.ONLY_PROTECTED | IslandChunkFlags.NO_EMPTY_CHUNKS));
        islandChunks.sort(Comparator.comparingInt(o -> o.distanceSquared(homeChunk)));

        findSafeSpotInChunk(island, islandChunks, 0, islandsWorld, homeLocation, safeSpot -> {
            if (safeSpot != null) {
                result.complete(adjustLocationToHome(island, safeSpot.getBlock(), rotationYaw, rotationPitch));
            } else {
                result.complete(null);
            }
        });
    }

    private static void findSafeSpotInChunk(Island island, List<ChunkPosition> islandChunks, int index,
                                            World islandsWorld, Location homeLocation, Consumer<Location> onResult) {
        if (index >= islandChunks.size()) {
            onResult.accept(null);
            return;
        }

        ChunkPosition chunkPosition = islandChunks.get(index);

        ChunksProvider.loadChunk(chunkPosition, ChunkLoadReason.FIND_SAFE_SPOT, null).whenComplete((chunk, err) -> {
            ChunkSnapshot chunkSnapshot = chunk.getChunkSnapshot();

            if (WorldBlocks.isChunkEmpty(island, chunkSnapshot)) {
                findSafeSpotInChunk(island, islandChunks, index + 1, islandsWorld, homeLocation, onResult);
                return;
            }

            BukkitExecutor.createTask().runAsync(v -> {
                Location closestSafeSpot = null;
                double closestSafeSpotDistance = 0;

                int worldBuildLimit = islandsWorld.getMaxHeight() - 1;
                int worldMinLimit = plugin.getNMSWorld().getMinHeight(islandsWorld);

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        int y = chunkSnapshot.getHighestBlockYAt(x, z);

                        if (y < worldMinLimit || y + 2 > worldBuildLimit)
                            continue;

                        int worldX = chunkSnapshot.getX() * 16 + x;
                        int worldZ = chunkSnapshot.getZ() * 16 + z;

                        // In some versions, the ChunkSnapshot#getHighestBlockYAt seems to return
                        // one block above the actual highest block. Therefore, the check is on the
                        // returned block and the block below it.
                        Location safeSpot;
                        if (WorldBlocks.isSafeBlock(chunkSnapshot, x, y, z)) {
                            safeSpot = new Location(islandsWorld, worldX, y + 1, worldZ);
                        } else if (y - 1 >= worldMinLimit && WorldBlocks.isSafeBlock(chunkSnapshot, x, y - 1, z)) {
                            safeSpot = new Location(islandsWorld, worldX, y, worldZ);
                        } else {
                            continue;
                        }

                        double distanceFromHome = safeSpot.distanceSquared(homeLocation);
                        if (closestSafeSpot == null || distanceFromHome < closestSafeSpotDistance) {
                            closestSafeSpotDistance = distanceFromHome;
                            closestSafeSpot = safeSpot;
                        }
                    }
                }

                return closestSafeSpot;
            }).runSync(location -> {
                if (location != null) {
                    onResult.accept(location);
                } else {
                    findSafeSpotInChunk(island, islandChunks, index + 1, islandsWorld, homeLocation, onResult);
                }
            });

        });
    }

    private static void teleportEntity(Entity entity, Location location, @Nullable Consumer<Boolean> teleportResult) {
        entity.eject();
        plugin.getProviders().getAsyncProvider().teleport(entity, location, teleportResult);
    }

    private static Location adjustLocationToHome(Island island, Block block, float yaw, float pitch) {
        Location newHomeLocation;

        {
            Location location = block.getLocation().add(0.5, 0, 0.5);
            location.setYaw(yaw);
            location.setPitch(pitch);
            EventResult<Location> eventResult = plugin.getEventsBus().callIslandSetHomeEvent(island, location,
                    IslandSetHomeEvent.Reason.SAFE_HOME, null);

            if (eventResult.isCancelled()) {
                newHomeLocation = location;
            } else {
                newHomeLocation = eventResult.getResult();
                island.setIslandHome(newHomeLocation);
            }
        }

        Log.debugResult(Debug.FIND_SAFE_TELEPORT, "Result Location", newHomeLocation);

        return newHomeLocation;
    }

}
