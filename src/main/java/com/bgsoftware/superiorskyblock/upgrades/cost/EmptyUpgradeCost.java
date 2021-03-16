package com.bgsoftware.superiorskyblock.upgrades.cost;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.math.BigDecimal;

public final class EmptyUpgradeCost extends UpgradeCostAbstract {

    private static final EmptyUpgradeCost instance = new EmptyUpgradeCost();

    public static EmptyUpgradeCost getInstance() {
        return instance;
    }

    private EmptyUpgradeCost(){
        super(BigDecimal.ZERO);
    }

    @Override
    public boolean hasEnoughBalance(SuperiorPlayer superiorPlayer) {
        return false;
    }

    @Override
    public void withdrawCost(SuperiorPlayer superiorPlayer) {
    }

}
