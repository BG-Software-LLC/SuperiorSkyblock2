package com.bgsoftware.superiorskyblock.api.key;

import org.bukkit.Location;

public interface CustomKeyParser {

    /**
     * Get a custom key for a block.
     * Please note: this method should support async calls.
     * @param location The location of the block.
     */
    Key getCustomKey(Location location);

    /**
     * Check if a key was created by this parser.
     * @param key The key to check.
     */
    boolean isCustomKey(Key key);

}
