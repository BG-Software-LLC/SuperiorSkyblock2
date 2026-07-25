package com.bgsoftware.superiorskyblock.nms.v1_16_R3.world;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.IRegistry;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;

import java.util.IdentityHashMap;
import java.util.Map;

public class KeyBlocksCache {

    private static final Map<Block, Key> BLOCK_TO_KEY = new IdentityHashMap<>();

    private KeyBlocksCache() {

    }

    public static Key getBlockKey(Block block) {
        return BLOCK_TO_KEY.computeIfAbsent(block, unused -> {
            Material blockType = CraftMagicNumbers.getMaterial(block);
            return blockType == null ? null : blockType.isItem() ? Keys.of(blockType, (short) 0) : Keys.of(blockType);
        });
    }

    public static void cacheAllBlocks() {
        IRegistry.BLOCK.forEach(KeyBlocksCache::getBlockKey);
    }

}
