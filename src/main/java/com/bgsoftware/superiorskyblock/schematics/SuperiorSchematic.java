package com.bgsoftware.superiorskyblock.schematics;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.schematics.data.SchematicBlock;
import com.bgsoftware.superiorskyblock.schematics.data.SchematicEntity;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.blocks.BlockChangeTask;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.tags.ByteTag;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.tags.FloatTag;
import com.bgsoftware.superiorskyblock.utils.tags.IntTag;
import com.bgsoftware.superiorskyblock.utils.tags.ListTag;
import com.bgsoftware.superiorskyblock.utils.tags.StringTag;
import com.bgsoftware.superiorskyblock.utils.tags.Tag;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SchematicPosition;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class SuperiorSchematic extends BaseSchematic implements Schematic {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final CompoundTag compoundTag;

    private final int[] offsets = new int[3];
    private final float yaw, pitch;
    private final int[] sizes = new int[3];
    private final SchematicBlock[][][] blocks;
    private final SchematicEntity[] entities;

    public SuperiorSchematic(String name, CompoundTag compoundTag){
        super(name);
        this.compoundTag = compoundTag;

        sizes[0] = parseTag(compoundTag.getValue().get("xSize"));
        sizes[1] = parseTag(compoundTag.getValue().get("ySize"));
        sizes[2] = parseTag(compoundTag.getValue().get("zSize"));

        offsets[0] = ((IntTag) compoundTag.getValue().getOrDefault("offsetX", new IntTag(sizes[0] / 2))).getValue();
        offsets[1] = ((IntTag) compoundTag.getValue().getOrDefault("offsetY", new IntTag(sizes[1] / 2))).getValue();
        offsets[2] = ((IntTag) compoundTag.getValue().getOrDefault("offsetZ", new IntTag(sizes[2] / 2))).getValue();
        yaw = ((FloatTag) compoundTag.getValue().getOrDefault("yaw", new FloatTag(0))).getValue();
        pitch = ((FloatTag) compoundTag.getValue().getOrDefault("pitch", new FloatTag(0))).getValue();

        blocks = new SchematicBlock[sizes[0] + 1][sizes[1] + 1][sizes[2] + 1];

        for (SchematicBlock[][] blocksSection : blocks)
            for (SchematicBlock[] _blocksSection : blocksSection)
                Arrays.fill(_blocksSection, SchematicBlock.AIR);

        if(compoundTag.getValue().containsKey("blocks")) {
            for(Tag<?> tag : ((ListTag) compoundTag.getValue().get("blocks")).getValue()){
                Map<String, Tag<?>> compoundValue = ((CompoundTag) tag).getValue();
                SchematicPosition schematicPosition = SchematicPosition.of(((StringTag) compoundValue.get("blockPosition")).getValue());
                int x = schematicPosition.getX(), y = schematicPosition.getY(), z = schematicPosition.getZ();
                int combinedId;

                if(compoundValue.containsKey("combinedId")){
                    combinedId = ((IntTag) compoundValue.get("combinedId")).getValue();
                }
                else if(compoundValue.containsKey("id") && compoundValue.containsKey("data")){
                    int id = ((IntTag) compoundValue.get("id")).getValue();
                    int data = ((IntTag) compoundValue.get("data")).getValue();
                    combinedId = id + (data << 12);
                }
                else if(compoundValue.containsKey("type")){
                    Material type;

                    try{
                        type = Material.valueOf(((StringTag) compoundValue.get("type")).getValue());
                    }catch (Exception ignored){
                        continue;
                    }

                    int data = ((IntTag) compoundValue.getOrDefault("data", new IntTag(0))).getValue();

                    combinedId = plugin.getNMSBlocks().getCombinedId(type, (byte) data);
                }
                else{
                    SuperiorSkyblockPlugin.log("&cCouldn't find combinedId for the block " + x + ", " + y + ", " + z + " - skipping...");
                    continue;
                }

                CompoundTag statesTag = (CompoundTag) compoundValue.get("states");
                CompoundTag tileEntity = (CompoundTag) compoundValue.get("tileEntity");
                blocks[x][y][z] = SchematicBlock.of(combinedId, statesTag, tileEntity);

                readBlock(blocks[x][y][z]);
            }
        }

        List<Tag<?>> entitiesList = new ArrayList<>();

        if(compoundTag.getValue().containsKey("entities"))
            entitiesList = ((ListTag) compoundTag.getValue().get("entities")).getValue();

        entities = new SchematicEntity[entitiesList.size()];

        for(int i = 0; i < entitiesList.size(); i++){
            Map<String, Tag<?>> compoundValue = ((CompoundTag) entitiesList.get(i)).getValue();
            EntityType entityType = EntityType.valueOf(((StringTag) compoundValue.get("entityType")).getValue());
            CompoundTag entityTag = (CompoundTag) compoundValue.get("NBT");
            Location offset = LocationUtils.getLocation(((StringTag) compoundValue.get("offset")).getValue());
            entities[i] = SchematicEntity.of(entityType, entityTag, offset);
        }
    }

    @Override
    public void pasteSchematic(Location location, Runnable callback){
        pasteSchematic(null, location, callback, null);
    }

    @Override
    public void pasteSchematic(Island island, Location location, Runnable callback) {
        pasteSchematic(island, location, callback, null);
    }

    @Override
    public void pasteSchematic(Island island, Location location, Runnable callback, Consumer<Throwable> onFailure) {
        if(!Bukkit.isPrimaryThread()){
            Executor.sync(() -> pasteSchematic(island, location, callback, onFailure));
            return;
        }

        try {
            if (schematicProgress) {
                pasteSchematicQueue.push(new PasteSchematicData(this, island, location, callback, onFailure));
                return;
            }

            SuperiorSkyblockPlugin.debug("Action: Paste Schematic, Island: " + island.getOwner().getName() + ", Location: " + LocationUtils.getLocation(location) + ", Schematic: " + name);

            schematicProgress = true;

            Location min = location.clone().subtract(offsets[0], offsets[1], offsets[2]);

            BlockChangeTask blockChangeTask = new BlockChangeTask(island);

            for (int y = 0; y <= sizes[1]; y++) {
                for (int x = 0; x <= sizes[0]; x++) {
                    for (int z = 0; z <= sizes[2]; z++) {
                        if (blocks[x][y][z].getCombinedId() > 0)
                            blocks[x][y][z].applyBlock(blockChangeTask, min.clone().add(x, y, z));
                    }
                }
            }

            blockChangeTask.submitUpdate(() -> {
                try {
                    for (SchematicEntity entity : entities) {
                        entity.spawnEntity(min);
                    }

                    ((SIsland) island).handleBlocksPlace(cachedCounts);
                    ((SIsland) island).saveDirtyChunks();

                    EventsCaller.callIslandSchematicPasteEvent(island, name, location);

                    callback.run();
                }catch(Throwable ex) {
                    if(onFailure != null)
                        onFailure.accept(ex);
                }finally {
                    Executor.sync(this::onSchematicFinish, 10L);
                }
            });
        }catch (Throwable ex){
            onSchematicFinish();
            if(onFailure != null)
                onFailure.accept(ex);
        }
    }

    @Override
    public Location getTeleportLocation(Location location) {
        location.setYaw(yaw);
        location.setPitch(pitch);
        return location;
    }

    public CompoundTag getTag(){
        return compoundTag;
    }

    private void readBlock(SchematicBlock block){
        int combinedId = block.getCombinedId();
        Key key = Key.of(plugin.getNMSBlocks().getMaterial(combinedId) + ":" + plugin.getNMSBlocks().getData(combinedId));
        cachedCounts.put(key, cachedCounts.getRaw(key, 0) + 1);
    }

    private static int parseTag(Tag<?> tag){
        if(tag instanceof ByteTag)
            return ((ByteTag) tag).getValue();
        else
            return ((IntTag) tag).getValue();
    }

}
