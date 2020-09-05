package com.bgsoftware.superiorskyblock.api.key;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public interface CustomKeyParser {

    /**
     * Get a custom key for a block.
     * Please note: this method should support async calls.
     * @param location The location of the block.
     */
    Key getCustomKey(Location location);

    /**
     * Get a custom key for an item-stack.
     * Please note: this method should support async calls.
     * @param itemStack The item-stack to parse.
     * @param def The original key of the item.
     */
    default Key getCustomKey(ItemStack itemStack, Key def){
        return def;
    }

    /**
     * Check if a key was created by this parser.
     * @param key The key to check.
     */
    boolean isCustomKey(Key key);

}
