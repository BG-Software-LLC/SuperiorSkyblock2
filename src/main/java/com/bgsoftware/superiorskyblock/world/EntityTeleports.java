package com.bgsoftware.superiorskyblock.world;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandSetHomeEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChunkFlags;
import com.bgsoftware.superiorskyblock.api.player.algorithm.PlayerTeleportAlgorithm;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.IslandWorlds;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
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

import java.time.Duration;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class EntityTeleports {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private EntityTeleports() {
    }

    public static void warmupTeleport(SuperiorPlayer superiorPlayer, long warmupInMillis, TeleportCallback teleportCallback) {
        if (warmupInMillis > 0 && !superiorPlayer.hasBypassModeEnabled() &&
                !superiorPlayer.hasPermission("superior.admin.bypass.warmup")) {
            Message.TELEPORT_WARMUP.send(superiorPlayer, Formatters.TIME_FORMATTER.format(
                    Duration.ofMillis(warmupInMillis), superiorPlayer.getUserLocale()));

            superiorPlayer.setTeleportTask(BukkitExecutor.sync(superiorPlayer.asPlayer(), () -> teleportCallback.accept(true), warmupInMillis / 50));
        } else {
            teleportCallback.accept(false);
        }
    }

    public static void teleport(Entity entity, Location location) {
        teleport(entity, location, null);
    }

    public static void teleport(Entity entity, Location location, @Nullable Consumer<PlayerTeleportAlgorithm.TeleportResult> teleportResult) {
        Island island = plugin.getGrid().getIslandAt(location);

        if (island != null) {
            plugin.getProviders().getWorldsProvider().prepareTeleport(island, location.clone(),
                    () -> teleportEntity(entity, location, teleportResult));
        } else {
            teleportEntity(entity, location, teleportResult);
        }
    }

    public static void teleportUntilSuccess(Entity entity, Location location, long cooldown, @Nullable Runnable onFinish) {
        teleport(entity, location, result -> {
            if (result != PlayerTeleportAlgorithm.TeleportResult.SUCCESS) {
                if (BukkitExecutor.isFolia()) {
                    if (result == PlayerTeleportAlgorithm.TeleportResult.OFFLINE_PLAYER) {
                        if (onFinish != null)
                            onFinish.run();
                        return;
                    }
                    try {
                        plugin.getTaskScheduler().entity(entity,
                                () -> teleportUntilSuccess(entity, location, cooldown, onFinish), onFinish, cooldown, 0L);
                    } catch (Throwable error) {
                        if (onFinish != null)
                            onFinish.run();
                        throw error;
                    }
                } else if (cooldown > 0) {
                    BukkitExecutor.sync(entity, () -> teleportUntilSuccess(entity, location, cooldown, onFinish), cooldown);
                } else {
                    teleportUntilSuccess(entity, location, cooldown, onFinish);
                }
            } else if (onFinish != null) {
                onFinish.run();
            }
        });
    }

    public static CompletableFuture<Location> findIslandSafeLocation(Island island, Dimension dimension) {
        CompletableFuture<Location> result = new CompletableFuture<>();
        IslandWorlds.accessIslandWorldAsync(island, dimension, true, islandWorldResult -> {
            islandWorldResult.ifRight(result::completeExceptionally).ifLeft(world ->
                    findIslandSafeLocation(island, dimension, result));
        });
        return result;
    }

    public static void findIslandSafeLocation(Island island, Dimension dimension, CompletableFuture<Location> result) {
        Location homeLocation = island.getIslandHome(dimension);

        Preconditions.checkNotNull(homeLocation, "Cannot find a suitable home location for island " +
                island.getUniqueId());

        if (BukkitExecutor.isFolia() && !BukkitExecutor.isOwned(homeLocation)) {
            BukkitExecutor.submit(homeLocation, () -> {
                findIslandSafeLocation(island, dimension, result);
                return null;
            }).exceptionally(error -> {
                result.completeExceptionally(error);
                return null;
            });
            return;
        }

        World islandsWorld = Objects.requireNonNull(plugin.getGrid().getIslandsWorld(island, dimension), "world is null");
        float rotationYaw = homeLocation.getYaw();
        float rotationPitch = homeLocation.getPitch();

        Log.debug(Debug.FIND_SAFE_TELEPORT, island.getOwner().getName(), dimension.getName());

        // We first check that the home location is safe. If it is, we can return.
        {
            Block homeLocationBlock = homeLocation.getBlock();
            if (island.isSpawn() || WorldBlocks.isSafeBlock(homeLocationBlock)) {
                Log.debugResult(Debug.FIND_SAFE_TELEPORT, "Result Location", homeLocation);
                result.complete(homeLocation);
                return;
            }
        }

        // In case it is not safe anymore, we check in the same location if the highest block is safe.
        {
            Block teleportLocationHighestBlock = islandsWorld.getHighestBlockAt(homeLocation).getRelative(BlockFace.UP);
            if (WorldBlocks.isSafeBlock(teleportLocationHighestBlock)) {
                result.complete(adjustLocationToHome(island, teleportLocationHighestBlock, rotationYaw, rotationPitch));
                return;
            }
        }

        // The teleport location is not safe. We check for a safe spot in the center of the island.

        Location islandCenterLocation = island.getCenter(dimension);

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
    }

    private static void findNewSafeSpotOnIsland(Island island, World islandsWorld, Location homeLocation,
                                                float rotationYaw, float rotationPitch, CompletableFuture<Location> result) {
        LinkedList<ChunkPosition> islandChunks = new LinkedList<>(IslandUtils.getChunkCoords(island,
                WorldInfo.of(islandsWorld), IslandChunkFlags.ONLY_PROTECTED | IslandChunkFlags.NO_EMPTY_CHUNKS));

        try (ChunkPosition homeChunk = ChunkPosition.of(homeLocation)) {
            islandChunks.sort(Comparator.comparingInt(o -> o.distanceSquared(homeChunk)));
        }

        findSafeSpotInChunk(island, islandChunks, islandsWorld, homeLocation, safeSpot -> {
            if (safeSpot != null) {
                BukkitExecutor.submit(safeSpot, () -> adjustLocationToHome(island, safeSpot.getBlock(),
                        rotationYaw, rotationPitch)).whenComplete((location, error) -> {
                    if (error == null)
                        result.complete(location);
                    else
                        result.completeExceptionally(error);
                });
            } else {
                result.complete(null);
            }
        }, result::completeExceptionally);
    }

    private static void findSafeSpotInChunk(Island island, Queue<ChunkPosition> islandChunks, World islandsWorld,
                                            Location homeLocation, Consumer<Location> onResult, Consumer<Throwable> onError) {
        ChunkPosition chunkPosition = islandChunks.poll();
        if (chunkPosition == null) {
            onResult.accept(null);
            return;
        }

        ChunksProvider.loadChunk(chunkPosition, ChunkLoadReason.FIND_SAFE_SPOT, null)
                .thenCompose(chunk -> BukkitExecutor.submit(new Location(islandsWorld, chunk.getX() << 4,
                        0, chunk.getZ() << 4), chunk::getChunkSnapshot)).whenComplete((chunkSnapshot, err) -> {
            if (err != null) {
                onError.accept(err);
                return;
            }

            if (WorldBlocks.isChunkEmpty(island, chunkSnapshot)) {
                findSafeSpotInChunk(island, islandChunks, islandsWorld, homeLocation, onResult, onError);
                return;
            }

            if (BukkitExecutor.isFolia()) {
                Location chunkLocation = new Location(islandsWorld, chunkSnapshot.getX() << 4,
                        0, chunkSnapshot.getZ() << 4);
                BukkitExecutor.submit(chunkLocation, () -> findClosestSafeSpot(chunkSnapshot, islandsWorld, homeLocation))
                        .whenComplete((location, error) -> {
                            if (error != null)
                                onError.accept(error);
                            else if (location != null)
                                onResult.accept(location);
                            else
                                findSafeSpotInChunk(island, islandChunks, islandsWorld, homeLocation, onResult, onError);
                        });
                return;
            }

            BukkitExecutor.createTask().runAsync(v -> {
                return findClosestSafeSpot(chunkSnapshot, islandsWorld, homeLocation);
            }).runSync(location -> {
                if (location != null) {
                    onResult.accept(location);
                } else {
                    findSafeSpotInChunk(island, islandChunks, islandsWorld, homeLocation, onResult, onError);
                }
            });

        }).exceptionally(error -> {
            onError.accept(error);
            return null;
        });
    }

    private static Location findClosestSafeSpot(ChunkSnapshot chunkSnapshot, World islandsWorld, Location homeLocation) {
        Location closestSafeSpot = null;
        double closestSafeSpotDistance = 0;

        int worldBuildLimit = islandsWorld.getMaxHeight();
        int worldMinLimit = plugin.getNMSWorld().getMinHeight(islandsWorld);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int y = chunkSnapshot.getHighestBlockYAt(x, z);

                // ChunkSnapshot#getHighestBlockYAt returns the highest block in 1.18+, and the
                // block above it in older versions. Therefore, we check both possible standing spots.
                int safeY;
                if (y > worldMinLimit && y + 2 < worldBuildLimit &&
                        WorldBlocks.isSafeStandingSpot(chunkSnapshot, x, y + 1, z)) {
                    safeY = y + 1;
                } else if (y - 1 > worldMinLimit && y + 1 < worldBuildLimit &&
                        WorldBlocks.isSafeStandingSpot(chunkSnapshot, x, y, z)) {
                    safeY = y;
                } else {
                    continue;
                }

                int worldX = chunkSnapshot.getX() * 16 + x;
                int worldZ = chunkSnapshot.getZ() * 16 + z;

                Location safeSpot = new Location(islandsWorld, worldX, safeY, worldZ);

                double distanceFromHome = safeSpot.distanceSquared(homeLocation);
                if (closestSafeSpot == null || distanceFromHome < closestSafeSpotDistance) {
                    closestSafeSpotDistance = distanceFromHome;
                    closestSafeSpot = safeSpot;
                }
            }
        }

        return closestSafeSpot;
    }

    private static void teleportEntity(Entity entity, Location location, @Nullable Consumer<PlayerTeleportAlgorithm.TeleportResult> teleportResult) {
        Location destination = BukkitExecutor.isFolia() ? location.clone() : location;
        Runnable teleport = () -> {
            entity.eject();
            plugin.getProviders().getAsyncProvider().teleport(entity, destination, teleportResult == null ? null : res ->
                    teleportResult.accept(res ? PlayerTeleportAlgorithm.TeleportResult.SUCCESS :
                            PlayerTeleportAlgorithm.TeleportResult.GENERAL_FAILURE));
        };
        if (!BukkitExecutor.isFolia() || BukkitExecutor.isOwned(entity)) {
            teleport.run();
        } else {
            plugin.getTaskScheduler().entity(entity, teleport, () -> {
                if (teleportResult != null)
                    teleportResult.accept(PlayerTeleportAlgorithm.TeleportResult.OFFLINE_PLAYER);
            }, 1L, 0L);
        }
    }

    private static Location adjustLocationToHome(Island island, Block block, float yaw, float pitch) {
        Location newHomeLocation;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location location = block.getLocation(wrapper.getHandle()).add(0.5, 0, 0.5);
            location.setYaw(yaw);
            location.setPitch(pitch);

            PluginEvent<PluginEventArgs.IslandSetHome> event = PluginEventsFactory.callIslandSetHomeEvent(
                    island, (SuperiorPlayer) null, location, IslandSetHomeEvent.Reason.SAFE_HOME);

            if (event.isCancelled()) {
                newHomeLocation = location;
            } else {
                newHomeLocation = event.getArgs().islandHome;
                island.setIslandHome(newHomeLocation);
            }
        }

        Log.debugResult(Debug.FIND_SAFE_TELEPORT, "Result Location", newHomeLocation);

        return newHomeLocation;
    }

    public interface TeleportCallback {

        void accept(boolean afterWarmup);

    }

}
