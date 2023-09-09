package com.bgsoftware.superiorskyblock.api.hooks;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.key.Key;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public interface PricesProvider {

    /**
     * Get price of a block/item.
     *
     * @param key The key of the block or the item.
     * @return The price of that block/item.
     */
    BigDecimal getPrice(Key key);

    /**
     * Get the correct block-key for a price.
     * Mostly used for legacy-versions where data values of blocks can be ignored.
     *
     * @param blockKey The original block-key.
     * @return The correct-block key for a price.
     */
    @Nullable
    Key getBlockKey(Key blockKey);

    /**
     * Get a CompletableFuture that is completed when all prices and data of this provider are ready.
     */
    default CompletableFuture<Void> getWhenPricesAreReady() {
        return CompletableFuture.completedFuture(null);
    }

}
