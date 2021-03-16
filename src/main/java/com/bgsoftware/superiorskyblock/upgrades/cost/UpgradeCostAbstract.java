package com.bgsoftware.superiorskyblock.upgrades.cost;

import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;

import java.math.BigDecimal;

public abstract class UpgradeCostAbstract implements UpgradeCost {

    protected final BigDecimal cost;

    protected UpgradeCostAbstract(BigDecimal cost){
        this.cost = cost;
    }

    @Override
    public BigDecimal getCost() {
        return cost;
    }

}
