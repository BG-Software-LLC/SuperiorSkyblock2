package com.bgsoftware.superiorskyblock.api.key;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

/**
 * Used as a wrapper for objects into material & data object.
 */
public interface Key extends Comparable<Key> {

    /**
     * Get the global key of this key instance.
     */
    String getGlobalKey();

    /**
     * Get the sub key of this key instance.
     */
    String getSubKey();

    /**
     * Get the key of an entity type.
     * @param entityType The entity type to check.
     */
    static Key of(EntityType entityType){
        Preconditions.checkNotNull(entityType, "entityType parameter cannot be null.");
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().getKey(entityType);
    }

    /**
     * Get the key of an entity.
     * @param entity The entity to check.
     */
    static Key of(Entity entity){
        Preconditions.checkNotNull(entity, "entity parameter cannot be null.");
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().getKey(entity);
    }

    /**
     * Get the key of a block.
     * @param block The block to check.
     */
    static Key of(Block block){
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().getKey(block);
    }

    /**
     * Get the key of a block-state.
     * @param blockState The block-state to check.
     */
    static Key of(BlockState blockState){
        Preconditions.checkNotNull(blockState, "blockState parameter cannot be null.");
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().getKey(blockState);
    }

    /**
     * Get the key of an item-stack.
     * @param itemStack The item-stack to check.
     */
    static Key of(ItemStack itemStack){
        Preconditions.checkNotNull(itemStack, "itemStack parameter cannot be null.");
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().getKey(itemStack);
    }

    /**
     * Get the key of a material and data.
     * @param material The material to check.
     * @param data The data to check.
     */
    static Key of(Material material, short data){
        Preconditions.checkNotNull(material, "material parameter cannot be null.");
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().getKey(material, data);
    }

    /**
     * Get the key of a string.
     * @param key The string to check.
     */
    static Key of(String key){
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().getKey(key);
    }

}
