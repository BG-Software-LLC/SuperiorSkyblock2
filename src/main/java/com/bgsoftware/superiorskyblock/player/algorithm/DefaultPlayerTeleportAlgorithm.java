package com.bgsoftware.superiorskyblock.player.algorithm;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandSetHomeEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.player.algorithm.PlayerTeleportAlgorithm;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.world.EntityTeleports;
import com.bgsoftware.superiorskyblock.world.WorldBlocks;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksProvider;
import com.google.common.base.Preconditions;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DefaultPlayerTeleportAlgorithm implements PlayerTeleportAlgorithm {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final DefaultPlayerTeleportAlgorithm INSTANCE = new DefaultPlayerTeleportAlgorithm();

    private DefaultPlayerTeleportAlgorithm() {

    }

    public static DefaultPlayerTeleportAlgorithm getInstance() {
        return INSTANCE;
    }

    @Override
    public CompletableFuture<Boolean> teleport(Player player, Location location) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        EntityTeleports.teleport(player, location, completableFuture::complete);
        return completableFuture;
    }

    @Override
    public CompletableFuture<Boolean> teleport(Player player, Island island) {
        return this.teleport(player, island, plugin.getSettings().getWorlds().getDefaultWorld());
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

    @Override
    public CompletableFuture<Boolean> teleport(Player player, Island island, World.Environment environment) {
        Location homeLocation = island.getIslandHome(environment);

        Preconditions.checkNotNull(homeLocation, "Cannot find a suitable home location for island " +
                island.getUniqueId());

        Block islandTeleportBlock = homeLocation.getBlock();

        if (island.isSpawn()) {
            PluginDebugger.debug("Action: Teleport Player, Player: " + player.getName() + ", Location: " +
                    Formatters.LOCATION_FORMATTER.format(homeLocation));
            return teleport(player, homeLocation.add(0, 0.5, 0));
        }

        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        teleportIfSafe(player, island, islandTeleportBlock, homeLocation, 0, 0, (teleportResult, teleportLocation) -> {
            if (teleportResult) {
                completableFuture.complete(true);
                return;
            }

            Block islandCenterBlock = island.getCenter(environment).getBlock();
            float rotationYaw = homeLocation.getYaw();
            float rotationPitch = homeLocation.getPitch();

            teleportIfSafe(player, island, islandCenterBlock, null, rotationYaw, rotationPitch,
                    (centerTeleportResult, centerTeleportLocation) -> {
                        if (centerTeleportResult) {
                            changeIslandHome(island, centerTeleportLocation);
                            completableFuture.complete(true);
                            return;
                        }

                        {
                            Block teleportLocationHighestBlock = islandTeleportBlock.getWorld()
                                    .getHighestBlockAt(islandTeleportBlock.getLocation());
                            if (WorldBlocks.isSafeBlock(teleportLocationHighestBlock)) {
                                adjustAndTeleportPlayerToLocation(player, island, teleportLocationHighestBlock.getLocation(),
                                        rotationYaw, rotationPitch, completableFuture::complete);
                                return;
                            }
                        }

                        {
                            Block centerHighestBlock = islandCenterBlock.getWorld().getHighestBlockAt(islandCenterBlock.getLocation());
                            if (WorldBlocks.isSafeBlock(centerHighestBlock)) {
                                adjustAndTeleportPlayerToLocation(player, island, centerHighestBlock.getLocation(), rotationYaw,
                                        rotationPitch, completableFuture::complete);
                                return;
                            }
                        }

                        /*
                         *   Finding a new block to teleport the player to.
                         */

                        World world = island.getCenter(environment).getWorld();

                        List<CompletableFuture<ChunkSnapshot>> chunksToLoad = new SequentialListBuilder<CompletableFuture<ChunkSnapshot>>()
                                .build(IslandUtils.getAllChunksAsync(island, world, true, true, ChunkLoadReason.FIND_SAFE_SPOT, (Consumer<Chunk>) null),
                                        future -> future.thenApply(Chunk::getChunkSnapshot));

                        World islandsWorld = plugin.getGrid().getIslandsWorld(island, environment);

                        BukkitExecutor.createTask().runAsync(v -> {
                            List<Location> safeLocations = new LinkedList<>();

                            for (CompletableFuture<ChunkSnapshot> chunkToLoad : chunksToLoad) {
                                ChunkSnapshot chunkSnapshot;

                                try {
                                    chunkSnapshot = chunkToLoad.get();
                                } catch (Exception ex) {
                                    SuperiorSkyblockPlugin.log("&cCouldn't load chunk!");
                                    PluginDebugger.debug(ex);
                                    continue;
                                }

                                if (WorldBlocks.isChunkEmpty(null, chunkSnapshot))
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
                                adjustAndTeleportPlayerToLocation(player, island, location, rotationYaw, rotationPitch,
                                        completableFuture::complete);
                            } else {
                                completableFuture.complete(false);
                            }
                        });

                    });
        });

        return completableFuture;
    }

    private void adjustAndTeleportPlayerToLocation(Player player, Island island, Location location, float yaw,
                                                   float pitch, Consumer<Boolean> result) {
        Location homeLocation = location.add(0.5, 0, 0.5);
        homeLocation.setYaw(yaw);
        homeLocation.setPitch(pitch);


        PluginDebugger.debug("Action: Teleport Player, Player: " + player.getName() + ", Location: " +
                Formatters.LOCATION_FORMATTER.format(location));

        Location teleportLocation = changeIslandHome(island, homeLocation).add(0, 1.5, 0);
        teleport(player, teleportLocation);
        if (result != null)
            result.accept(true);
    }

    private void teleportIfSafe(Player player, Island island, Block block, Location customLocation, float yaw, float pitch,
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
                toTeleport = changeIslandHome(island, toTeleport);
            }

            PluginDebugger.debug("Action: Teleport Player, Player: " + player.getName() + ", Location: " +
                    Formatters.LOCATION_FORMATTER.format(toTeleport));
            teleport(player, toTeleport.add(0, 1.5, 0));

            if (teleportResult != null)
                teleportResult.accept(true, toTeleport);
        });
    }

}
