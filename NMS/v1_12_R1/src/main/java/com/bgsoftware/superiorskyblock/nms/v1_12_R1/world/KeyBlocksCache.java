package com.bgsoftware.superiorskyblock.nms.v1_12_R1.world;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.IBlockData;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;

import java.util.IdentityHashMap;
import java.util.Map;

public class KeyBlocksCache {

    private static final Map<IBlockData, Key> BLOCK_TO_KEY = new IdentityHashMap<>();

    private KeyBlocksCache() {

    }

    public static Key getBlockKey(IBlockData blockData) {
        return BLOCK_TO_KEY.computeIfAbsent(blockData, unused -> {
            Block block = blockData.getBlock();
            Material blockType = CraftMagicNumbers.getMaterial(block);
            if (blockType == null)
                return null;
            byte data = (byte) block.toLegacyData(blockData);
            return Keys.of(blockType, data);
        });
    }

    public static void cacheAllBlocks() {
        Block.REGISTRY_ID.forEach(KeyBlocksCache::getBlockKey);
    }

}
