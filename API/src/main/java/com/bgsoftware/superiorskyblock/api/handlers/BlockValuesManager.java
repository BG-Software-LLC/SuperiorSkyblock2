package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.key.Key;

import java.math.BigDecimal;

public interface BlockValuesManager {

    BigDecimal getBlockWorth(Key key);

    BigDecimal getBlockLevel(Key key);

    Key getBlockKey(Key key);

}
