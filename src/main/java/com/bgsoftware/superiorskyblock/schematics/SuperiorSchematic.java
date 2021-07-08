package com.bgsoftware.superiorskyblock.schematics;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.schematics.data.SchematicBlock;
import com.bgsoftware.superiorskyblock.schematics.data.SchematicEntity;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.blocks.BlockChangeTask;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.key.Key;
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
import com.google.common.collect.Maps;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class SuperiorSchematic extends BaseSchematic implements Schematic {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final EnumMap<BlockFace, Byte> rotationToByte = Maps.newEnumMap(BlockFace.class);

    static {
        rotationToByte.put(BlockFace.EAST, (byte) 4);
        rotationToByte.put(BlockFace.SOUTH, (byte) 8);
        rotationToByte.put(BlockFace.WEST, (byte) 12);
        rotationToByte.put(BlockFace.NORTH_EAST, (byte) 2);
        rotationToByte.put(BlockFace.NORTH_WEST, (byte) 14);
        rotationToByte.put(BlockFace.SOUTH_EAST, (byte) 6);
        rotationToByte.put(BlockFace.SOUTH_WEST, (byte) 10);
        rotationToByte.put(BlockFace.WEST_NORTH_WEST, (byte) 13);
        rotationToByte.put(BlockFace.NORTH_NORTH_WEST, (byte) 15);
        rotationToByte.put(BlockFace.NORTH_NORTH_EAST, (byte) 1);
        rotationToByte.put(BlockFace.EAST_NORTH_EAST, (byte) 3);
        rotationToByte.put(BlockFace.EAST_SOUTH_EAST, (byte) 5);
        rotationToByte.put(BlockFace.SOUTH_SOUTH_EAST, (byte) 7);
        rotationToByte.put(BlockFace.SOUTH_SOUTH_WEST, (byte) 9);
        rotationToByte.put(BlockFace.WEST_SOUTH_WEST, (byte) 11);
    }

    private final CompoundTag compoundTag;

    private final int[] offsets = new int[3];
    private final float yaw, pitch;
    private final int[] sizes = new int[3];
    private final SchematicBlock[][][] blocks;
    private final SchematicEntity[] entities;

    private Set<ChunkPosition> loadedChunks = null;

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

                byte skyLightLevel = ((ByteTag) compoundValue.getOrDefault("skyLightLevel", new ByteTag((byte)0))).getValue();
                byte blockLightLevel = ((ByteTag) compoundValue.getOrDefault("blockLightLevel", new ByteTag((byte)0))).getValue();

                parseOldTileEntity((CompoundTag) tag);

                CompoundTag statesTag = (CompoundTag) compoundValue.get("states");
                CompoundTag tileEntity = (CompoundTag) compoundValue.get("tileEntity");
                blocks[x][y][z] = SchematicBlock.of(combinedId, skyLightLevel, blockLightLevel, statesTag, tileEntity);

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
            SuperiorSkyblockPlugin.debug("Action: Paste Schematic, Island: " + island.getOwner().getName() + ", Location: " + LocationUtils.getLocation(location) + ", Schematic: " + name);

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

                    island.handleBlocksPlace(cachedCounts);

                    EventsCaller.callIslandSchematicPasteEvent(island, name, location);

                    loadedChunks = blockChangeTask.getLoadedChunks();
                    callback.run();
                    loadedChunks = null;

                }catch(Throwable ex) {
                    if(onFailure != null)
                        onFailure.accept(ex);
                }
            });
        }catch (Throwable ex){
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

    public Set<ChunkPosition> getLoadedChunks() {
        return loadedChunks;
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

    private static void parseOldTileEntity(CompoundTag compoundTag){
        CompoundTag tileEntity = new CompoundTag();

        {
            String baseColor = compoundTag.getString("baseColor");
            if(baseColor != null)
                //noinspection deprecation
                tileEntity.setInt("Base", DyeColor.valueOf(baseColor).getDyeData());
        }

        {
            CompoundTag patterns = compoundTag.getCompound("patterns");
            if(patterns != null) {
                ListTag patternsList = new ListTag(CompoundTag.class, new ArrayList<>());

                for(Tag<?> tag : patterns){
                    if(tag instanceof CompoundTag){
                        CompoundTag oldPatternTag = (CompoundTag) tag;
                        CompoundTag patternTag = new CompoundTag();
                        patternTag.setInt("Color", oldPatternTag.getInt("color"));
                        patternTag.setString("Pattern", oldPatternTag.getString("type"));
                        patternsList.addTag(patternTag);
                    }
                }

                tileEntity.setTag("Patterns", patternsList);
            }
        }

        {
            CompoundTag contents = compoundTag.getCompound("contents");
            if(contents != null){
                ListTag items = new ListTag(CompoundTag.class, new ArrayList<>());
                for(Map.Entry<String, Tag<?>> item : contents.entrySet()){
                    if(item.getValue() instanceof CompoundTag) {
                        try {
                            ItemStack itemStack = TagUtils.compoundToItem((CompoundTag) item.getValue());
                            CompoundTag itemCompound = plugin.getNMSAdapter().getNMSCompound(itemStack);
                            itemCompound.setByte("Slot", Byte.parseByte(item.getKey()));
                            items.addTag(itemCompound);
                        } catch (Exception ignored) {
                        }
                    }
                }

                tileEntity.setTag("Items", items);

                String inventoryType = compoundTag.getString("inventoryType");
                tileEntity.setString("inventoryType", inventoryType != null ? inventoryType : "CHEST");
            }
        }

        {
            String flower = compoundTag.getString("flower");
            if(flower != null){
                try {
                    String[] flowerSections = flower.split(":");
                    tileEntity.setString("Item", plugin.getNMSAdapter().getMinecraftKey(new ItemStack(Material.valueOf(flowerSections[0]))));
                    tileEntity.setInt("Data", Integer.parseInt(flowerSections[1]));
                }catch (Exception ignored){}
            }
        }

        {
            String skullType = compoundTag.getString("skullType");
            if(skullType != null){
                tileEntity.setByte("SkullType", (byte) (SkullType.valueOf(skullType).ordinal() - 1));
            }
        }

        {
            String rotation = compoundTag.getString("rotation");
            if(rotation != null){
                tileEntity.setByte("Rot", rotationToByte.getOrDefault(BlockFace.valueOf(rotation), (byte) 0));
            }
        }

        {
            String owner = compoundTag.getString("owner");
            if(owner != null){
                tileEntity.setString("Name", owner);
            }
        }

        {
            String signLine0 = compoundTag.getString("signLine0");
            if(signLine0 != null){
                tileEntity.setString("Text1", plugin.getNMSBlocks().parseSignLine(signLine0));
            }
        }

        {
            String signLine1 = compoundTag.getString("signLine1");
            if(signLine1 != null){
                tileEntity.setString("Text2", plugin.getNMSBlocks().parseSignLine(signLine1));
            }
        }

        {
            String signLine2 = compoundTag.getString("signLine2");
            if(signLine2 != null){
                tileEntity.setString("Text3", plugin.getNMSBlocks().parseSignLine(signLine2));
            }
        }

        {
            String signLine3 = compoundTag.getString("signLine3");
            if(signLine3 != null){
                tileEntity.setString("Text4", plugin.getNMSBlocks().parseSignLine(signLine3));
            }
        }

        {
            String spawnedType = compoundTag.getString("spawnedType");
            if(spawnedType != null){
                tileEntity.setString("EntityId", spawnedType);
            }
        }

        if(tileEntity.size() != 0)
            compoundTag.setTag("tileEntity", tileEntity);
    }

}
