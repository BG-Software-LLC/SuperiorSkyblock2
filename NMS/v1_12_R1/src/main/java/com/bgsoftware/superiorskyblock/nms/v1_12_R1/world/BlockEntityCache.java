package com.bgsoftware.superiorskyblock.nms.v1_12_R1.world;

import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.ITileEntity;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.TileEntity;
import net.minecraft.server.v1_12_R1.World;

import java.util.IdentityHashMap;
import java.util.Map;

public class BlockEntityCache {

    private static final Map<Block, String> BLOCK_TO_ID = new IdentityHashMap<>();

    private BlockEntityCache() {

    }

    public static String getTileEntityId(IBlockData blockData) {
        return BLOCK_TO_ID.computeIfAbsent(blockData.getBlock(), block -> {
            if (block instanceof ITileEntity) {
                World world = MinecraftServer.getServer().getWorld();
                TileEntity tileEntity = ((ITileEntity) block).a(world, block.toLegacyData(blockData));
                if (tileEntity != null)
                    return tileEntity.getMinecraftKeyString();
            }

            return "";
        });
    }

}
