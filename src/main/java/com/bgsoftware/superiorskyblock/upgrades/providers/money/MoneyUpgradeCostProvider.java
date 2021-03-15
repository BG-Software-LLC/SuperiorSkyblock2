package com.bgsoftware.superiorskyblock.upgrades.providers.money;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeCostProvider;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.upgrades.SUpgradeCost;
import org.bukkit.configuration.ConfigurationSection;

import java.math.BigDecimal;

public class MoneyUpgradeCostProvider implements UpgradeCostProvider {
    @Override
    public String getName() {
        return "money";
    }

    @Override
    public BigDecimal getBalance(SuperiorPlayer superiorPlayer, UpgradeCost upgradeCost) {
        return SuperiorSkyblockPlugin.getPlugin().getProviders().getBalance(superiorPlayer);
    }

    @Override
    public void take(SuperiorPlayer superiorPlayer, UpgradeCost upgradeCost) {
        SuperiorSkyblockPlugin.getPlugin().getProviders().depositMoney(superiorPlayer, upgradeCost.getValue());
    }

    @Override
    public Pair<UpgradeCost, String> createCost(ConfigurationSection configurationSection) {
        if (!configurationSection.contains("price"))
            return new Pair<>(null, "Missing price value");

        return new Pair<>(
                new SUpgradeCost(
                        new BigDecimal(configurationSection.get("price").toString()),
                        this
                ),
                null
        );
    }
}
