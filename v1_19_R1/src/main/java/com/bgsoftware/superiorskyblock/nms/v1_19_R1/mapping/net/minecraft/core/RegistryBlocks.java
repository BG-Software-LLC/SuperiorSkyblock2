package com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.core;

import net.minecraft.core.IRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.Item;

public final class RegistryBlocks {

    public static final net.minecraft.core.RegistryBlocks<Item> ITEM_REGISTRY = IRegistry.Y;

    public static MinecraftKey getKey(net.minecraft.core.RegistryBlocks<Item> registryBlocks, Item item) {
        return registryBlocks.b(item);
    }

}
