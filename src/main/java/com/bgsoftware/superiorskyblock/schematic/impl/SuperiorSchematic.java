package com.bgsoftware.superiorskyblock.schematic.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.formatting.Formatters;
import com.bgsoftware.superiorskyblock.schematic.BaseSchematic;
import com.bgsoftware.superiorskyblock.schematic.data.SchematicBlock;
import com.bgsoftware.superiorskyblock.schematic.data.SchematicEntity;
import com.bgsoftware.superiorskyblock.serialization.Serializers;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.FloatTag;
import com.bgsoftware.superiorskyblock.tag.IntTag;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import com.bgsoftware.superiorskyblock.tag.StringTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.world.blocks.BlockChangeTask;
import com.bgsoftware.superiorskyblock.world.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.wrappers.SBlockOffset;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

public final class SuperiorSchematic extends BaseSchematic implements Schematic {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final CompoundTag compoundTag;

    private final BlockOffset offset;
    private final float yaw;
    private final float pitch;
    private final List<SchematicBlock> blocks;
    private final List<SchematicEntity> entities;

    private Set<ChunkPosition> loadedChunks = null;

    public SuperiorSchematic(String name, CompoundTag compoundTag) {
        super(name);
        this.compoundTag = compoundTag;

        int xSize = SuperiorSchematicDeserializer.readNumberFromTag(compoundTag.getValue().get("xSize"));
        int ySize = SuperiorSchematicDeserializer.readNumberFromTag(compoundTag.getValue().get("ySize"));
        int zSize = SuperiorSchematicDeserializer.readNumberFromTag(compoundTag.getValue().get("zSize"));

        int offsetX = ((IntTag) compoundTag.getValue().getOrDefault("offsetX", new IntTag(xSize / 2))).getValue();
        int offsetY = ((IntTag) compoundTag.getValue().getOrDefault("offsetY", new IntTag(ySize / 2))).getValue();
        int offsetZ = ((IntTag) compoundTag.getValue().getOrDefault("offsetZ", new IntTag(zSize / 2))).getValue();

        this.offset = SBlockOffset.fromOffsets(offsetX, offsetY, offsetZ).negate();
        this.yaw = ((FloatTag) compoundTag.getValue().getOrDefault("yaw", new FloatTag(0))).getValue();
        this.pitch = ((FloatTag) compoundTag.getValue().getOrDefault("pitch", new FloatTag(0))).getValue();

        if (!compoundTag.getValue().containsKey("blocks")) {
            this.blocks = Collections.emptyList();
        } else {
            List<Tag<?>> blocksList = ((ListTag) compoundTag.getValue().get("blocks")).getValue();
            Set<SchematicBlock> schematicBlocks = new TreeSet<>(SchematicBlock::compareTo);

            for (Tag<?> tag : blocksList) {
                SchematicBlock schematicBlock = SuperiorSchematicDeserializer.deserializeSchematicBlock(tag);
                if (schematicBlock != null && schematicBlock.getCombinedId() > 0) {
                    schematicBlocks.add(schematicBlock);
                    readBlock(schematicBlock);
                }
            }

            this.blocks = Collections.unmodifiableList(new LinkedList<>(schematicBlocks));
        }


        if (!compoundTag.getValue().containsKey("entities")) {
            this.entities = Collections.emptyList();
        } else {
            List<Tag<?>> entitiesList = ((ListTag) compoundTag.getValue().get("entities")).getValue();
            List<SchematicEntity> entities = new LinkedList<>();

            for (Tag<?> tag : entitiesList) {
                Map<String, Tag<?>> compoundValue = ((CompoundTag) tag).getValue();
                EntityType entityType = EntityType.valueOf(((StringTag) compoundValue.get("entityType")).getValue());
                CompoundTag entityTag = (CompoundTag) compoundValue.get("NBT");
                BlockOffset blockOffset = Serializers.OFFSET_SERIALIZER.deserialize(((StringTag) compoundValue.get("offset")).getValue());
                entities.add(new SchematicEntity(entityType, entityTag, blockOffset));
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
            Executor.sync(() -> pasteSchematic(island, location, callback, onFailure));
            return;
        }

        try {
            PluginDebugger.debug("Action: Paste Schematic, Island: " + island.getOwner().getName() + ", Location: " +
                    Formatters.LOCATION_FORMATTER.format(location) + ", Schematic: " + name);

            Location min = this.offset.applyToLocation(location);

            BlockChangeTask blockChangeTask = new BlockChangeTask(island);

            for (SchematicBlock schematicBlock : this.blocks)
                schematicBlock.applyBlock(blockChangeTask, min.clone());

            blockChangeTask.submitUpdate(() -> {
                for (SchematicEntity entity : this.entities) {
                    entity.spawnEntity(min);
                }

                island.handleBlocksPlace(cachedCounts);

                EventsCaller.callIslandSchematicPasteEvent(island, name, location);

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

    public CompoundTag getTag() {
        return compoundTag;
    }

    private void readBlock(SchematicBlock block) {
        Key key = plugin.getNMSAlgorithms().getBlockKey(block.getCombinedId());
        cachedCounts.put(key, cachedCounts.getRaw(key, 0) + 1);
    }

}
