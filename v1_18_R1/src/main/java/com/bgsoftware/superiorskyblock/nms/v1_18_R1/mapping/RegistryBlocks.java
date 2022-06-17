package com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping;

import net.minecraft.core.IRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.Item;

public class RegistryBlocks {

    public static final net.minecraft.core.RegistryBlocks<Item> ITEM_REGISTRY = IRegistry.aa;

    public static MinecraftKey getKey(net.minecraft.core.RegistryBlocks<Item> registryBlocks, Item item) {
        return registryBlocks.b(item);
    }

}
