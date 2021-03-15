package com.bgsoftware.superiorskyblock.api.upgrades;

import java.math.BigDecimal;

public interface UpgradeCost {

    /**
     * Get the value of the upgrade cost
     */
    BigDecimal getValue();

    /**
     * Get the cost provider
     */
    UpgradeCostProvider getProvider();

    /**
     * Create a copy of this cost with a new value
     */
    UpgradeCost copyOfValue(BigDecimal value);

}
