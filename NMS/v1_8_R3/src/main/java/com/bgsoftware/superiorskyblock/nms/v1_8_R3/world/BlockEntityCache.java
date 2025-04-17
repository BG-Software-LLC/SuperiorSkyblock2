package com.bgsoftware.superiorskyblock.nms.v1_8_R3.world;

import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.IContainer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.TileEntity;
import net.minecraft.server.v1_8_R3.World;

import java.util.IdentityHashMap;
import java.util.Map;

public class BlockEntityCache {

    private static final Map<Block, String> BLOCK_TO_ID = new IdentityHashMap<>();

    private BlockEntityCache() {

    }

    public static String getTileEntityId(IBlockData blockData) {
        return BLOCK_TO_ID.computeIfAbsent(blockData.getBlock(), block -> {
            if (block instanceof IContainer) {
                World world = MinecraftServer.getServer().getWorld();
                TileEntity tileEntity = ((IContainer) block).a(world, block.toLegacyData(blockData));
                if (tileEntity != null) {
                    NBTTagCompound nbtTagCompound = new NBTTagCompound();
                    tileEntity.b(nbtTagCompound);
                    return nbtTagCompound.getString("id");
                }
            }

            return "";
        });
    }

}
