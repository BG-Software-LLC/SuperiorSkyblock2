package com.bgsoftware.superiorskyblock.upgrades.providers.placeholder;

import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeCostProvider;
import com.bgsoftware.superiorskyblock.upgrades.SUpgradeCost;

import java.math.BigDecimal;

public class PlaceholderUpgradeCost extends SUpgradeCost {

    private final String placeholder;
    private final String takeCommand;

    public PlaceholderUpgradeCost(BigDecimal value, String placeholder, String takeCommand, UpgradeCostProvider provider) {
        super(value, provider);
        this.placeholder = placeholder;
        this.takeCommand = takeCommand;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public String getTakeCommand() {
        return takeCommand;
    }

}
