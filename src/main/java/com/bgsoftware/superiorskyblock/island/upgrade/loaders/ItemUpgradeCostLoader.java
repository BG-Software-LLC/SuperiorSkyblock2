package com.bgsoftware.superiorskyblock.island.upgrade.loaders;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoadException;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoader;
import com.bgsoftware.superiorskyblock.island.upgrade.cost.ItemUpgradeCost;
import org.bukkit.configuration.ConfigurationSection;

import java.math.BigDecimal;
import java.util.Locale;

public class ItemUpgradeCostLoader implements UpgradeCostLoader {

    @Override
    public UpgradeCost loadCost(ConfigurationSection upgradeSection) throws UpgradeCostLoadException {
        if (!upgradeSection.contains("amount")) {
            throw new UpgradeCostLoadException("The field 'amount' is missing from the section.");
        }
        if (!upgradeSection.contains("types")) {
            throw new UpgradeCostLoadException("The field 'types' is missing from the section.");
        }

        KeySet keySet = KeySet.createKeySet();
        for (String type : upgradeSection.getStringList("types")) {
            keySet.add(Key.ofMaterialAndData(type.toUpperCase(Locale.ENGLISH)));
        }

        if (keySet.isEmpty()) {
            throw new UpgradeCostLoadException("The field 'types' cannot be empty.");
        }

        return new ItemUpgradeCost(
                BigDecimal.valueOf(upgradeSection.getDouble("amount")),
                keySet
        );
    }

}
