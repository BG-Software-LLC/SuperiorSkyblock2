package com.bgsoftware.superiorskyblock.api.key;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.handlers.KeysManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

/**
 * Used as a wrapper for objects into material & data object.
 */
public final class Key implements Comparable<Key> {

    private final String globalKey;
    private final String subKey;

    private Key(String key){
        String[] keySections = key.replace(";", ":").split(":");
        this.globalKey = keySections[0];
        this.subKey = keySections.length == 2 ? keySections[1] : "";
    }

    public String getGlobalKey() {
        return globalKey;
    }

    public String getSubKey() {
        return subKey;
    }

    @Override
    public String toString() {
        return subKey.isEmpty() ? globalKey : globalKey + ":" + subKey;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(Object obj) {
        return obj instanceof Key && toString().equals(obj.toString());
    }

    @Override
    public int compareTo(Key o) {
        return toString().compareTo(o.toString());
    }

    /**
     * Get the key of an entity type.
     * @param entityType The entity type to check.
     */
    public static Key of(EntityType entityType){
        return of(entityType.name());
    }

    /**
     * Get the key of a block.
     * @param block The block to check.
     */
    public static Key of(Block block){
        return of(block.getState());
    }

    /**
     * Get the key of a block-state.
     * @param blockState The block-state to check.
     */
    public static Key of(BlockState blockState){
        KeysManager keysManager = SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys();
        return keysManager.isSpawner(blockState.getType()) ? keysManager.getSpawnerKey(blockState) : of(blockState.getType(), blockState.getRawData());
    }

    /**
     * Get the key of an item-stack.
     * @param itemStack The item-stack to check.
     */
    public static Key of(ItemStack itemStack){
        KeysManager keysManager = SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys();
        return keysManager.isSpawner(itemStack.getType()) ? keysManager.getSpawnerKey(itemStack) : of(itemStack.getType(), itemStack.getDurability());
    }

    /**
     * Get the key of a material and data.
     * @param material The material to check.
     * @param data The data to check.
     */
    public static Key of(Material material, short data){
        return of(material + ":" + data);
    }

    /**
     * Get the key of a string.
     * @param key The string to check.
     */
    public static Key of(String key){
        return new Key(key.replace("LEGACY_", ""));
    }

}
