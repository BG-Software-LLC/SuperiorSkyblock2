package com.bgsoftware.superiorskyblock.world.schematic.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.SBlockOffset;
import com.bgsoftware.superiorskyblock.core.SchematicBlock;
import com.bgsoftware.superiorskyblock.core.SchematicBlockData;
import com.bgsoftware.superiorskyblock.core.SchematicEntity;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeCropGrowth;
import com.bgsoftware.superiorskyblock.nms.world.WorldEditSession;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksProvider;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksTracker;
import com.bgsoftware.superiorskyblock.world.schematic.BaseSchematic;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SuperiorSchematic extends BaseSchematic implements Schematic {

    private final BlockOffset offset;
    private final float yaw;
    private final float pitch;
    private final List<SchematicBlockData> blocks;
    private final List<SchematicEntity> entities;

    private Set<ChunkPosition> loadedChunks = null;

    public SuperiorSchematic(String name, CompoundTag compoundTag) {
        super(name);

        int xSize = compoundTag.getInt("xSize");
        int ySize = compoundTag.getInt("ySize");
        int zSize = compoundTag.getInt("zSize");

        int offsetX = compoundTag.getInt("offsetX", xSize / 2);
        int offsetY = compoundTag.getInt("offsetY", ySize / 2);
        int offsetZ = compoundTag.getInt("offsetZ", zSize / 2);

        this.offset = SBlockOffset.fromOffsets(offsetX, offsetY, offsetZ).negate();
        this.yaw = compoundTag.getFloat("yaw");
        this.pitch = compoundTag.getFloat("pitch");

        ListTag blocksList = compoundTag.getList("blocks");
        if (blocksList == null) {
            this.blocks = Collections.emptyList();
        } else {
            LinkedList<SchematicBlockData> schematicBlocks = new LinkedList<>();

            for (Tag<?> tag : blocksList) {
                SchematicBlockData schematicBlock = SuperiorSchematicDeserializer.deserializeSchematicBlock((CompoundTag) tag);
                if (schematicBlock != null && schematicBlock.getCombinedId() > 0) {
                    schematicBlocks.add(schematicBlock);
                    readBlock(schematicBlock);
                }
            }

            this.blocks = new SequentialListBuilder<SchematicBlockData>()
                    .sorted(SchematicBlockData::compareTo)
                    .build(schematicBlocks);
        }

        ListTag entitiesList = compoundTag.getList("entities");
        if (entitiesList == null) {
            this.entities = Collections.emptyList();
        } else {
            List<SchematicEntity> entities = new LinkedList<>();

            for (Tag<?> tag : entitiesList) {
                CompoundTag compound = (CompoundTag) tag;
                try {
                    EntityType entityType = EntityType.valueOf(compound.getString("entityType"));
                    CompoundTag entityTag = compound.getCompound("NBT");
                    Location offset = Serializers.LOCATION_SERIALIZER.deserialize(compound.getString("offset"));
                    if (offset != null)
                        entities.add(new SchematicEntity(entityType, entityTag,
                                SBlockOffset.fromOffsets(offset.getX(), offset.getY(), offset.getZ())));
                } catch (Exception error) {
                    error.printStackTrace();
                }
            }

            this.entities = Collections.unmodifiableList(entities);
        }
    }

    @Override
    public void pasteSchematic(Island island, Location location, Runnable callback) {
        pasteSchematic(island, location, callback, null);
    }

    @Override
    public void pasteSchematic(Island island, Location location, Runnable callback, Consumer<Throwable> onFailure) {
        if (Bukkit.isPrimaryThread()) {
            BukkitExecutor.async(() -> pasteSchematic(island, location, callback, onFailure));
            return;
        }

        Log.debug(Debug.PASTE_SCHEMATIC, "SuperiorSchematic", "pasteSchematic",
                this.name, island.getOwner().getName(), location);

        WorldEditSession worldEditSession = plugin.getNMSWorld().createEditSession(location.getWorld());
        Location min = this.offset.applyToLocation(location);

        List<Runnable> finishTasks = new LinkedList<>();

        this.blocks.forEach(schematicBlockData -> {
            Location blockLocation = schematicBlockData.getBlockOffset().applyToLocation(min.clone());
            SchematicBlock schematicBlock = new SchematicBlock(blockLocation, schematicBlockData);

            schematicBlock.doPrePlace(island);

            worldEditSession.setBlock(blockLocation, schematicBlock.getCombinedId(),
                    schematicBlock.getStatesTag(), schematicBlock.getTileEntityData());

            if (schematicBlock.shouldPostPlace())
                finishTasks.add(() -> schematicBlock.doPostPlace(island));
        });

        List<ChunkPosition> affectedChunks = worldEditSession.getAffectedChunks();
        List<CompletableFuture<Chunk>> chunkFutures = new ArrayList<>(affectedChunks.size());

        AtomicBoolean failed = new AtomicBoolean(false);

        affectedChunks.forEach(chunkPosition -> {
            chunkFutures.add(ChunksProvider.loadChunk(chunkPosition, ChunkLoadReason.SCHEMATIC_PLACE, chunk -> {
                if (failed.get())
                    return;

                try {
                    worldEditSession.applyBlocks(chunk);

                    boolean cropGrowthEnabled = BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeCropGrowth.class);
                    if (cropGrowthEnabled && island.isInsideRange(chunk)) {
                        plugin.getNMSChunks().startTickingChunk(island, chunk, false);
                    }

                    ChunksTracker.markDirty(island, chunk, false);

                    Log.debugResult(Debug.PASTE_SCHEMATIC, "SuperiorSchematic", "pasteSchematic",
                            "Loaded Chunk", chunkPosition);
                } catch (Throwable error) {
                    Log.debugResult(Debug.PASTE_SCHEMATIC, "SuperiorSchematic", "pasteSchematic",
                            "Failed Loading Chunk", error);
                    failed.set(true);
                    if (onFailure != null)
                        onFailure.accept(error);
                }
            }));
        });

        CompletableFuture.allOf(chunkFutures.toArray(new CompletableFuture[0])).whenComplete((v, error) -> {
            if (failed.get())
                return;

            Log.debugResult(Debug.PASTE_SCHEMATIC, "SuperiorSchematic", "pasteSchematic",
                    "Finished Chunks Loading", "");

            BukkitExecutor.ensureMain(() -> {
                try {
                    worldEditSession.finish(island);

                    if (island.getOwner().isOnline())
                        finishTasks.forEach(Runnable::run);

                    for (SchematicEntity entity : this.entities) {
                        entity.spawnEntity(min);
                    }

                    island.handleBlocksPlace(cachedCounts);

                    plugin.getEventsBus().callIslandSchematicPasteEvent(island, name, location);

                    this.loadedChunks = new HashSet<>(affectedChunks);
                    callback.run();
                    this.loadedChunks = null;
                } catch (Throwable error2) {
                    Log.debugResult(Debug.PASTE_SCHEMATIC, "SuperiorSchematic", "pasteSchematic",
                            "Failed Finishing Placement", error2);
                    if (onFailure != null)
                        onFailure.accept(error2);
                }
            });
        });
    }

    @Override
    public Location adjustRotation(Location location) {
        location.setYaw(yaw);
        location.setPitch(pitch);
        return location;
    }

    @Override
    public Set<ChunkPosition> getLoadedChunks() {
        return loadedChunks == null ? Collections.emptySet() : Collections.unmodifiableSet(loadedChunks);
    }

    private void readBlock(SchematicBlockData block) {
        Key key = plugin.getNMSAlgorithms().getBlockKey(block.getCombinedId());
        cachedCounts.put(key, cachedCounts.getRaw(key, 0) + 1);
    }

}
