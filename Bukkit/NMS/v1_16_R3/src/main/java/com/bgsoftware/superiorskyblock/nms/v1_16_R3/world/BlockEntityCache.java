package com.bgsoftware.superiorskyblock.nms.v1_16_R3.world;

import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.TileEntityTypes;

import java.util.IdentityHashMap;
import java.util.Map;

public class BlockEntityCache {

    private static final Map<Block, String> BLOCK_TO_ID = new IdentityHashMap<>();

    private BlockEntityCache() {

    }

    public static String getTileEntityId(IBlockData blockData) {
        return BLOCK_TO_ID.computeIfAbsent(blockData.getBlock(), block -> {
            for (TileEntityTypes<?> tileEntityTypes : IRegistry.BLOCK_ENTITY_TYPE) {
                if (tileEntityTypes.isValidBlock(block)) {
                    MinecraftKey minecraftKey = TileEntityTypes.a(tileEntityTypes);
                    if (minecraftKey != null)
                        return minecraftKey.getKey();
                }
            }

            return "";
        });
    }

}
