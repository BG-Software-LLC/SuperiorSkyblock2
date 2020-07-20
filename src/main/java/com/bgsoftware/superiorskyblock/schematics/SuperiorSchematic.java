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
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

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

                if(compoundValue.containsKey("baseColor") || compoundValue.containsKey("patterns")) {
                    blocks[x][y][z] = SchematicBlock.of(
                            combinedId, statesTag,
                            getOrNull(DyeColor.class, ((StringTag) compoundValue.getOrDefault("baseColor", new StringTag(null))).getValue()),
                            TagUtils.getPatternsFromTag((CompoundTag) compoundValue.getOrDefault("patterns", new CompoundTag()))
                    );
                }

                else if(compoundValue.containsKey("contents")){
                    ItemStack[] contents = null;

                    if(plugin.getSettings().defaultContainersEnabled){
                        InventoryType containerType = InventoryType.valueOf(((StringTag) compoundValue.getOrDefault("inventoryType", new StringTag("CHEST"))).getValue());
                        Registry<Integer, ItemStack> containerContents = plugin.getSettings().defaultContainersContents.get(containerType);
                        if(containerContents != null) {
                            contents = new ItemStack[27];
                            for (Map.Entry<Integer, ItemStack> entry : containerContents.entries())
                                contents[entry.getKey()] = entry.getValue().clone();
                        }
                    }

                    if(contents == null){
                        contents = TagUtils.compoundToInventory((CompoundTag) compoundValue.get("contents"));
                    }

                    String containerName = ((StringTag) compoundValue.getOrDefault("name", new StringTag(""))).getValue();

                    blocks[x][y][z] = SchematicBlock.of(combinedId, statesTag, contents, containerName);
                }

                else if(compoundValue.containsKey("flower")){
                    String[] sections = ((StringTag) compoundValue.get("flower")).getValue().split(":");
                    blocks[x][y][z] = SchematicBlock.of(combinedId, statesTag,
                            new ItemStack(Material.valueOf(sections[0]), 1, Short.parseShort(sections[1])));
                }

                else if(compoundValue.containsKey("skullType") || compoundValue.containsKey("rotation") || compoundValue.containsKey("owner")){
                    blocks[x][y][z] = SchematicBlock.of(
                            combinedId, statesTag,
                            getOrNull(SkullType.class, ((StringTag) compoundValue.getOrDefault("skullType", new StringTag(null))).getValue()),
                            getOrNull(BlockFace.class, ((StringTag) compoundValue.getOrDefault("rotation", new StringTag(null))).getValue()),
                            ((StringTag) compoundValue.getOrDefault("owner", new StringTag(null))).getValue()
                    );
                }

                else if(compoundValue.containsKey("signLine1") || compoundValue.containsKey("signLine2") || compoundValue.containsKey("signLine3") || compoundValue.containsKey("signLine4")){
                    String[] lines = new String[] { "", "", "", "" };

                    for(int i = 0; i < 4; i++)
                        lines[i] = ((StringTag) compoundValue.getOrDefault("signLine" + i, new StringTag(""))).getValue();

                    blocks[x][y][z] = SchematicBlock.of(combinedId, statesTag, lines);
                }

                else if(compoundValue.containsKey("spawnedType")){
                    blocks[x][y][z] = SchematicBlock.of(
                            combinedId, statesTag,
                            getOrValue(EntityType.class, ((StringTag) compoundValue.get("spawnedType")).getValue(), EntityType.PIG)
                    );
                }

                else{
                    blocks[x][y][z] = SchematicBlock.of(combinedId, statesTag);
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
                            blocks[x][y][z].applyBlock(blockChangeTask, min.clone().add(x, y, z), island);
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

    private static <T extends Enum<T>> T getOrNull(Class<T> enumType, String name){
        return getOrValue(enumType, name, null);
    }

    private static <T extends Enum<T>> T getOrValue(Class<T> enumType, String name, T value){
        try {
            return Enum.valueOf(enumType, name);
        }catch(IllegalArgumentException ex){
            return value;
        }
    }

}
