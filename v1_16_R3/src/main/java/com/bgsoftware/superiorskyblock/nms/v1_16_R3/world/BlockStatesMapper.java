package com.bgsoftware.superiorskyblock.nms.v1_16_R3.world;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import net.minecraft.server.v1_16_R3.IBlockState;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class BlockStatesMapper {

    private static final Map<String, IBlockState<?>> nameToBlockState = new HashMap<>();
    private static final Map<IBlockState<?>, String> blockStateToName = new HashMap<>();

    static {
        Map<String, String> fieldNameToName = new HashMap<>();
        fieldNameToName.put("F", "axis-empty");
        fieldNameToName.put("N", "facing-notup");
        fieldNameToName.put("O", "facing-horizontal");
        fieldNameToName.put("S", "wall-east");
        fieldNameToName.put("T", "wall-north");
        fieldNameToName.put("U", "wall-south");
        fieldNameToName.put("V", "wall-west");
        fieldNameToName.put("W", "redstone-east");
        fieldNameToName.put("X", "redstone-north");
        fieldNameToName.put("Y", "redstone-south");
        fieldNameToName.put("Z", "redstone-west");
        fieldNameToName.put("aa", "double-half");
        fieldNameToName.put("ac", "track-shape-empty");
        fieldNameToName.put("ad", "track-shape");
        fieldNameToName.put("ae", "age1");
        fieldNameToName.put("af", "age2");
        fieldNameToName.put("ag", "age3");
        fieldNameToName.put("ah", "age5");
        fieldNameToName.put("ai", "age7");
        fieldNameToName.put("aj", "age15");
        fieldNameToName.put("ak", "age25");
        fieldNameToName.put("ar", "level3");
        fieldNameToName.put("as", "level8");
        fieldNameToName.put("at", "level1-8");
        fieldNameToName.put("av", "level15");
        fieldNameToName.put("an", "distance1-7");
        fieldNameToName.put("aB", "distance7");
        fieldNameToName.put("aF", "chest-type");
        fieldNameToName.put("aG", "comparator-mode");
        fieldNameToName.put("aJ", "piston-type");
        fieldNameToName.put("aK", "slab-type");

        try {
            // Fixes BlockProperties being private-class in some versions of Yatopia causing illegal access errors.
            Class<?> blockPropertiesClass = Class.forName("net.minecraft.server.v1_16_R3.BlockProperties");

            for (Field field : blockPropertiesClass.getFields()) {
                field.setAccessible(true);
                Object value = field.get(null);
                if (value instanceof IBlockState) {
                    register(fieldNameToName.getOrDefault(field.getName(), ((IBlockState<?>) value).getName()),
                            field.getName(), (IBlockState<?>) value);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            PluginDebugger.debug(ex);
        }

    }

    private static void register(String key, String fieldName, IBlockState<?> blockState) {
        if (nameToBlockState.containsKey(key)) {
            SuperiorSkyblockPlugin.log("&cWarning: block state " + key + "(" + fieldName + ") already exists. Contact Ome_R!");
        } else {
            nameToBlockState.put(key, blockState);
            blockStateToName.put(blockState, key);
        }
    }

    @Nullable
    public static IBlockState<?> getBlockState(String name) {
        return nameToBlockState.get(name);
    }

    @Nullable
    public static String getBlockStateName(IBlockState<?> blockState) {
        return blockStateToName.get(blockState);
    }

}
