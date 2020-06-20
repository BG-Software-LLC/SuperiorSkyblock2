package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.api.key.Key;

import java.math.BigDecimal;

public interface PricesProvider {

    BigDecimal getPrice(Key key);

}
