package com.bgsoftware.superiorskyblock.nms.v1_15_R1.world;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import net.minecraft.server.v1_15_R1.BlockProperties;
import net.minecraft.server.v1_15_R1.IBlockState;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public final class BlockStatesMapper {

    private static final Map<String, IBlockState<?>> nameToBlockState = new HashMap<>();
    private static final Map<IBlockState<?>, String> blockStateToName = new HashMap<>();

    static {
        Map<String, String> fieldNameToName = new HashMap<>();
        fieldNameToName.put("E", "axis-empty");
        fieldNameToName.put("M", "facing-notup");
        fieldNameToName.put("N", "facing-horizontal");
        fieldNameToName.put("Q", "redstone-east");
        fieldNameToName.put("R", "redstone-north");
        fieldNameToName.put("S", "redstone-south");
        fieldNameToName.put("T", "redstone-west");
        fieldNameToName.put("U", "double-half");
        fieldNameToName.put("W", "track-shape-empty");
        fieldNameToName.put("X", "track-shape");
        fieldNameToName.put("Y", "age1");
        fieldNameToName.put("Z", "age2");
        fieldNameToName.put("aa", "age3");
        fieldNameToName.put("ab", "age5");
        fieldNameToName.put("ac", "age7");
        fieldNameToName.put("ad", "age15");
        fieldNameToName.put("ae", "age25");
        fieldNameToName.put("al", "level3");
        fieldNameToName.put("am", "level8");
        fieldNameToName.put("an", "level1-8");
        fieldNameToName.put("ap", "level15");
        fieldNameToName.put("ah", "distance1-7");
        fieldNameToName.put("av", "distance7");
        fieldNameToName.put("ay", "chest-type");
        fieldNameToName.put("az", "comparator-mode");
        fieldNameToName.put("aC", "piston-type");
        fieldNameToName.put("aD", "slab-type");

        try {
            for (Field field : BlockProperties.class.getFields()) {
                Object value = field.get(null);
                if (value instanceof IBlockState) {
                    register(fieldNameToName.getOrDefault(field.getName(), ((IBlockState<?>) value).a()),
                            field.getName(), (IBlockState<?>) value);
                }
            }
        } catch (Exception ignored) {
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
