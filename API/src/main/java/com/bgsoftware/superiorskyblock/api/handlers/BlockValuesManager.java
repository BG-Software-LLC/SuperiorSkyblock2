package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.key.Key;

import java.math.BigDecimal;

public interface BlockValuesManager {

    /**
     * Get the worth value of a key.
     * @param key The key to check.
     * @return The worth value.
     */
    BigDecimal getBlockWorth(Key key);

    /**
     * Get the level value of a key.
     * @param key The key to check.
     * @return The level value.
     */
    BigDecimal getBlockLevel(Key key);

    /**
     * Get the exact key that is used in the config.
     * @param key The key to check.
     * @return The key from the config.
     */
    Key getBlockKey(Key key);

}
