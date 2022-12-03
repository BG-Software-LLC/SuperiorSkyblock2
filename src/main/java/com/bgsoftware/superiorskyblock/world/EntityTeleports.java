package com.bgsoftware.superiorskyblock.world;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandSetHomeEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksProvider;
import com.google.common.base.Preconditions;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
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

        Log.debug(Debug.FIND_SAFE_TELEPORT, island.getOwner().getName(), environment);

        Block islandTeleportBlock = homeLocation.getBlock();

        if (island.isSpawn()) {
            Log.debugResult(Debug.FIND_SAFE_TELEPORT, "Result Location", homeLocation);
            return CompletableFuture.completedFuture(homeLocation.add(0, 0.5, 0));
        }

        CompletableFuture<Location> result = new CompletableFuture<>();

        findSafeSpot(island, islandTeleportBlock, homeLocation, 0, 0, (teleportResult, teleportLocation) -> {
            if (teleportResult) {
                result.complete(teleportLocation);
                return;
            }

            Block islandCenterBlock = island.getCenter(environment).getBlock();
            float rotationYaw = homeLocation.getYaw();
            float rotationPitch = homeLocation.getPitch();

            findSafeSpot(island, islandCenterBlock, null, rotationYaw, rotationPitch, (centerTeleportResult, centerTeleportLocation) -> {
                if (centerTeleportResult) {
                    result.complete(centerTeleportLocation);
                    return;
                }

                {
                    Block teleportLocationHighestBlock = islandTeleportBlock.getWorld()
                            .getHighestBlockAt(islandTeleportBlock.getLocation());
                    if (WorldBlocks.isSafeBlock(teleportLocationHighestBlock)) {
                        adjustLocationToHome(island, teleportLocationHighestBlock.getLocation(), rotationYaw, rotationPitch, result::complete);
                        return;
                    }
                }

                {
                    Block centerHighestBlock = islandCenterBlock.getWorld().getHighestBlockAt(islandCenterBlock.getLocation());
                    if (WorldBlocks.isSafeBlock(centerHighestBlock)) {
                        adjustLocationToHome(island, centerHighestBlock.getLocation(), rotationYaw, rotationPitch, result::complete);
                        return;
                    }
                }

                /*
                 *   Finding a new block to teleport the player to.
                 */

                World world = island.getCenter(environment).getWorld();

                List<CompletableFuture<ChunkSnapshot>> chunksToLoad = new SequentialListBuilder<CompletableFuture<ChunkSnapshot>>()
                        .build(IslandUtils.getAllChunksAsync(island, world, true, true, ChunkLoadReason.FIND_SAFE_SPOT, null),
                                future -> future.thenApply(Chunk::getChunkSnapshot));

                World islandsWorld = plugin.getGrid().getIslandsWorld(island, environment);

                BukkitExecutor.createTask().runAsync(v -> {
                    List<Location> safeLocations = new LinkedList<>();

                    for (CompletableFuture<ChunkSnapshot> chunkToLoad : chunksToLoad) {
                        ChunkSnapshot chunkSnapshot;

                        try {
                            chunkSnapshot = chunkToLoad.get();
                        } catch (Exception error) {
                            Log.error(error, "An unexpected error occurred while loading chunk:");
                            continue;
                        }

                        if (WorldBlocks.isChunkEmpty(island, chunkSnapshot))
                            continue;

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
                                if (WorldBlocks.isSafeBlock(chunkSnapshot, x, y, z)) {
                                    safeLocations.add(new Location(islandsWorld, worldX, y, worldZ));
                                } else if (y - 1 >= worldMinLimit && WorldBlocks.isSafeBlock(chunkSnapshot, x, y - 1, z)) {
                                    safeLocations.add(new Location(islandsWorld, worldX, y - 1, worldZ));
                                }
                            }
                        }
                    }

                    return safeLocations.stream().min(Comparator.comparingDouble(loc ->
                            loc.distanceSquared(homeLocation))).orElse(null);
                }).runSync(location -> {
                    if (location != null) {
                        adjustLocationToHome(island, location, rotationYaw, rotationPitch, result::complete);
                    } else {
                        result.complete(null);
                    }
                });

            });
        });

        return result;
    }

    private static void teleportEntity(Entity entity, Location location, @Nullable Consumer<Boolean> teleportResult) {
        entity.eject();
        plugin.getProviders().getAsyncProvider().teleport(entity, location, teleportResult);
    }

    private static void adjustLocationToHome(Island island, Location location, float yaw,
                                             float pitch, Consumer<Location> result) {
        Location homeLocation = location.add(0.5, 0, 0.5);
        homeLocation.setYaw(yaw);
        homeLocation.setPitch(pitch);

        Location locationResult = changeIslandHome(island, homeLocation).add(0, 1.5, 0);

        Log.debugResult(Debug.FIND_SAFE_TELEPORT, "Result Location", locationResult);

        result.accept(locationResult);
    }

    private static void findSafeSpot(Island island, Block block, Location customLocation, float yaw, float pitch,
                                     BiConsumer<Boolean, Location> teleportResult) {
        ChunksProvider.loadChunk(ChunkPosition.of(block), ChunkLoadReason.ENTITY_TELEPORT, chunk -> {
            if (!WorldBlocks.isSafeBlock(block)) {
                if (teleportResult != null)
                    teleportResult.accept(false, null);
                return;
            }

            Location toTeleport;

            if (customLocation != null) {
                toTeleport = customLocation;
            } else {
                toTeleport = block.getLocation().add(0.5, 0, 0.5);
                toTeleport.setYaw(yaw);
                toTeleport.setPitch(pitch);
            }

            toTeleport = changeIslandHome(island, toTeleport).add(0, 1.5, 0);

            Log.debugResult(Debug.FIND_SAFE_TELEPORT, "Result Location", toTeleport);

            if (teleportResult != null)
                teleportResult.accept(true, toTeleport);
        });
    }

    private static Location changeIslandHome(Island island, Location islandHome) {
        EventResult<Location> eventResult = plugin.getEventsBus().callIslandSetHomeEvent(island, islandHome,
                IslandSetHomeEvent.Reason.SAFE_HOME, null);
        if (!eventResult.isCancelled()) {
            island.setIslandHome(eventResult.getResult());
            return eventResult.getResult();
        }

        return islandHome;
    }

}
