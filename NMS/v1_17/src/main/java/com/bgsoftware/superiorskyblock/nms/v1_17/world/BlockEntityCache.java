package com.bgsoftware.superiorskyblock.nms.v1_17.world;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.IdentityHashMap;
import java.util.Map;

public class BlockEntityCache {

    private static final Map<Block, String> BLOCK_TO_ID = new IdentityHashMap<>();

    private BlockEntityCache() {

    }

    public static String getBlockEntityId(BlockState blockState) {
        return BLOCK_TO_ID.computeIfAbsent(blockState.getBlock(), block -> {
            for (BlockEntityType<?> blockEntityType : Registry.BLOCK_ENTITY_TYPE) {
                if (blockEntityType.isValid(blockState)) {
                    ResourceLocation resourceLocation = BlockEntityType.getKey(blockEntityType);
                    if (resourceLocation != null)
                        return resourceLocation.toString();
                }
            }

            return "";
        });
    }

}
