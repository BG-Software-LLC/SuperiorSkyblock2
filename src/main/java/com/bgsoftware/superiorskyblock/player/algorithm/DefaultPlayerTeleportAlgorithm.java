package com.bgsoftware.superiorskyblock.player.algorithm;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.player.algorithm.PlayerTeleportAlgorithm;
import com.bgsoftware.superiorskyblock.island.SpawnIsland;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.utils.teleport.TeleportUtils;
import com.bgsoftware.superiorskyblock.world.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.world.chunks.ChunksProvider;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
        TeleportUtils.teleport(player, location, completableFuture::complete);
        return completableFuture;
    }

    @Override
    public CompletableFuture<Boolean> teleport(Player player, Island island) {
        Location homeLocation = island.getIslandHome(plugin.getSettings().getWorlds().getDefaultWorld());

        Preconditions.checkNotNull(homeLocation, "Cannot find a suitable home location for island " +
                island.getUniqueId());

        Block islandTeleportBlock = homeLocation.getBlock();

        if (island instanceof SpawnIsland) {
            PluginDebugger.debug("Action: Teleport Player, Player: " + player.getName() + ", Location: " + LocationUtils.getLocation(homeLocation));
            return teleport(player, homeLocation.add(0, 0.5, 0));
        }

        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        teleportIfSafe(player, island, islandTeleportBlock, homeLocation, 0, 0, (teleportResult, teleportLocation) -> {
            if (teleportResult) {
                completableFuture.complete(true);
                return;
            }

            Block islandCenterBlock = island.getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).getBlock();
            float rotationYaw = homeLocation.getYaw();
            float rotationPitch = homeLocation.getPitch();

            teleportIfSafe(player, island, islandCenterBlock, null, rotationYaw, rotationPitch,
                    (centerTeleportResult, centerTeleportLocation) -> {
                        if (centerTeleportResult) {
                            island.setIslandHome(centerTeleportLocation);
                            completableFuture.complete(true);
                            return;
                        }

                        {
                            Block teleportLocationHighestBlock = islandTeleportBlock.getWorld()
                                    .getHighestBlockAt(islandTeleportBlock.getLocation()).getRelative(BlockFace.UP);
                            if (LocationUtils.isSafeBlock(teleportLocationHighestBlock)) {
                                adjustAndTeleportPlayerToLocation(player, island, teleportLocationHighestBlock.getLocation(),
                                        rotationYaw, rotationPitch, completableFuture::complete);
                                return;
                            }
                        }

                        {
                            Block centerHighestBlock = islandCenterBlock.getWorld()
                                    .getHighestBlockAt(islandCenterBlock.getLocation()).getRelative(BlockFace.UP);
                            if (LocationUtils.isSafeBlock(centerHighestBlock)) {
                                adjustAndTeleportPlayerToLocation(player, island, centerHighestBlock.getLocation(), rotationYaw,
                                        rotationPitch, completableFuture::complete);
                                return;
                            }
                        }

                        /*
                         *   Finding a new block to teleport the player to.
                         */

                        List<CompletableFuture<ChunkSnapshot>> chunksToLoad = island.getAllChunksAsync(
                                        plugin.getSettings().getWorlds().getDefaultWorld(), true, true, null)
                                .stream().map(future -> future.thenApply(Chunk::getChunkSnapshot)).collect(Collectors.toList());

                        Executor.createTask().runAsync(v -> {
                            List<Location> safeLocations = new ArrayList<>();

                            for (CompletableFuture<ChunkSnapshot> chunkToLoad : chunksToLoad) {
                                ChunkSnapshot chunkSnapshot;

                                try {
                                    chunkSnapshot = chunkToLoad.get();
                                } catch (Exception ex) {
                                    SuperiorSkyblockPlugin.log("&cCouldn't load chunk!");
                                    PluginDebugger.debug(ex);
                                    continue;
                                }

                                if (LocationUtils.isChunkEmpty(null, chunkSnapshot))
                                    continue;

                                World world = Bukkit.getWorld(chunkSnapshot.getWorldName());
                                int worldBuildLimit = world.getMaxHeight() - 1;
                                int worldMinLimit = plugin.getNMSWorld().getMinHeight(world);

                                for (int x = 0; x < 16; x++) {
                                    for (int z = 0; z < 16; z++) {
                                        int y = Math.min(chunkSnapshot.getHighestBlockYAt(x, z), worldBuildLimit);
                                        Key blockKey = plugin.getNMSWorld().getBlockKey(chunkSnapshot, x, y, z);
                                        Key belowKey = plugin.getNMSWorld().getBlockKey(chunkSnapshot, x,
                                                y == worldMinLimit ? worldMinLimit : y - 1, z);

                                        Material blockType;
                                        Material belowType;

                                        try {
                                            blockType = Material.valueOf(blockKey.getGlobalKey());
                                            belowType = Material.valueOf(belowKey.getGlobalKey());
                                        } catch (IllegalArgumentException ex) {
                                            continue;
                                        }

                                        if (blockType.isSolid() || belowType.isSolid()) {
                                            safeLocations.add(new Location(Bukkit.getWorld(chunkSnapshot.getWorldName()),
                                                    chunkSnapshot.getX() * 16 + x, y, chunkSnapshot.getZ() * 16 + z));
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
        location = location.add(0.5, 0, 0.5);
        location.setYaw(yaw);
        location.setPitch(pitch);

        PluginDebugger.debug("Action: Teleport Player, Player: " + player.getName() + ", Location: " + LocationUtils.getLocation(location));

        island.setIslandHome(location);
        teleport(player, location.add(0, 0.5, 0));
        if (result != null)
            result.accept(true);
    }

    private void teleportIfSafe(Player player, Island island, Block block, Location customLocation, float yaw, float pitch,
                                BiConsumer<Boolean, Location> teleportResult) {
        ChunksProvider.loadChunk(ChunkPosition.of(block), chunk -> {
            if (!LocationUtils.isSafeBlock(block)) {
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
                island.setIslandHome(toTeleport);
            }

            PluginDebugger.debug("Action: Teleport Player, Player: " + player.getName() + ", Location: " + LocationUtils.getLocation(toTeleport));
            teleport(player, toTeleport.add(0, 0.5, 0));

            if (teleportResult != null)
                teleportResult.accept(true, toTeleport);
        });
    }

}
