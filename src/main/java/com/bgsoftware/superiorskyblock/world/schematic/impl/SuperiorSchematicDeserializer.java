package com.bgsoftware.superiorskyblock.world.schematic.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.schematic.SchematicBlock;
import com.bgsoftware.superiorskyblock.core.schematic.SchematicBlockData;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.google.common.collect.Maps;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class SuperiorSchematicDeserializer {

    private static final ListTag EMPTY_ITEMS_LIST = ListTag.of(Collections.emptyList());

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

    private SuperiorSchematicDeserializer() {

    }

    public static void convertOldTileEntity(CompoundTag compoundTag) {
        CompoundTag tileEntity = CompoundTag.of();

        compoundTag.getString("baseColor").ifPresent(baseColor ->
                tileEntity.setInt("Base", DyeColor.valueOf(baseColor).getDyeData()));

        compoundTag.getCompound("patterns").ifPresent(patterns -> {
            ListTag patternsList = ListTag.of(CompoundTag.class);

            for (Tag<?> tag : patterns) {
                if (tag instanceof CompoundTag) {
                    CompoundTag oldPatternTag = (CompoundTag) tag;
                    CompoundTag patternTag = CompoundTag.of();
                    patternTag.setInt("Color", oldPatternTag.getInt("color").orElse(0));
                    patternTag.setString("Pattern", oldPatternTag.getString("type").orElse(""));
                    patternsList.addTag(patternTag);
                }
            }

            tileEntity.setTag("Patterns", patternsList);
        });

        compoundTag.getCompound("contents").ifPresent(contents -> {
            ListTag items = ListTag.of(CompoundTag.class);
            for (Map.Entry<String, Tag<?>> item : contents.entrySet()) {
                if (item.getValue() instanceof CompoundTag) {
                    try {
                        ItemStack itemStack = Serializers.ITEM_STACK_TO_TAG_SERIALIZER.deserialize((CompoundTag) item.getValue());
                        CompoundTag itemCompound = plugin.getNMSTags().serializeItem(itemStack);
                        itemCompound.setByte("Slot", Byte.parseByte(item.getKey()));
                        items.addTag(itemCompound);
                    } catch (Exception ignored) {
                    }
                }
            }

            tileEntity.setTag("Items", items);

            String inventoryType = compoundTag.getString("inventoryType").orElse("CHEST");
            tileEntity.setString("inventoryType", inventoryType);
        });

        compoundTag.getString("flower").ifPresent(flower -> {
            try {
                String[] flowerSections = flower.split(":");
                tileEntity.setString("Item", plugin.getNMSAlgorithms().getMinecraftKey(new ItemStack(Material.valueOf(flowerSections[0]))));
                tileEntity.setInt("Data", Integer.parseInt(flowerSections[1]));
            } catch (Exception ignored) {
            }
        });

        compoundTag.getString("skullType").ifPresent(skullType ->
                tileEntity.setByte("SkullType", (byte) (SkullType.valueOf(skullType).ordinal() - 1)));

        compoundTag.getString("rotation").ifPresent(rotation ->
                tileEntity.setByte("Rot", rotationToByte.getOrDefault(BlockFace.valueOf(rotation), (byte) 0)));

        compoundTag.getString("owner").ifPresent(owner ->
                tileEntity.setString("Name", owner));

        for (int i = 0; i < 4; ++i) {
            final String textLineKey = "Text" + (i + 1);
            compoundTag.getString("signLine" + i).ifPresent(signLine ->
                    tileEntity.setString(textLineKey, plugin.getNMSAlgorithms().parseSignLine(signLine)));
        }

        compoundTag.getString("spawnedType").ifPresent(spawnedType ->
                tileEntity.setString("EntityId", spawnedType));

        if (!tileEntity.isEmpty())
            compoundTag.setTag("tileEntity", tileEntity);
    }

    @Nullable
    public static SchematicBlockData deserializeSchematicBlock(CompoundTag compoundTag, int dataVersion) {
        BlockOffset blockOffset = Serializers.OFFSET_SERIALIZER.deserialize(compoundTag.getString("blockPosition").orElse(null));
        int combinedId;

        if (compoundTag.containsKey("combinedId")) {
            combinedId = compoundTag.getInt("combinedId").getAsInt();
        } else if (compoundTag.containsKey("id") && compoundTag.containsKey("data")) {
            int id = compoundTag.getInt("id").getAsInt();
            int data = compoundTag.getInt("data").getAsInt();
            combinedId = id + (data << 12);
        } else if (compoundTag.containsKey("type")) {
            Material type;

            try {
                type = Material.valueOf(compoundTag.getString("type").get());
            } catch (Exception ignored) {
                return null;
            }

            int data = compoundTag.getInt("data").orElse(0);

            combinedId = plugin.getNMSAlgorithms().getCombinedId(type, (byte) data);
        } else {
            Log.warn("Couldn't find combinedId for the block ", compoundTag.getString("blockPosition"), " - skipping...");
            return null;
        }

        SuperiorSchematicDeserializer.convertOldTileEntity(compoundTag);

        SchematicBlock.Extra extra = deserializeSchematicBlockExtra(compoundTag, dataVersion);

        if (extra != null) {
            CompoundTag tileEntityTag = extra.getTileEntity();
            if (tileEntityTag != null && !tileEntityTag.containsKey("id")) {
                // We try to detect the id from its combinedId
                plugin.getNMSAlgorithms().getTileEntityIdFromCombinedId(combinedId).ifPresent(tileEntityId -> {
                    tileEntityTag.setString("id", tileEntityId);
                });
            }
        }

        return new SchematicBlockData(combinedId, blockOffset, extra);
    }

    private static SchematicBlock.Extra deserializeSchematicBlockExtra(CompoundTag compoundTag, int dataVersion) {
        // Ignore light levels
        // byte skyLightLevel = compoundTag.getByte("skyLightLevel");
        // byte blockLightLevel = compoundTag.getByte("blockLightLevel");

        CompoundTag statesTag = compoundTag.getCompound("states").orElse(null);
        CompoundTag tileEntity = compoundTag.getCompound("tileEntity").orElse(null);
        tileEntity = SuperiorSchematicDeserializer.upgradeTileEntity(tileEntity, dataVersion);

        return statesTag == null && tileEntity == null ? null : new SchematicBlock.Extra(statesTag, tileEntity);
    }

    @Nullable
    private static CompoundTag upgradeTileEntity(@Nullable CompoundTag compoundTag, int dataVersion) {
        if (compoundTag == null || dataVersion == -1)
            return compoundTag;

        int currentDataVersion = plugin.getNMSAlgorithms().getDataVersion();
        if (currentDataVersion <= dataVersion)
            return compoundTag;

        // Convert chest contents
        ListTag itemsTag = compoundTag.getList("Items").orElse(EMPTY_ITEMS_LIST);
        if (itemsTag.size() > 0) {
            ListTag newItemsTag = ListTag.of(CompoundTag.class);

            for (Tag<?> tag : itemsTag) {
                if (tag instanceof CompoundTag) {
                    CompoundTag itemTag = (CompoundTag) tag;
                    int slot = itemTag.getInt("Slot").orElse(0);
                    itemTag.setInt("DataVersion", dataVersion);
                    ItemStack itemStack = Serializers.ITEM_STACK_TO_TAG_SERIALIZER.deserialize(itemTag);
                    CompoundTag newItemTag = Serializers.ITEM_STACK_TO_TAG_SERIALIZER.serialize(itemStack);
                    newItemTag.setInt("Slot", slot);
                    newItemsTag.addTag(newItemTag);
                }
            }

            if (newItemsTag.size() > 0)
                compoundTag.setTag("Items", newItemsTag);
        }

        return compoundTag;
    }

}
