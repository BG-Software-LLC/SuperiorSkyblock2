package com.bgsoftware.superiorskyblock.island.upgrade.cost;

import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;

import java.math.BigDecimal;

public abstract class UpgradeCostAbstract implements UpgradeCost {

    protected final BigDecimal cost;
    protected final String id;

    protected UpgradeCostAbstract(BigDecimal cost, String id) {
        this.cost = cost;
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public BigDecimal getCost() {
        return cost;
    }

}
