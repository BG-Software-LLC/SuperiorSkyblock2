package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.CustomKeyParser;

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

    /**
     * Register a value for a key.
     * @param key The key to set custom value of.
     * @param worthValue The custom worth value of the key. May be null.
     * @param levelValue The custom level value of the key. May be null.
     */
    void registerCustomKey(Key key, BigDecimal worthValue, BigDecimal levelValue);

    /**
     * Register a custom key parser.
     * @param customKeyParser The custom key parser.
     * @param blockTypes All the block types you want to check.
     */
    void registerKeyParser(CustomKeyParser customKeyParser, Key... blockTypes);

}
