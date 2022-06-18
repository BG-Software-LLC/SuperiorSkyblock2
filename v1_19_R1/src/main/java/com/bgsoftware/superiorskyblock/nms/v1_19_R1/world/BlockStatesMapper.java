package com.bgsoftware.superiorskyblock.nms.v1_19_R1.world;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.level.block.state.properties.BlockState;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.IBlockState;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public final class BlockStatesMapper {

    private static final Map<String, BlockState<?>> nameToBlockState = new HashMap<>();
    private static final Map<IBlockState<?>, String> blockStateToName = new HashMap<>();

    static {
        Map<String, String> fieldNameToName = new HashMap<>();
        fieldNameToName.put("J", "axis-empty");
        fieldNameToName.put("R", "facing-notup");
        fieldNameToName.put("S", "facing-horizontal");
        fieldNameToName.put("W", "wall-east");
        fieldNameToName.put("X", "wall-north");
        fieldNameToName.put("Y", "wall-south");
        fieldNameToName.put("Z", "wall-west");
        fieldNameToName.put("aa", "redstone-east");
        fieldNameToName.put("ab", "redstone-north");
        fieldNameToName.put("ac", "redstone-south");
        fieldNameToName.put("ad", "redstone-west");
        fieldNameToName.put("ae", "double-half");
        fieldNameToName.put("ag", "track-shape-empty");
        fieldNameToName.put("ah", "track-shape");
        fieldNameToName.put("aq", "age1");
        fieldNameToName.put("ar", "age2");
        fieldNameToName.put("as", "age3");
        fieldNameToName.put("at", "age4");
        fieldNameToName.put("au", "age5");
        fieldNameToName.put("av", "age7");
        fieldNameToName.put("aw", "age15");
        fieldNameToName.put("ax", "age25");
        fieldNameToName.put("aK", "level3");
        fieldNameToName.put("aL", "level8");
        fieldNameToName.put("aM", "level1-8");
        fieldNameToName.put("aP", "level15");
        fieldNameToName.put("aC", "distance1-7");
        fieldNameToName.put("aW", "distance7");
        fieldNameToName.put("bd", "chest-type");
        fieldNameToName.put("be", "comparator-mode");
        fieldNameToName.put("bh", "piston-type");
        fieldNameToName.put("bi", "slab-type");

        try {
            for (Field field : BlockProperties.class.getFields()) {
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
