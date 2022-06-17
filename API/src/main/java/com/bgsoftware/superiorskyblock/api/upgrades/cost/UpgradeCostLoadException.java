package com.bgsoftware.superiorskyblock.api.upgrades.cost;

import org.bukkit.configuration.ConfigurationSection;

/**
 * This exception is used inside {@link UpgradeCostLoader#loadCost(ConfigurationSection)}
 * when a faulty configuration is given for the loader.
 */
public class UpgradeCostLoadException extends Exception {

    public UpgradeCostLoadException(String message) {
        super(message);
    }

}
