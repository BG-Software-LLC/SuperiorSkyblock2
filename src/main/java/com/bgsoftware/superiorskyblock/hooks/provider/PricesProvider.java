package com.bgsoftware.superiorskyblock.hooks.provider;

import com.bgsoftware.superiorskyblock.api.key.Key;

import java.math.BigDecimal;

public interface PricesProvider {

    /**
     * Get price of a block/item.
     * @param key The key of the block or the item.
     * @return The price of that block/item.
     */
    BigDecimal getPrice(Key key);

}
