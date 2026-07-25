package com.bgsoftware.superiorskyblock.island.upgrade.cost;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.math.BigDecimal;

public class VaultUpgradeCost extends UpgradeCostAbstract {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public VaultUpgradeCost(BigDecimal value) {
        super(value, "money");
    }

    @Override
    public boolean hasEnoughBalance(SuperiorPlayer superiorPlayer) {
        return plugin.getProviders().getEconomyProvider().getBalance(superiorPlayer).compareTo(cost) >= 0;
    }

    @Override
    public void withdrawCost(SuperiorPlayer superiorPlayer) {
        plugin.getProviders().withdrawMoney(superiorPlayer, cost);
    }

    @Override
    public UpgradeCost clone(BigDecimal cost) {
        return new VaultUpgradeCost(cost);
    }

}

