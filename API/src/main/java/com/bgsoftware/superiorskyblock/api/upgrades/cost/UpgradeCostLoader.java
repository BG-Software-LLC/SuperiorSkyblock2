package com.bgsoftware.superiorskyblock.api.upgrades.cost;

import org.bukkit.configuration.ConfigurationSection;

public interface UpgradeCostLoader {

    /**
     * Load a cost from a configuration section.
     *
     * @param upgradeSection The section to load the cost from.
     * @throws UpgradeCostLoadException when an issue occurred when loading the cost.
     */
    UpgradeCost loadCost(ConfigurationSection upgradeSection) throws UpgradeCostLoadException;

}
