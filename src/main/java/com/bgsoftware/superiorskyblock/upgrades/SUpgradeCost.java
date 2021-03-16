package com.bgsoftware.superiorskyblock.upgrades;

import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeCostProvider;

import java.math.BigDecimal;

public class SUpgradeCost implements UpgradeCost {

    private final BigDecimal value;
    private final UpgradeCostProvider provider;

    public SUpgradeCost(BigDecimal value, UpgradeCostProvider provider) {
        this.value = value;
        this.provider = provider;
    }

    @Override
    public BigDecimal getValue() {
        return value;
    }

    @Override
    public UpgradeCostProvider getProvider() {
        return provider;
    }

}
