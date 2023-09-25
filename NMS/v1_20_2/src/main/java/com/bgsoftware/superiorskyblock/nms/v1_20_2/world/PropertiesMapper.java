package com.bgsoftware.superiorskyblock.nms.v1_20_2.world;

import com.bgsoftware.superiorskyblock.core.logging.Log;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class PropertiesMapper {

    private static final Map<String, Property<?>> nameToProperty = new HashMap<>();
    private static final Map<Property<?>, String> propertyToName = new HashMap<>();

    static {
        Map<Object, String> fieldsToNames = new IdentityHashMap<>();

        fieldsToNames.put(BlockStateProperties.AXIS, "axis-empty");
        fieldsToNames.put(BlockStateProperties.FACING_HOPPER, "facing-notup");
        fieldsToNames.put(BlockStateProperties.HORIZONTAL_FACING, "facing-horizontal");
        fieldsToNames.put(BlockStateProperties.EAST_WALL, "wall-east");
        fieldsToNames.put(BlockStateProperties.NORTH_WALL, "wall-north");
        fieldsToNames.put(BlockStateProperties.SOUTH_WALL, "wall-south");
        fieldsToNames.put(BlockStateProperties.WEST_WALL, "wall-west");
        fieldsToNames.put(BlockStateProperties.EAST_REDSTONE, "redstone-east");
        fieldsToNames.put(BlockStateProperties.NORTH_REDSTONE, "redstone-north");
        fieldsToNames.put(BlockStateProperties.SOUTH_REDSTONE, "redstone-south");
        fieldsToNames.put(BlockStateProperties.WEST_REDSTONE, "redstone-west");
        fieldsToNames.put(BlockStateProperties.DOUBLE_BLOCK_HALF, "double-half");
        fieldsToNames.put(BlockStateProperties.RAIL_SHAPE, "track-shape-empty");
        fieldsToNames.put(BlockStateProperties.RAIL_SHAPE_STRAIGHT, "track-shape");
        fieldsToNames.put(BlockStateProperties.AGE_1, "age1");
        fieldsToNames.put(BlockStateProperties.AGE_2, "age2");
        fieldsToNames.put(BlockStateProperties.AGE_3, "age3");
        fieldsToNames.put(BlockStateProperties.AGE_4, "age4");
        fieldsToNames.put(BlockStateProperties.AGE_5, "age5");
        fieldsToNames.put(BlockStateProperties.AGE_7, "age7");
        fieldsToNames.put(BlockStateProperties.AGE_15, "age15");
        fieldsToNames.put(BlockStateProperties.AGE_25, "age25");
        fieldsToNames.put(BlockStateProperties.LEVEL_CAULDRON, "level3");
        fieldsToNames.put(BlockStateProperties.LEVEL_COMPOSTER, "level8");
        fieldsToNames.put(BlockStateProperties.LEVEL_FLOWING, "level1-8");
        fieldsToNames.put(BlockStateProperties.LEVEL, "level15");
        fieldsToNames.put(BlockStateProperties.DISTANCE, "distance1-7");
        fieldsToNames.put(BlockStateProperties.STABILITY_DISTANCE, "distance7");
        fieldsToNames.put(BlockStateProperties.CHEST_TYPE, "chest-type");
        fieldsToNames.put(BlockStateProperties.MODE_COMPARATOR, "comparator-mode");
        fieldsToNames.put(BlockStateProperties.PISTON_TYPE, "piston-type");
        fieldsToNames.put(BlockStateProperties.SLAB_TYPE, "slab-type");

        try {
            for (Field field : BlockStateProperties.class.getFields()) {
                field.setAccessible(true);
                Object value = field.get(null);
                if (value instanceof Property) {
                    Property<?> property = (Property<?>) value;
                    register(fieldsToNames.getOrDefault(field.get(null), property.getName()), field.getName(), property);
                }
            }
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while loading properties mapper:");
        }

    }

    private static void register(String key, String fieldName, Property<?> property) {
        if (nameToProperty.containsKey(key)) {
            Log.error("Block state ", key, "(", fieldName, ") already exists. Contact Ome_R!");
        } else {
            nameToProperty.put(key, property);
            propertyToName.put(property, key);
        }
    }

    public static Property<?> getProperty(String name) {
        return nameToProperty.get(name);
    }

    public static String getPropertyName(Property<?> property) {
        return propertyToName.get(property);
    }

}
