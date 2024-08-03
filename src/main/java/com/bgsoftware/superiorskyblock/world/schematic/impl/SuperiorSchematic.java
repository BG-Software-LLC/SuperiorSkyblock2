package com.bgsoftware.superiorskyblock.world.schematic.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.SBlockOffset;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.profiler.ProfileType;
import com.bgsoftware.superiorskyblock.core.profiler.Profiler;
import com.bgsoftware.superiorskyblock.core.schematic.SchematicBlock;
import com.bgsoftware.superiorskyblock.core.schematic.SchematicBlockData;
import com.bgsoftware.superiorskyblock.core.schematic.SchematicEntity;
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
import com.bgsoftware.superiorskyblock.world.schematic.BaseSchematic;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SuperiorSchematic extends BaseSchematic implements Schematic {

    private final Data data;

    private List<ChunkPosition> affectedChunks = null;

    public SuperiorSchematic(String name, CompoundTag compoundTag) {
        super(name);

        int xSize = compoundTag.getInt("xSize");
        int ySize = compoundTag.getInt("ySize");
        int zSize = compoundTag.getInt("zSize");

        int offsetX = compoundTag.getInt("offsetX", xSize / 2);
        int offsetY = compoundTag.getInt("offsetY", ySize / 2);
        int offsetZ = compoundTag.getInt("offsetZ", zSize / 2);

        BlockOffset schematicOffset = SBlockOffset.fromOffsets(offsetX, offsetY, offsetZ).negate();
        float yaw = compoundTag.getFloat("yaw");
        float pitch = compoundTag.getFloat("pitch");

        int dataVersion = compoundTag.getInt("minecraftDataVersion", -1);

        List<SchematicBlockData> blocks;
        ListTag blocksList = compoundTag.getList("blocks");
        if (blocksList == null) {
            blocks = Collections.emptyList();
        } else {
            LinkedList<SchematicBlockData> schematicBlocks = new LinkedList<>();

            for (Tag<?> tag : blocksList) {
                SchematicBlockData schematicBlock = SuperiorSchematicDeserializer.deserializeSchematicBlock((CompoundTag) tag, dataVersion);
                if (schematicBlock != null && schematicBlock.getCombinedId() > 0) {
                    schematicBlocks.add(schematicBlock);
                    readBlock(schematicBlock);
                }
            }

            blocks = new SequentialListBuilder<SchematicBlockData>()
                    .sorted(SchematicBlockData::compareTo)
                    .build(schematicBlocks);
        }

        List<SchematicEntity> entities;
        ListTag entitiesList = compoundTag.getList("entities");
        if (entitiesList == null) {
            entities = Collections.emptyList();
        } else {
            entities = new LinkedList<>();

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
        }

        this.data = new Data(schematicOffset, yaw, pitch, blocks, entities);
    }

    private SuperiorSchematic(String name, Data data, KeyMap<Integer> cachedCounts) {
        super(name, cachedCounts);
        this.data = data;
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

        long profiler = Profiler.start(ProfileType.SCHEMATIC_PLACE);

        Log.debug(Debug.PASTE_SCHEMATIC, this.name, island.getOwner().getName(), location);

        try {
            pasteSchematicAsyncInternal(island, location, profiler, callback, onFailure);
        } catch (Throwable error) {
            Log.debugResult(Debug.PASTE_SCHEMATIC, "Failed Schematic Placement", error);
            Profiler.end(profiler);
            if (onFailure != null)
                onFailure.accept(error);
        }
    }

    private void pasteSchematicAsyncInternal(Island island, Location location, long profiler, Runnable callback, Consumer<Throwable> onFailure) {
        WorldEditSession worldEditSession = plugin.getNMSWorld().createEditSession(location.getWorld());
        Location min = this.data.offset.applyToLocation(location);

        List<Runnable> finishTasks = new LinkedList<>();

        this.data.blocks.forEach(schematicBlockData -> {
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

                    island.markChunkDirty(chunk.getWorld(), chunk.getX(), chunk.getZ(), true);

                    Log.debugResult(Debug.PASTE_SCHEMATIC, "Loaded Chunk", chunkPosition);
                } catch (Throwable error) {
                    Log.debugResult(Debug.PASTE_SCHEMATIC, "Failed Loading Chunk", error);
                    failed.set(true);
                    Profiler.end(profiler);
                    if (onFailure != null)
                        onFailure.accept(error);
                }
            }));
        });

        CompletableFuture.allOf(chunkFutures.toArray(new CompletableFuture[0])).whenComplete((v, error) -> {
            if (failed.get())
                return;

            Log.debugResult(Debug.PASTE_SCHEMATIC, "Finished Chunks Loading", "");

            BukkitExecutor.ensureMain(() -> {
                try {
                    Log.debugResult(Debug.PASTE_SCHEMATIC, "Placing Schematic", "");
                    worldEditSession.finish(island);

                    if (island.getOwner().isOnline())
                        finishTasks.forEach(Runnable::run);

                    Log.debugResult(Debug.PASTE_SCHEMATIC, "Spawning Entities", "");

                    for (SchematicEntity entity : this.data.entities) {
                        entity.spawnEntity(min);
                    }

                    Log.debugResult(Debug.PASTE_SCHEMATIC, "Finished Schematic Placement", "");

                    island.handleBlocksPlace(cachedCounts);

                    plugin.getEventsBus().callIslandSchematicPasteEvent(island, name, location);

                    Profiler.end(profiler);

                    this.affectedChunks = new LinkedList<>(affectedChunks);
                    callback.run();
                    this.affectedChunks = null;
                } catch (Throwable error2) {
                    Log.debugResult(Debug.PASTE_SCHEMATIC, "Failed Finishing Placement", error2);
                    Profiler.end(profiler);
                    if (onFailure != null)
                        onFailure.accept(error2);
                }
            });
        });
    }

    @Override
    public Location adjustRotation(Location location) {
        location.setYaw(this.data.yaw);
        location.setPitch(this.data.pitch);
        return location;
    }

    @Override
    public List<ChunkPosition> getAffectedChunks() {
        return affectedChunks == null ? Collections.emptyList() : Collections.unmodifiableList(affectedChunks);
    }

    public SuperiorSchematic copy(String newName) {
        return new SuperiorSchematic(newName, this.data, this.cachedCounts);
    }

    private void readBlock(SchematicBlockData block) {
        Key key = plugin.getNMSAlgorithms().getBlockKey(block.getCombinedId());
        cachedCounts.put(key, cachedCounts.getRaw(key, 0) + 1);
    }

    private static class Data {

        private final BlockOffset offset;
        private final float yaw;
        private final float pitch;
        private final List<SchematicBlockData> blocks;
        private final List<SchematicEntity> entities;

        Data(BlockOffset offset, float yaw, float pitch, List<SchematicBlockData> blocks, List<SchematicEntity> entities) {
            this.offset = offset;
            this.yaw = yaw;
            this.pitch = pitch;
            this.blocks = Collections.unmodifiableList(blocks);
            this.entities = Collections.unmodifiableList(entities);
        }

    }

}
