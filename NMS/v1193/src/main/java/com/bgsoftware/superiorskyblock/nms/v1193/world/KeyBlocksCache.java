package com.bgsoftware.superiorskyblock.nms.v1193.world;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R2.util.CraftMagicNumbers;

import java.util.IdentityHashMap;
import java.util.Map;

public class KeyBlocksCache {

    private static final Map<Block, Key> BLOCK_TO_KEY = new IdentityHashMap<>();

    private KeyBlocksCache() {

    }

    public static Key getBlockKey(Block block) {
        return BLOCK_TO_KEY.computeIfAbsent(block, unused -> {
            Material blockType = CraftMagicNumbers.getMaterial(block);
            return KeyImpl.of(blockType.name() + "", "0");
        });
    }

    public static void cacheAllBlocks() {
        BuiltInRegistries.BLOCK.forEach(KeyBlocksCache::getBlockKey);
    }

}
