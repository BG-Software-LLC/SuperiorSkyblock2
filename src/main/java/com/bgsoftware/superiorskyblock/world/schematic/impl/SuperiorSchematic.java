package com.bgsoftware.superiorskyblock.world.schematic.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.SBlockOffset;
import com.bgsoftware.superiorskyblock.core.SchematicBlockData;
import com.bgsoftware.superiorskyblock.core.SchematicEntity;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.bgsoftware.superiorskyblock.world.BlockChangeTask;
import com.bgsoftware.superiorskyblock.world.schematic.BaseSchematic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
                EntityType entityType = EntityType.valueOf(compound.getString("entityType"));
                CompoundTag entityTag = compound.getCompound("NBT");
                Location offset = Serializers.LOCATION_SERIALIZER.deserialize(compound.getString("offset"));
                if (offset != null)
                    entities.add(new SchematicEntity(entityType, entityTag,
                            SBlockOffset.fromOffsets(offset.getX(), offset.getY(), offset.getZ())));
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
        if (!Bukkit.isPrimaryThread()) {
            BukkitExecutor.sync(() -> pasteSchematic(island, location, callback, onFailure));
            return;
        }

        try {
            PluginDebugger.debug("Action: Paste Schematic, Island: " + island.getOwner().getName() + ", Location: " +
                    Formatters.LOCATION_FORMATTER.format(location) + ", Schematic: " + name);

            Location min = this.offset.applyToLocation(location);

            BlockChangeTask blockChangeTask = new BlockChangeTask(island);

            for (SchematicBlockData schematicBlock : this.blocks)
                schematicBlock.applyBlock(blockChangeTask, min.clone());

            blockChangeTask.submitUpdate(() -> {
                for (SchematicEntity entity : this.entities) {
                    entity.spawnEntity(min);
                }

                island.handleBlocksPlace(cachedCounts);

                plugin.getEventsBus().callIslandSchematicPasteEvent(island, name, location);

                loadedChunks = blockChangeTask.getLoadedChunks();
                callback.run();
                loadedChunks = null;
            }, onFailure);
        } catch (Throwable ex) {
            if (onFailure != null)
                onFailure.accept(ex);
        }
    }

    @Override
    public Location adjustRotation(Location location) {
        location.setYaw(yaw);
        location.setPitch(pitch);
        return location;
    }

    @Override
    public Set<ChunkPosition> getLoadedChunks() {
        return loadedChunks;
    }

    private void readBlock(SchematicBlockData block) {
        Key key = plugin.getNMSAlgorithms().getBlockKey(block.getCombinedId());
        cachedCounts.put(key, cachedCounts.getRaw(key, 0) + 1);
    }

}
