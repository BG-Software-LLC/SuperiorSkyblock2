package com.bgsoftware.superiorskyblock.api.upgrades;

import java.math.BigDecimal;

public interface UpgradeCost {

    /**
     * Get the value of the uprade cost
     */
    BigDecimal getValue();

    /**
     * Get the cost provider
     */
    UpgradeCostProvider getProvider();

}
