package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.key.Key;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public interface KeysManager {

    /**
     * Get the key of an entity type.
     * @param entityType The entity type to check.
     */
    Key getKey(EntityType entityType);

    /**
     * Get the key of an entity.
     * @param entity The entity to check.
     */
    Key getKey(Entity entity);

    /**
     * Get the key of a block.
     * @param block The block to check.
     */
    Key getKey(Block block);

    /**
     * Get the key of a block-state.
     * @param blockState The block-state to check.
     */
    Key getKey(BlockState blockState);

    /**
     * Get the key of an item-stack.
     * @param itemStack The item-stack to check.
     */
    Key getKey(ItemStack itemStack);

    /**
     * Get the key of a material and data.
     * @param material The material to check.
     * @param data The data to check.
     */
    Key getKey(Material material, short data);

    /**
     * Get the key of a string.
     * @param key The string to check.
     */
    Key getKey(String key);

}
