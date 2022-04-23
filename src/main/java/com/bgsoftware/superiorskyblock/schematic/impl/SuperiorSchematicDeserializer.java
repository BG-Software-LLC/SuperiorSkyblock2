package com.bgsoftware.superiorskyblock.schematic.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.schematic.data.SchematicBlock;
import com.bgsoftware.superiorskyblock.schematic.data.SchematicPosition;
import com.bgsoftware.superiorskyblock.tag.ByteTag;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.IntTag;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import com.bgsoftware.superiorskyblock.tag.StringTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.bgsoftware.superiorskyblock.tag.TagUtils;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.wrappers.SBlockOffset;
import com.google.common.collect.Maps;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

public final class SuperiorSchematicDeserializer {

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

    public static int readNumberFromTag(Tag<?> tag) {
        if (tag instanceof ByteTag)
            return ((ByteTag) tag).getValue();
        else
            return ((IntTag) tag).getValue();
    }

    public static void convertOldTileEntity(CompoundTag compoundTag) {
        CompoundTag tileEntity = new CompoundTag();

        {
            String baseColor = compoundTag.getString("baseColor");
            if (baseColor != null)
                //noinspection deprecation
                tileEntity.setInt("Base", DyeColor.valueOf(baseColor).getDyeData());
        }

        {
            CompoundTag patterns = compoundTag.getCompound("patterns");
            if (patterns != null) {
                ListTag patternsList = new ListTag(CompoundTag.class, new ArrayList<>());

                for (Tag<?> tag : patterns) {
                    if (tag instanceof CompoundTag) {
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
            if (contents != null) {
                ListTag items = new ListTag(CompoundTag.class, new ArrayList<>());
                for (Map.Entry<String, Tag<?>> item : contents.entrySet()) {
                    if (item.getValue() instanceof CompoundTag) {
                        try {
                            ItemStack itemStack = TagUtils.compoundToItem((CompoundTag) item.getValue());
                            CompoundTag itemCompound = plugin.getNMSTags().convertToNBT(itemStack);
                            itemCompound.setByte("Slot", Byte.parseByte(item.getKey()));
                            items.addTag(itemCompound);
                        } catch (Exception error) {
                            PluginDebugger.debug(error);
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
            if (flower != null) {
                try {
                    String[] flowerSections = flower.split(":");
                    tileEntity.setString("Item", plugin.getNMSAlgorithms().getMinecraftKey(new ItemStack(Material.valueOf(flowerSections[0]))));
                    tileEntity.setInt("Data", Integer.parseInt(flowerSections[1]));
                } catch (Exception error) {
                    PluginDebugger.debug(error);
                }
            }
        }

        {
            String skullType = compoundTag.getString("skullType");
            if (skullType != null) {
                tileEntity.setByte("SkullType", (byte) (SkullType.valueOf(skullType).ordinal() - 1));
            }
        }

        {
            String rotation = compoundTag.getString("rotation");
            if (rotation != null) {
                tileEntity.setByte("Rot", rotationToByte.getOrDefault(BlockFace.valueOf(rotation), (byte) 0));
            }
        }

        {
            String owner = compoundTag.getString("owner");
            if (owner != null) {
                tileEntity.setString("Name", owner);
            }
        }

        {
            String signLine0 = compoundTag.getString("signLine0");
            if (signLine0 != null) {
                tileEntity.setString("Text1", plugin.getNMSAlgorithms().parseSignLine(signLine0));
            }
        }

        {
            String signLine1 = compoundTag.getString("signLine1");
            if (signLine1 != null) {
                tileEntity.setString("Text2", plugin.getNMSAlgorithms().parseSignLine(signLine1));
            }
        }

        {
            String signLine2 = compoundTag.getString("signLine2");
            if (signLine2 != null) {
                tileEntity.setString("Text3", plugin.getNMSAlgorithms().parseSignLine(signLine2));
            }
        }

        {
            String signLine3 = compoundTag.getString("signLine3");
            if (signLine3 != null) {
                tileEntity.setString("Text4", plugin.getNMSAlgorithms().parseSignLine(signLine3));
            }
        }

        {
            String spawnedType = compoundTag.getString("spawnedType");
            if (spawnedType != null) {
                tileEntity.setString("EntityId", spawnedType);
            }
        }

        if (tileEntity.size() != 0)
            compoundTag.setTag("tileEntity", tileEntity);
    }

    @Nullable
    public static SchematicBlock deserializeSchematicBlock(Tag<?> tag) {
        Map<String, Tag<?>> compoundValue = ((CompoundTag) tag).getValue();
        SchematicPosition schematicPosition = SchematicPosition.of(((StringTag) compoundValue.get("blockPosition")).getValue());
        int x = schematicPosition.getX();
        int y = schematicPosition.getY();
        int z = schematicPosition.getZ();
        int combinedId;

        if (compoundValue.containsKey("combinedId")) {
            combinedId = ((IntTag) compoundValue.get("combinedId")).getValue();
        } else if (compoundValue.containsKey("id") && compoundValue.containsKey("data")) {
            int id = ((IntTag) compoundValue.get("id")).getValue();
            int data = ((IntTag) compoundValue.get("data")).getValue();
            combinedId = id + (data << 12);
        } else if (compoundValue.containsKey("type")) {
            Material type;

            try {
                type = Material.valueOf(((StringTag) compoundValue.get("type")).getValue());
            } catch (Exception ignored) {
                return null;
            }

            int data = ((IntTag) compoundValue.getOrDefault("data", new IntTag(0))).getValue();

            combinedId = plugin.getNMSAlgorithms().getCombinedId(type, (byte) data);
        } else {
            SuperiorSkyblockPlugin.log("&cCouldn't find combinedId for the block " + x + ", " + y + ", " + z + " - skipping...");
            return null;
        }

        byte skyLightLevel = ((ByteTag) compoundValue.getOrDefault("skyLightLevel", new ByteTag((byte) 0))).getValue();
        byte blockLightLevel = ((ByteTag) compoundValue.getOrDefault("blockLightLevel", new ByteTag((byte) 0))).getValue();

        SuperiorSchematicDeserializer.convertOldTileEntity((CompoundTag) tag);

        CompoundTag statesTag = (CompoundTag) compoundValue.get("states");
        CompoundTag tileEntity = (CompoundTag) compoundValue.get("tileEntity");

        return new SchematicBlock(combinedId, SBlockOffset.fromOffsets(x, y, z), skyLightLevel, blockLightLevel, statesTag, tileEntity);
    }

}
