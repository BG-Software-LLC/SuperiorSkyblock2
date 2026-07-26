package com.bgsoftware.superiorskyblock.island.upgrade.cost;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.service.economy.EconomyService;

import java.math.BigDecimal;

public class VaultUpgradeCost extends UpgradeCostAbstract {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final LazyReference<EconomyService> economyService = new LazyReference<EconomyService>() {
        @Override
        protected EconomyService create() {
            return plugin.getServices().getService(EconomyService.class);
        }
    };

    public VaultUpgradeCost(BigDecimal value) {
        super(value, "money");
    }

    @Override
    public boolean hasEnoughBalance(SuperiorPlayer superiorPlayer) {
        return economyService.get().getBalance(superiorPlayer).compareTo(cost) >= 0;
    }

    @Override
    public void withdrawCost(SuperiorPlayer superiorPlayer) {
        economyService.get().withdrawMoney(superiorPlayer, cost);
    }

    @Override
    public UpgradeCost clone(BigDecimal cost) {
        return new VaultUpgradeCost(cost);
    }

}

