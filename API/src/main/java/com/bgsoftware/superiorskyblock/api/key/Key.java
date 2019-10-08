package com.bgsoftware.superiorskyblock.api.key;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

/**
 * Used as a wrapper for objects into material & data object.
 */
public final class Key {

    private String key;

    private Key(String key){
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(Object obj) {
        if(obj instanceof Key){
            String key = obj.toString();
            if(this.key.equals(key))
                return true;
            else if(key.contains(":") && this.key.equals(key.split(":")[0]))
                return true;
            else if(key.contains(";") && this.key.equals(key.split(";")[0]))
                return true;
        }
        return false;
    }

    /**
     * Get the key of an entity type.
     * @param entityType The entity type to check.
     */
    public static Key of(EntityType entityType){
        return of(entityType.name());
    }

    /**
     * Get the key of a block..
     * @param block The block to check.
     */
    public static Key of(Block block){
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getGrid().isSpawner(block.getType()) ?
                of(block.getType() + ":" + ((CreatureSpawner) block.getState()).getSpawnedType()) :
                of(block.getState().getData().toItemStack());
    }

    /**
     * Get the key of an item-stack.
     * @param itemStack The item-stack to check.
     */
    public static Key of(ItemStack itemStack){
        return of(itemStack.getType(), itemStack.getDurability());
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
