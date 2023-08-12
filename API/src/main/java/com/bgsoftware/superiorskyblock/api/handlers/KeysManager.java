package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public interface KeysManager {

    /**
     * Get the key of an entity type.
     *
     * @param entityType The entity type to check.
     */
    Key getKey(EntityType entityType);

    /**
     * Get the key of an entity type.
     *
     * @param entityTypeName The name of the entity type to create key for.
     */
    Key getEntityTypeKey(String entityTypeName);

    /**
     * Get the key of an entity.
     *
     * @param entity The entity to check.
     */
    Key getKey(Entity entity);

    /**
     * Get the key of a block.
     *
     * @param block The block to check.
     */
    Key getKey(Block block);

    /**
     * Get the key of a block-state.
     *
     * @param blockState The block-state to check.
     */
    Key getKey(BlockState blockState);

    /**
     * Get the key of an item-stack.
     *
     * @param itemStack The item-stack to check.
     */
    Key getKey(ItemStack itemStack);

    /**
     * Get the key of a material and data.
     *
     * @param material The material to check.
     * @param data     The data to check.
     */
    Key getKey(Material material, short data);

    /**
     * Get the key of a material.
     *
     * @param material The material to create key for.
     */
    Key getKey(Material material);

    /**
     * Get the key of a material and data, split by ':' (optionally).
     *
     * @param type The combined material-data pair to create key for.
     */
    Key getMaterialAndDataKey(String type);

    /**
     * Get the key of a spawner block with specific entity type.
     *
     * @param entityType The entity type of the spawner to create key for.
     */
    Key getSpawnerKey(EntityType entityType);

    /**
     * Get the key of a spawner block with specific entity type.
     *
     * @param entityTypeName The name of the entity type of the spawner to create key for.
     */
    Key getSpawnerKey(String entityTypeName);

    /**
     * Get the key of a string.
     *
     * @param key The string to check.
     */
    Key getKey(String key);

    /**
     * Get the key of a global-key and a sub-key.
     *
     * @param globalKey The global key
     * @param subKey    The sub key
     */
    Key getKey(String globalKey, String subKey);

    /**
     * Create a new empty {@link KeySet} instance.
     */
    KeySet createKeySet(Supplier<Set<String>> setCreator);

    /**
     * Create a new {@link KeySet} instance from the given collection.
     * If the provided collection is also a {@link KeySet}, the exact same instance of that set is returned.
     * Otherwise, the returned {@link KeySet} is a copy of that collection.
     *
     * @param collection The collection to create {@link KeySet} from.
     */
    KeySet createKeySet(Supplier<Set<String>> setCreator, Collection<Key> collection);

    /**
     * Create a new empty {@link KeyMap <V>} instance.
     */
    <V> KeyMap<V> createKeyMap(Supplier<Map<String, V>> mapCreator);

    /**
     * Create a new {@link KeyMap<V>} instance from the given map.
     * If the provided map is also a {@link KeyMap<V>}, the exact same instance of the map is returned.
     * Otherwise, the returned {@link KeyMap<V>} is a copy of that map.
     *
     * @param map The map to create {@link KeySet} from.
     */
    <V> KeyMap<V> createKeyMap(Supplier<Map<String, V>> mapCreator, Map<Key, V> map);

}
