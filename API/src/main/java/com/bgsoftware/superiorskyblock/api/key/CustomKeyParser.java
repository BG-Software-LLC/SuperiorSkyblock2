package com.bgsoftware.superiorskyblock.api.key;

import com.bgsoftware.common.annotations.Nullable;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public interface CustomKeyParser {

    /**
     * Get a custom key for a block.
     * Please note: this method should support async calls.
     *
     * @param location The location of the block.
     */
    @Nullable
    default Key getCustomKey(Location location) {
        return null;
    }

    /**
     * Get a custom key for an entity.
     * Please note: this method should support async calls.
     *
     * @param entity The entity to check.
     */
    @Nullable
    default Key getCustomKey(Entity entity) {
        return null;
    }

    /**
     * Get a custom key for an item-stack.
     * Please note: this method should support async calls.
     *
     * @param itemStack The item-stack to parse.
     * @param def       The original key of the item.
     */
    default Key getCustomKey(ItemStack itemStack, Key def) {
        return def;
    }

    /**
     * Check if a key was created by this parser.
     *
     * @param key The key to check.
     */
    boolean isCustomKey(Key key);

    /**
     * Get the custom item of the custom key.
     * The provided key is guaranteed to pass the {@link #isCustomKey(Key)} check.
     *
     * @param key The key to get the custom item for.
     * @return The item stack for that custom key, or null for using the default one.
     */
    @Nullable
    default ItemStack getCustomKeyItem(Key key) {
        return null;
    }

}
