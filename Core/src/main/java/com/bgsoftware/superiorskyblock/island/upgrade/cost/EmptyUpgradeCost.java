package com.bgsoftware.superiorskyblock.island.upgrade.cost;

import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.math.BigDecimal;

public class EmptyUpgradeCost extends UpgradeCostAbstract {

    private static final EmptyUpgradeCost INSTANCE = new EmptyUpgradeCost();

    private EmptyUpgradeCost() {
        super(BigDecimal.ZERO, "Null");
    }

    public static EmptyUpgradeCost getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean hasEnoughBalance(SuperiorPlayer superiorPlayer) {
        return false;
    }

    @Override
    public void withdrawCost(SuperiorPlayer superiorPlayer) {
        // Do nothing.
    }

    @Override
    public UpgradeCost clone(BigDecimal cost) {
        return this;
    }

}
