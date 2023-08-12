package com.bgsoftware.superiorskyblock.api.key;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
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
     * Get the key of an entity type.
     *
     * @param entityType The entity type to create key for.
     */
    static Key of(EntityType entityType) {
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().getKey(entityType);
    }

    /**
     * Get the key of an entity type.
     *
     * @param entityTypeName The name of the entity type to create key for.
     */
    static Key ofEntityType(String entityTypeName) {
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().getEntityTypeKey(entityTypeName);
    }

    /**
     * Get the key of an entity.
     *
     * @param entity The entity to check.
     */
    static Key of(Entity entity) {
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().getKey(entity);
    }

    /**
     * Get the key of a block.
     *
     * @param block The block to create key for.
     */
    static Key of(Block block) {
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().getKey(block);
    }

    /**
     * Get the key of a block-state.
     *
     * @param blockState The block-state to create key for.
     */
    static Key of(BlockState blockState) {
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().getKey(blockState);
    }

    /**
     * Get the key of an item-stack.
     *
     * @param itemStack The item-stack to create key for.
     */
    static Key of(ItemStack itemStack) {
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().getKey(itemStack);
    }

    /**
     * Get the key of a material and data.
     *
     * @param material The material to create key for.
     * @param data     The data to create key for.
     */
    static Key of(Material material, short data) {
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().getKey(material, data);
    }

    /**
     * Get the key of a material.
     *
     * @param material The material to create key for.
     */
    static Key of(Material material) {
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().getKey(material);
    }

    /**
     * Get the key of a material and data.
     *
     * @param type The combined material-data pair to create key for.
     */
    static Key ofMaterialAndData(String type) {
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().getMaterialAndDataKey(type);
    }

    /**
     * Get the key of a spawner block with specific entity type.
     *
     * @param entityType The entity type of the spawner to create key for.
     */
    static Key ofSpawner(EntityType entityType) {
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().getSpawnerKey(entityType);
    }

    /**
     * Get the key of a spawner block with specific entity type.
     *
     * @param entityTypeName The name of the entity type of the spawner to create key for.
     */
    static Key ofSpawner(String entityTypeName) {
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().getSpawnerKey(entityTypeName);
    }

    /**
     * Get the key of a global-key and a sub-key.
     * It is recommended to use the other Key#of methods whenever possible, and only use this one
     * for custom keys that has no Key#of methods.
     *
     * @param globalKey The global key
     * @param subKey    The sub key
     */
    static Key of(String globalKey, String subKey) {
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().getKey(globalKey, subKey);
    }

    /**
     * Get the key of a string.
     * It is recommended to use the other Key#of methods whenever possible, and only use this one
     * for custom keys that has no Key#of methods.
     *
     * @param key The string to check.
     */
    static Key of(String key) {
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().getKey(key);
    }

    /**
     * Get the global key of this key instance.
     */
    String getGlobalKey();

    /**
     * Get the sub key of this key instance.
     */
    String getSubKey();

}
