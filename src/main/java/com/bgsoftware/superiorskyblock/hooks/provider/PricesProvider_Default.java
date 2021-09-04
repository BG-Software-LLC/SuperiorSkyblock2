package com.bgsoftware.superiorskyblock.hooks.provider;

import com.bgsoftware.superiorskyblock.key.Key;

import java.math.BigDecimal;

public final class PricesProvider_Default implements PricesProvider {

    private final BigDecimal INVALID_WORTH = BigDecimal.valueOf(-1);

    @Override
    public BigDecimal getPrice(com.bgsoftware.superiorskyblock.api.key.Key key) {
        return INVALID_WORTH;
    }

    @Override
    public Key getBlockKey(Key blockKey) {
        return blockKey;
    }

}
