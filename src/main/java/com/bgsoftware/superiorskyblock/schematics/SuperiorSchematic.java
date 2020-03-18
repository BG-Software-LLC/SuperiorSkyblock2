package com.bgsoftware.superiorskyblock.schematics;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandSchematicPasteEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.schematics.data.SchematicBlock;
import com.bgsoftware.superiorskyblock.schematics.data.SchematicEntity;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.blocks.BlockChangeTask;
import com.bgsoftware.superiorskyblock.utils.tags.ByteTag;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.tags.FloatTag;
import com.bgsoftware.superiorskyblock.utils.tags.IntTag;
import com.bgsoftware.superiorskyblock.utils.tags.ListTag;
import com.bgsoftware.superiorskyblock.utils.tags.StringTag;
import com.bgsoftware.superiorskyblock.utils.tags.Tag;
import com.bgsoftware.superiorskyblock.utils.tags.TagUtils;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SchematicPosition;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class SuperiorSchematic extends BaseSchematic implements Schematic {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final CompoundTag compoundTag;

    private final int[] offsets = new int[3];
    private final float yaw, pitch;
    private final byte[] sizes = new byte[3];
    private final SchematicBlock[][][] blocks;
    private final SchematicEntity[] entities;

    public SuperiorSchematic(String name, CompoundTag compoundTag){
        super(name);
        this.compoundTag = compoundTag;

        sizes[0] = ((ByteTag) compoundTag.getValue().get("xSize")).getValue();
        sizes[1] = ((ByteTag) compoundTag.getValue().get("ySize")).getValue();
        sizes[2] = ((ByteTag) compoundTag.getValue().get("zSize")).getValue();

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
                else{
                    SuperiorSkyblockPlugin.log("&cCouldn't find combinedId for the block " + x + ", " + y + ", " + z + " - skipping...");
                    continue;
                }

                if(compoundValue.containsKey("baseColor") || compoundValue.containsKey("patterns")) {
                    blocks[x][y][z] = SchematicBlock.of(
                            combinedId,
                            getOrNull(DyeColor.class, ((StringTag) compoundValue.getOrDefault("baseColor", new StringTag(null))).getValue()),
                            TagUtils.getPatternsFromTag((CompoundTag) compoundValue.getOrDefault("patterns", new CompoundTag()))
                    );
                }

                else if(compoundValue.containsKey("contents")){
                    ItemStack[] contents = TagUtils.compoundToInventory((CompoundTag) compoundValue.get("contents"));

                    if(plugin.getSettings().starterChestEnabled){
                        contents = new ItemStack[27];

                        for(Map.Entry<Integer, ItemStack> entry : plugin.getSettings().starterChestContents.entries())
                            contents[entry.getKey()] = entry.getValue().clone();
                    }

                    blocks[x][y][z] = SchematicBlock.of(combinedId, contents);
                }

                else if(compoundValue.containsKey("flower")){
                    String[] sections = ((StringTag) compoundValue.get("flower")).getValue().split(":");
                    blocks[x][y][z] = SchematicBlock.of(combinedId,
                            new ItemStack(Material.valueOf(sections[0]), 1, Short.parseShort(sections[1])));
                }

                else if(compoundValue.containsKey("skullType") || compoundValue.containsKey("rotation") || compoundValue.containsKey("owner")){
                    blocks[x][y][z] = SchematicBlock.of(
                            combinedId,
                            getOrNull(SkullType.class, ((StringTag) compoundValue.getOrDefault("skullType", new StringTag(null))).getValue()),
                            getOrNull(BlockFace.class, ((StringTag) compoundValue.getOrDefault("rotation", new StringTag(null))).getValue()),
                            ((StringTag) compoundValue.getOrDefault("owner", new StringTag(null))).getValue()
                    );
                }

                else if(compoundValue.containsKey("signLine1") || compoundValue.containsKey("signLine2") || compoundValue.containsKey("signLine3") || compoundValue.containsKey("signLine4")){
                    List<String> lines = new ArrayList<>(4);

                    for(int i = 0; i < 4; i++)
                        lines.add(i, ((StringTag) compoundValue.getOrDefault("signLine" + i, new StringTag(""))).getValue());

                    blocks[x][y][z] = SchematicBlock.of(
                            combinedId,
                            lines.toArray(new String[0])
                    );
                }

                else if(compoundValue.containsKey("spawnedType")){
                    blocks[x][y][z] = SchematicBlock.of(
                            combinedId,
                            getOrValue(EntityType.class, ((StringTag) compoundValue.get("spawnedType")).getValue(), EntityType.PIG)
                    );
                }

                else{
                    blocks[x][y][z] = SchematicBlock.of(combinedId);
                }

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
        pasteSchematic(null, location, callback);
    }

    @Override
    public void pasteSchematic(Island island, Location location, Runnable callback) {
        if(!Bukkit.isPrimaryThread()){
            Executor.sync(() -> pasteSchematic(island, location, callback));
            return;
        }

        if(schematicProgress) {
            pasteSchematicQueue.push(new PasteSchematicData(this, island, location, callback));
            return;
        }

        schematicProgress = true;

        Location min = location.clone().subtract(offsets[0], offsets[1], offsets[2]);

        BlockChangeTask blockChangeTask = new BlockChangeTask();

        for(int y = 0; y <= sizes[1]; y++){
            for(int x = 0; x <= sizes[0]; x++){
                for(int z = 0; z <= sizes[2]; z++) {
                    if (blocks[x][y][z].getCombinedId() > 0)
                        blocks[x][y][z].applyBlock(blockChangeTask, min.clone().add(x, y, z), island);
                }
            }
        }

        blockChangeTask.submitUpdate(() -> {
            for(SchematicEntity entity : entities) {
                entity.spawnEntity(min);
            }

            ((SIsland) island).handleBlocksPlace(cachedCounts);
            ((SIsland) island).saveDirtyChunks();

            IslandSchematicPasteEvent islandSchematicPasteEvent = new IslandSchematicPasteEvent(island, name, location);
            Bukkit.getPluginManager().callEvent(islandSchematicPasteEvent);

            callback.run();

            Executor.sync(() -> {
                schematicProgress = false;

                if (pasteSchematicQueue.size() != 0) {
                    PasteSchematicData data = pasteSchematicQueue.pop();
                    data.schematic.pasteSchematic(data.island, data.location, data.callback);
                }
            }, 10L);
        });
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
        Key key = Key.of(plugin.getNMSBlocks().getMaterial(combinedId), plugin.getNMSBlocks().getData(combinedId));
        cachedCounts.put(key, cachedCounts.getRaw(key, 0) + 1);
    }

    private <T extends Enum<T>> T getOrNull(Class<T> enumType, String name){
        return getOrValue(enumType, name, null);
    }

    private <T extends Enum<T>> T getOrValue(Class<T> enumType, String name, T value){
        try {
            return Enum.valueOf(enumType, name);
        }catch(IllegalArgumentException ex){
            return value;
        }
    }

}
