package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.key.Key;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public interface KeysManager {

    @Deprecated
    boolean isSpawner(Material type);

    @Deprecated
    Key getSpawnerKey(Block block);

    @Deprecated
    Key getSpawnerKey(BlockState blockState);

    @Deprecated
    Key getSpawnerKey(ItemStack itemStack);

    Key getKey(EntityType entityType);

    Key getKey(Block block);

    Key getKey(BlockState blockState);

    Key getKey(ItemStack itemStack);

    Key getKey(Material material, short data);

    Key getKey(String key);

}
