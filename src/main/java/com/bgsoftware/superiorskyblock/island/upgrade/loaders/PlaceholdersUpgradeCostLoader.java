package com.bgsoftware.superiorskyblock.island.upgrade.loaders;

import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoadException;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoader;
import com.bgsoftware.superiorskyblock.island.upgrade.cost.PlaceholdersUpgradeCost;
import org.bukkit.configuration.ConfigurationSection;

import java.math.BigDecimal;

public class PlaceholdersUpgradeCostLoader implements UpgradeCostLoader {

    @Override
    public UpgradeCost loadCost(ConfigurationSection upgradeSection) throws UpgradeCostLoadException {
        if (!upgradeSection.contains("price"))
            throw new UpgradeCostLoadException("The field 'price' is missing from the section.");
        if (!upgradeSection.contains("placeholder"))
            throw new UpgradeCostLoadException("The field 'placeholder' is missing from the section.");
        if (!upgradeSection.contains("withdraw-commands"))
            throw new UpgradeCostLoadException("The field 'withdraw-commands' is missing from the section.");

        return new PlaceholdersUpgradeCost(
                BigDecimal.valueOf(upgradeSection.getDouble("price")),
                upgradeSection.getString("placeholder"),
                upgradeSection.getStringList("withdraw-commands")
        );
    }

}
