package com.bgsoftware.superiorskyblock.nms.v1_18_R1.world;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.world.level.block.state.properties.BlockState;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.IBlockState;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public final class BlockStatesMapper {

    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "AXIS", type = Remap.Type.FIELD, remappedName = "G")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "FACING_HOPPER", type = Remap.Type.FIELD, remappedName = "O")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "HORIZONTAL_FACING", type = Remap.Type.FIELD, remappedName = "P")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "EAST_WALL", type = Remap.Type.FIELD, remappedName = "T")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "NORTH_WALL", type = Remap.Type.FIELD, remappedName = "U")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "SOUTH_WALL", type = Remap.Type.FIELD, remappedName = "V")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "WEST_WALL", type = Remap.Type.FIELD, remappedName = "W")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "EAST_REDSTONE", type = Remap.Type.FIELD, remappedName = "X")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "NORTH_REDSTONE", type = Remap.Type.FIELD, remappedName = "Y")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "SOUTH_REDSTONE", type = Remap.Type.FIELD, remappedName = "Z")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "WEST_REDSTONE", type = Remap.Type.FIELD, remappedName = "aa")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "DOUBLE_BLOCK_HALF", type = Remap.Type.FIELD, remappedName = "ab")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "RAIL_SHAPE", type = Remap.Type.FIELD, remappedName = "ad")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "RAIL_SHAPE_STRAIGHT", type = Remap.Type.FIELD, remappedName = "ae")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "AGE_1", type = Remap.Type.FIELD, remappedName = "am")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "AGE_2", type = Remap.Type.FIELD, remappedName = "an")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "AGE_3", type = Remap.Type.FIELD, remappedName = "ao")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "AGE_5", type = Remap.Type.FIELD, remappedName = "ap")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "AGE_7", type = Remap.Type.FIELD, remappedName = "aq")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "AGE_15", type = Remap.Type.FIELD, remappedName = "ar")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "AGE_25", type = Remap.Type.FIELD, remappedName = "as")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "LEVEL_CAULDRON", type = Remap.Type.FIELD, remappedName = "aF")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "LEVEL_COMPOSTER", type = Remap.Type.FIELD, remappedName = "aG")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "LEVEL_FLOWING", type = Remap.Type.FIELD, remappedName = "aH")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "LEVEL", type = Remap.Type.FIELD, remappedName = "aK")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "DISTANCE", type = Remap.Type.FIELD, remappedName = "ax")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "STABILITY_DISTANCE", type = Remap.Type.FIELD, remappedName = "aR")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "CHEST_TYPE", type = Remap.Type.FIELD, remappedName = "aY")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "MODE_COMPARATOR", type = Remap.Type.FIELD, remappedName = "aZ")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "PISTON_TYPE", type = Remap.Type.FIELD, remappedName = "bc")
    @Remap(classPath = "net.minecraft.world.level.block.state.properties.BlockStateProperties", name = "SLAB_TYPE", type = Remap.Type.FIELD, remappedName = "bd")
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
