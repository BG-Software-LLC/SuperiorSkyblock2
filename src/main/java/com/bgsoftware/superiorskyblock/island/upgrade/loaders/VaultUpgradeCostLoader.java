package com.bgsoftware.superiorskyblock.island.upgrade.loaders;

import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoadException;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoader;
import com.bgsoftware.superiorskyblock.island.upgrade.cost.VaultUpgradeCost;
import org.bukkit.configuration.ConfigurationSection;

import java.math.BigDecimal;

public class VaultUpgradeCostLoader implements UpgradeCostLoader {

    @Override
    public UpgradeCost loadCost(ConfigurationSection upgradeSection) throws UpgradeCostLoadException {
        if (!upgradeSection.contains("price"))
            throw new UpgradeCostLoadException("The field 'price' is missing from the section.");
        return new VaultUpgradeCost(BigDecimal.valueOf(upgradeSection.getDouble("price")));
    }

}
