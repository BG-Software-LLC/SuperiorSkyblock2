package com.bgsoftware.superiorskyblock.island.upgrade.loaders;

import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoadException;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoader;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.set.KeySets;
import com.bgsoftware.superiorskyblock.island.upgrade.cost.ItemsUpgradeCost;
import org.bukkit.configuration.ConfigurationSection;

import java.math.BigDecimal;

public class ItemsUpgradeCostLoader implements UpgradeCostLoader {

    @Override
    public UpgradeCost loadCost(ConfigurationSection upgradeSection) throws UpgradeCostLoadException {
        if (!upgradeSection.contains("amount")) {
            throw new UpgradeCostLoadException("The field 'amount' is missing from the section.");
        }
        if (!upgradeSection.contains("types")) {
            throw new UpgradeCostLoadException("The field 'types' is missing from the section.");
        }

        KeySet keySet = KeySets.createHashSet(KeyIndicator.MATERIAL, upgradeSection.getStringList("types"));

        if (keySet.isEmpty()) {
            throw new UpgradeCostLoadException("The field 'types' cannot be empty.");
        }

        return new ItemsUpgradeCost(
                BigDecimal.valueOf(upgradeSection.getDouble("amount")),
                keySet
        );
    }

}
