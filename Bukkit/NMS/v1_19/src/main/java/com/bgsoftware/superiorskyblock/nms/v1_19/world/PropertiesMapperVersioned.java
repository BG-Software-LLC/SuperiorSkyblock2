package com.bgsoftware.superiorskyblock.nms.v1_19.world;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Map;

public class PropertiesMapperVersioned {

    public static void initializeFields(Map<Object, String> fieldsToNames) {
        fieldsToNames.put(BlockStateProperties.AGE_4, "age4");
    }

    private PropertiesMapperVersioned() {

    }

}
