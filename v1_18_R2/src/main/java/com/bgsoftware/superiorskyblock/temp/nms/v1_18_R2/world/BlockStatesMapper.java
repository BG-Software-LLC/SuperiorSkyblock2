package com.bgsoftware.superiorskyblock.temp.nms.v1_18_R2.world;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.temp.nms.v1_18_R2.mapping.level.block.state.properties.BlockState;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import net.minecraft.world.level.block.state.properties.IBlockState;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class BlockStatesMapper {

    private static final Map<String, BlockState<?>> nameToBlockState = new HashMap<>();
    private static final Map<IBlockState<?>, String> blockStateToName = new HashMap<>();

    static {
        Map<String, String> fieldNameToName = new HashMap<>();
        fieldNameToName.put("G", "axis-empty");
        fieldNameToName.put("O", "facing-notup");
        fieldNameToName.put("P", "facing-horizontal");
        fieldNameToName.put("T", "wall-east");
        fieldNameToName.put("U", "wall-north");
        fieldNameToName.put("V", "wall-south");
        fieldNameToName.put("W", "wall-west");
        fieldNameToName.put("X", "redstone-east");
        fieldNameToName.put("Y", "redstone-north");
        fieldNameToName.put("Z", "redstone-south");
        fieldNameToName.put("aa", "redstone-west");
        fieldNameToName.put("ab", "double-half");
        fieldNameToName.put("ad", "track-shape-empty");
        fieldNameToName.put("ae", "track-shape");
        fieldNameToName.put("am", "age1");
        fieldNameToName.put("an", "age2");
        fieldNameToName.put("ao", "age3");
        fieldNameToName.put("ap", "age5");
        fieldNameToName.put("aq", "age7");
        fieldNameToName.put("ar", "age15");
        fieldNameToName.put("as", "age25");
        fieldNameToName.put("aF", "level3");
        fieldNameToName.put("aG", "level8");
        fieldNameToName.put("aH", "level1-8");
        fieldNameToName.put("aK", "level15");
        fieldNameToName.put("ax", "distance1-7");
        fieldNameToName.put("aR", "distance7");
        fieldNameToName.put("aY", "chest-type");
        fieldNameToName.put("aZ", "comparator-mode");
        fieldNameToName.put("bc", "piston-type");
        fieldNameToName.put("bd", "slab-type");

        try {
            // Fixes BlockProperties being private-class in some versions of Yatopia causing illegal access errors.
            Class<?> blockPropertiesClass = Class.forName("net.minecraft.world.level.block.state.properties.BlockProperties");

            for (Field field : blockPropertiesClass.getFields()) {
                field.setAccessible(true);
                Object value = field.get(null);
                if (value instanceof IBlockState) {
                    BlockState<?> blockState = new BlockState<>((IBlockState<?>) value);
                    register(fieldNameToName.getOrDefault(field.getName(), blockState.getName()),
                            field.getName(), blockState);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            PluginDebugger.debug(ex);
        }

    }

    private static void register(String key, String fieldName, BlockState<?> blockState) {
        if (nameToBlockState.containsKey(key)) {
            SuperiorSkyblockPlugin.log("&cWarning: block state " + key + "(" + fieldName + ") already exists. Contact Ome_R!");
        } else {
            nameToBlockState.put(key, blockState);
            blockStateToName.put(blockState.getHandle(), key);
        }
    }

    public static BlockState<?> getBlockState(String name) {
        return nameToBlockState.get(name);
    }

    public static String getBlockStateName(IBlockState<?> blockState) {
        return blockStateToName.get(blockState);
    }

}
