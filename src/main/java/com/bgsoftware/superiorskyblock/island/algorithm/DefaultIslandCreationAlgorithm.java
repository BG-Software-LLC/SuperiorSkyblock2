package com.bgsoftware.superiorskyblock.island.algorithm;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.world.algorithm.IslandCreationAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.island.builder.IslandBuilderImpl;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DefaultIslandCreationAlgorithm implements IslandCreationAlgorithm {

    private static final DefaultIslandCreationAlgorithm INSTANCE = new DefaultIslandCreationAlgorithm();

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private DefaultIslandCreationAlgorithm() {

    }

    public static DefaultIslandCreationAlgorithm getInstance() {
        return INSTANCE;
    }

    @Override
    public CompletableFuture<IslandCreationResult> createIsland(UUID islandUUID, SuperiorPlayer owner,
                                                                BlockPosition lastIsland, String islandName,
                                                                Schematic schematic) {
        Preconditions.checkNotNull(islandUUID, "islandUUID parameter cannot be null.");
        Preconditions.checkNotNull(owner, "owner parameter cannot be null.");
        Preconditions.checkNotNull(lastIsland, "lastIsland parameter cannot be null.");
        Preconditions.checkNotNull(islandName, "islandName parameter cannot be null.");
        Preconditions.checkNotNull(schematic, "schematic parameter cannot be null.");
        return createIsland(Island.newBuilder()
                        .setOwner(owner)
                        .setUniqueId(islandUUID)
                        .setName(islandName)
                        .setSchematicName(schematic.getName())
                , lastIsland);
    }

    @Override
    public CompletableFuture<IslandCreationResult> createIsland(Island.Builder builderParam, BlockPosition lastIsland) {
        Preconditions.checkNotNull(builderParam, "builder parameter cannot be null.");
        Preconditions.checkArgument(builderParam instanceof IslandBuilderImpl, "Cannot create an island from custom builder.");
        Preconditions.checkNotNull(lastIsland, "lastIsland parameter cannot be null.");

        IslandBuilderImpl builder = (IslandBuilderImpl) builderParam;

        Schematic schematic = builder.islandType == null ? null : plugin.getSchematics().getSchematic(builder.islandType);

        Preconditions.checkArgument(builder.owner != null, "Cannot create an island from builder with no valid owner.");
        Preconditions.checkArgument(schematic != null, "Cannot create an island from builder with invalid schematic name.");

        Log.debug(Debug.CREATE_ISLAND, builder.owner.getName(), schematic.getName(), lastIsland);

        CompletableFuture<IslandCreationResult> completableFuture = new CompletableFuture<>();

        Location islandLocation = plugin.getProviders().getWorldsProvider().getNextLocation(
                lastIsland.parse().clone(),
                plugin.getSettings().getIslandHeight(),
                plugin.getSettings().getMaxIslandSize(),
                builder.owner.getUniqueId(),
                builder.uuid
        );

        Log.debugResult(Debug.CREATE_ISLAND, "Next Island Position", islandLocation);

        Island island = builder.setCenter(islandLocation.add(0.5, 0, 0.5)).build();

        island.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.IDLE);

        EventResult<Boolean> event = plugin.getEventsBus().callIslandCreateEvent(builder.owner, island, builder.islandType);

        if (!event.isCancelled()) {
            schematic.pasteSchematic(island, islandLocation.getBlock().getRelative(BlockFace.DOWN).getLocation(), () -> {
                plugin.getProviders().getWorldsProvider().finishIslandCreation(islandLocation,
                        builder.owner.getUniqueId(), builder.uuid);
                completableFuture.complete(new IslandCreationResult(island, islandLocation, event.getResult()));
                island.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.SAVE_DATA);
            }, error -> {
                island.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.SAVE_DATA);
                plugin.getProviders().getWorldsProvider().finishIslandCreation(islandLocation,
                        builder.owner.getUniqueId(), builder.uuid);
                completableFuture.completeExceptionally(error);
            });
        }

        return completableFuture;
    }


}
