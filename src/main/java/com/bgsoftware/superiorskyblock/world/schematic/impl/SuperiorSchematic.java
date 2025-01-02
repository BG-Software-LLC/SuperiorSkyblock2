package com.bgsoftware.superiorskyblock.world.schematic.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.core.BigBitSet;
import com.bgsoftware.superiorskyblock.core.ByteBigArray;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.SBlockOffset;
import com.bgsoftware.superiorskyblock.core.VarintArray;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.mutable.MutableBoolean;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SuperiorSchematic extends BaseSchematic implements Schematic {

    private static final ByteBigArray EMPTY_BLOCK_IDS = new ByteBigArray((short) 0);
    private static final BigBitSet EMPTY_BIT_SET = new BigBitSet(0);

    private final Data data;

    private List<ChunkPosition> affectedChunks = null;
    private Runnable onTeleportCallback = null;

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

        ByteBigArray blockIds;
        BigBitSet bitSet;
        Map<BlockOffset, SchematicBlock.Extra> extra;
        int minX;
        int minY;
        int minZ;

        ListTag blocksList = compoundTag.getList("blocks");
        if (blocksList == null) {
            blockIds = EMPTY_BLOCK_IDS;
            bitSet = EMPTY_BIT_SET;
            extra = Collections.emptyMap();
            minX = 0;
            minY = 0;
            minZ = 0;
        } else {
            minX = Integer.MAX_VALUE;
            minY = Integer.MAX_VALUE;
            minZ = Integer.MAX_VALUE;

            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;

            TreeSet<SchematicBlockData> schematicBlocks = new TreeSet<>(Comparator.naturalOrder());

            for (Tag<?> tag : blocksList) {
                SchematicBlockData schematicBlock = SuperiorSchematicDeserializer.deserializeSchematicBlock((CompoundTag) tag, dataVersion);
                if (schematicBlock != null && schematicBlock.getCombinedId() > 0) {
                    schematicBlocks.add(schematicBlock);
                    readBlock(schematicBlock);

                    // Compute the min and max block offset as we want to shrink as much as possible the amount
                    // of blocks in the schematic in memory. This will give us a layout of blocks that do not
                    // include any AIR blocks at all.
                    BlockOffset blockOffset = schematicBlock.getBlockOffset();
                    minX = Math.min(blockOffset.getOffsetX(), minX);
                    minY = Math.min(blockOffset.getOffsetY(), minY);
                    minZ = Math.min(blockOffset.getOffsetZ(), minZ);
                    maxX = Math.max(blockOffset.getOffsetX(), maxX);
                    maxY = Math.max(blockOffset.getOffsetY(), maxY);
                    maxZ = Math.max(blockOffset.getOffsetZ(), maxZ);
                }
            }

            // The xSize,ySize,zSize in the schematic consider air blocks, however we do not want them.
            // Therefore, we adjust the sizes accordingly to the actual blocks in the schematic.
            xSize = maxX - minX + 1;
            ySize = maxY - minY + 1;
            zSize = maxZ - minZ + 1;
            if ((long) xSize * (long) ySize * (long) zSize > Integer.MAX_VALUE) {
                throw new IllegalStateException("Cannot create such large schematic of size " + xSize + "x" + ySize + "x" + zSize);
            }

            VarintArray blockIdsVarintArray = new VarintArray();
            bitSet = new BigBitSet(xSize * ySize * zSize);
            extra = new HashMap<>();

            for (SchematicBlockData schematicBlock : schematicBlocks) {
                BlockOffset blockOffset = schematicBlock.getBlockOffset();

                int x = blockOffset.getOffsetX() - minX;
                int y = blockOffset.getOffsetY() - minY;
                int z = blockOffset.getOffsetZ() - minZ;

                // Calculate index in the BitSet for the given x,y,z.
                // The BitSet is sorted similar to how SchematicBlockData#compareTo is implemented.
                int index = y * (xSize * zSize) + x * zSize + z;
                bitSet.set(index);
                blockIdsVarintArray.add(schematicBlock.getCombinedId());

                if (schematicBlock.getExtra() != null)
                    extra.put(blockOffset, schematicBlock.getExtra());
            }

            blockIds = blockIdsVarintArray.toArray();
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

        this.data = new Data(schematicOffset, yaw, pitch, bitSet, blockIds,
                xSize, ySize, zSize, minX, minY, minZ, extra, entities);
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

        long profiler = Profiler.start(ProfileType.SCHEMATIC_PLACE, getName());

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

        long placeProfiler = Profiler.start(ProfileType.SCHEMATIC_BLOCKS_PLACE, getName());

        VarintArray.Itr blockIdsIterator = new VarintArray(this.data.blockIds).iterator();

        for (int i = this.data.bitSet.nextSetBit(0); i >= 0; i = this.data.bitSet.nextSetBit(i + 1)) {
            int x = ((i / this.data.zSize) % this.data.xSize) + this.data.minX;
            int y = (i / (this.data.xSize * this.data.zSize)) + this.data.minY;
            int z = (i % this.data.zSize) + this.data.minZ;

            BlockOffset blockOffset = SBlockOffset.fromOffsets(x, y, z);

            Location blockLocation = blockOffset.applyToLocation(min.clone());
            SchematicBlock schematicBlock = new SchematicBlock(blockLocation,
                    (int) blockIdsIterator.next(), data.extra.get(blockOffset));

            schematicBlock.doPrePlace(island);

            worldEditSession.setBlock(blockLocation, schematicBlock.getCombinedId(),
                    schematicBlock.getStatesTag(), schematicBlock.getTileEntityData());

            if (schematicBlock.shouldPostPlace())
                finishTasks.add(() -> schematicBlock.doPostPlace(island));
        }

        if (blockIdsIterator.hasNext())
            throw new IllegalStateException("Not all blocks were read from varint iterator");

        Profiler.end(placeProfiler);

        List<ChunkPosition> affectedChunks = worldEditSession.getAffectedChunks();
        List<CompletableFuture<Chunk>> chunkFutures = new ArrayList<>(affectedChunks.size());

        AtomicBoolean failed = new AtomicBoolean(false);
        MutableBoolean printedWarning = new MutableBoolean(false);

        affectedChunks.forEach(chunkPosition -> {
            if (!island.isInside(chunkPosition.getWorld(), chunkPosition.getX(), chunkPosition.getZ())) {
                if (!printedWarning.get()) {
                    Log.warn("Part of the schematic ", name, " is placed outside of the island, skipping this part...");
                    printedWarning.set(true);
                }
                return;
            }

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

                    Log.debugResult(Debug.PASTE_SCHEMATIC, "Finished Schematic Placement", "");

                    island.handleBlocksPlace(cachedCounts);

                    plugin.getEventsBus().callIslandSchematicPasteEvent(island, name, location);

                    Profiler.end(profiler);

                    synchronized (this) {
                        try {
                            prepareCallback(affectedChunks, min);
                            callback.run();
                        } finally {
                            finishCallback();
                        }
                    }
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

    @Override
    public Runnable onTeleportCallback() {
        return this.onTeleportCallback;
    }

    public SuperiorSchematic copy(String newName) {
        return new SuperiorSchematic(newName, this.data, this.cachedCounts);
    }

    private void readBlock(SchematicBlockData block) {
        Key key = plugin.getNMSAlgorithms().getBlockKey(block.getCombinedId());
        cachedCounts.put(key, cachedCounts.getRaw(key, 0) + 1);
    }

    private void prepareCallback(List<ChunkPosition> affectedChunks, Location min) {
        this.affectedChunks = new LinkedList<>(affectedChunks);
        // We spawn the entities with a delay, waiting for players to teleport to the island first.
        this.onTeleportCallback = () -> {
            BukkitExecutor.sync(() -> {
                for (SchematicEntity entity : data.entities) {
                    entity.spawnEntity(min);
                }
            }, 20L);
        };
    }

    private void finishCallback() {
        this.affectedChunks = null;
        this.onTeleportCallback = null;
    }

    private static class Data {

        private final BlockOffset offset;
        private final float yaw;
        private final float pitch;

        private final BigBitSet bitSet;
        private final ByteBigArray blockIds;
        private final Map<BlockOffset, SchematicBlock.Extra> extra;
        private final List<SchematicEntity> entities;

        /* Required to deserialize the bitset */
        private final int minX;
        private final int minY;
        private final int minZ;
        private final int xSize;
        private final int ySize;
        private final int zSize;

        Data(BlockOffset offset, float yaw, float pitch,
             BigBitSet bitSet, ByteBigArray blockIds, int xSize, int ySize, int zSize, int minX, int minY, int minZ,
             Map<BlockOffset, SchematicBlock.Extra> extra, List<SchematicEntity> entities) {
            this.offset = offset;
            this.yaw = yaw;
            this.pitch = pitch;
            this.bitSet = bitSet;
            this.blockIds = blockIds;
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.xSize = xSize;
            this.ySize = ySize;
            this.zSize = zSize;
            this.extra = Collections.unmodifiableMap(extra);
            this.entities = Collections.unmodifiableList(entities);
        }

    }

}
