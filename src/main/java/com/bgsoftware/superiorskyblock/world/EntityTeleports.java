package com.bgsoftware.superiorskyblock.world;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandSetHomeEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChunkFlags;
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

            superiorPlayer.setTeleportTask(BukkitExecutor.sync(() -> teleportCallback.accept(true), warmupInMillis / 50));
        } else {
            teleportCallback.accept(false);
        }
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
                result.complete(adjustLocationToHome(island, safeSpot.getBlock(), rotationYaw, rotationPitch));
            } else {
                result.complete(null);
            }
        });
    }

    private static void findSafeSpotInChunk(Island island, Queue<ChunkPosition> islandChunks, World islandsWorld,
                                            Location homeLocation, Consumer<Location> onResult) {
        ChunkPosition chunkPosition = islandChunks.poll();
        if (chunkPosition == null) {
            onResult.accept(null);
            return;
        }

        ChunksProvider.loadChunk(chunkPosition, ChunkLoadReason.FIND_SAFE_SPOT, null).whenComplete((chunk, err) -> {
            ChunkSnapshot chunkSnapshot = chunk.getChunkSnapshot();

            if (WorldBlocks.isChunkEmpty(island, chunkSnapshot)) {
                findSafeSpotInChunk(island, islandChunks, islandsWorld, homeLocation, onResult);
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
                    findSafeSpotInChunk(island, islandChunks, islandsWorld, homeLocation, onResult);
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
